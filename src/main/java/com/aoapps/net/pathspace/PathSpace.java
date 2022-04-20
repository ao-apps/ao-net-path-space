/*
 * ao-net-path-space - Manages allocation of a path space between components.
 * Copyright (C) 2018, 2019, 2020, 2021, 2022  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-net-path-space.
 *
 * ao-net-path-space is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-net-path-space is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-net-path-space.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aoapps.net.pathspace;

import com.aoapps.collections.MinimalMap;
import com.aoapps.net.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Manages a set of {@link Prefix}, identifying conflicts and providing efficient lookup
 * even when many prefixes are in the path space.
 * <p>
 * Each path space has an associated value.
 * </p>
 * <p>
 * This class is thread-safe.
 * </p>
 *
 * @author  AO Industries, Inc.
 */
public class PathSpace<V> {

  private static final Logger logger = Logger.getLogger(PathSpace.class.getName());

  // TODO: private static final boolean ASSERTIONS_ENABLED = true;
  /* TODO: Selective assertions once fast lookup is implemented:
  static {
    boolean assertionsEnabled = false;
    assert assertionsEnabled = true; // Intentional side-effect
    ASSERTIONS_ENABLED = assertionsEnabled;
  }
   */

  // Java 1.8: StampedLock since not needing reentrant
  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private final Lock readLock = readWriteLock.readLock();
  private final Lock writeLock = readWriteLock.writeLock();

  /**
   * The index of all bounded prefixes (prefixes with multilevel type {@link Prefix.MultiLevelType#NONE}).
   * <p>
   * The main list is indexed by the total path depth, including the prefix depth and all wildcards.
   * This will always be at least one (for /*), so the index is offset by one.
   * </p>
   * <p>
   * The list for each path depth is indexed by the number of wildcard substitutions in the prefix.
   * This also will always be at least one (for /path/*), so the index is offset by one.
   * </p>
   * <p>
   * The map for each path depth and number of wildcards contains {@link Prefix#getBase() prefix base}
   * and the associated value.
   * </p>
   */
  private final List<List<Map<String, ImmutablePair<Prefix, V>>>> boundedIndex = new ArrayList<>();

  /**
   * The index of all unbounded prefixes (prefixes with multilevel types other than {@link Prefix.MultiLevelType#NONE}.
   * <p>
   * The main list is indexed by the total path depth, including the prefix depth and all wildcards (+1 for effectiveWildcards).
   * Effective wildcards will always be at least one (for /** or /***), so the index is offset by one.
   * </p>
   * <p>
   * The list for each path depth is indexed by the number of wildcard substitutions in the prefix.
   * This also will always be at least one (for /path/*), so the index is offset by one.
   * </p>
   * <p>
   * The map for each path depth and number of wildcards contains {@link Prefix#getBase() prefix base}
   * and the associated value.
   * </p>
   *
   * @see  #boundedIndex
   */
  private final List<List<Map<String, ImmutablePair<Prefix, V>>>> unboundedIndex = new ArrayList<>();

  /**
   * A sorted set to verify map lookup results are consistent with a sequential
   * scan, in natural ordering, of prefixes checking for the first match.
   *
   * @see  Prefix#compareTo(com.aoapps.net.pathspace.Prefix)
   * @see  Prefix#matches(com.aoapps.net.Path)
   */
  private final SortedMap<Prefix, V> sortedMap = new TreeMap<>();

  /**
   * Adds a new prefix to this space while checking for conflicts.
   * <p>
   * Note: This implementation is very simple and not optimized for performance.
   * It does a sequential scan for the conflict check.
   * </p>
   *
   * @throws  PrefixConflictException  If the prefix conflicts with an existing entry.
   *
   * @see  Prefix#conflictsWith(com.aoapps.net.pathspace.Prefix)
   */
  public void put(Prefix prefix, V value) throws PrefixConflictException {
    writeLock.lock();
    try {
      if (logger.isLoggable(Level.FINER)) {
        logger.finer("prefix = " + prefix);
      }
      // TODO: Could check for conflicts within the indexed structure instead of relying on sequential scan through all entries
      // TODO: But this would be optimizing the performance of the put method, which is only used on application start-up for our use-case.
      // Check for conflict
      for (Prefix existing : sortedMap.keySet()) {
        if (existing.conflictsWith(prefix)) {
          throw new PrefixConflictException(existing, prefix);
        }
      }
      // Add to map
      if (sortedMap.put(prefix, value) != null) {
        throw new AssertionError("Duplicate prefix should have been found as a conflict already: " + prefix);
      }
      // Add to index
      List<List<Map<String, ImmutablePair<Prefix, V>>>> totalDepthIndex;
      int wildcardsOffset;
      if (prefix.getMultiLevelType() == Prefix.MultiLevelType.NONE) {
        totalDepthIndex = boundedIndex;
        wildcardsOffset = 0;
      } else {
        totalDepthIndex = unboundedIndex;
        wildcardsOffset = 1;
      }
      Path base = prefix.getBase();
      String baseStr = base == Path.ROOT ? "" : base.toString();
      if (logger.isLoggable(Level.FINEST)) {
        logger.finest("baseStr = " + baseStr + ", wildcardsOffset = " + wildcardsOffset);
      }
      int baseDepth = StringUtils.countMatches(baseStr, Path.SEPARATOR_CHAR);
      int wildcards = prefix.getWildcards() + wildcardsOffset;
      int totalDepth = baseDepth + wildcards;
      if (logger.isLoggable(Level.FINEST)) {
        logger.finest("baseDepth = " + baseDepth + ", wildcards = " + wildcards + ", totalDepth = " + totalDepth);
      }
      while (totalDepthIndex.size() <= (totalDepth - 1)) {
        totalDepthIndex.add(null);
      }
      List<Map<String, ImmutablePair<Prefix, V>>> wildcardDepthIndex = totalDepthIndex.get(totalDepth - 1);
      if (wildcardDepthIndex == null) {
        wildcardDepthIndex = new ArrayList<>(wildcards);
        totalDepthIndex.set(totalDepth - 1, wildcardDepthIndex);
      }
      while (wildcardDepthIndex.size() <= (wildcards - 1)) {
        wildcardDepthIndex.add(null);
      }
      wildcardDepthIndex.set(
        wildcards - 1,
        MinimalMap.put(
          wildcardDepthIndex.get(wildcards - 1),
          baseStr,
          new ImmutablePair<>(prefix, value)
        )
      );
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Sequential implementation of {@link #get(com.aoapps.net.Path)} based on iterative
   * calls to {@link Prefix#matches(com.aoapps.net.Path)}
   * in the natural ordering established by {@link Prefix#compareTo(com.aoapps.net.pathspace.Prefix)}.
   * <p>
   * The caller must already hold {@link #readLock}
   * </p>
   */
  PathMatch<V> getSequential(Path path) {
    for (Map.Entry<Prefix, V> entry : sortedMap.entrySet()) {
      Prefix prefix = entry.getKey();
      int matchLen = prefix.matches(path);
      if (matchLen != -1) {
        Path prefixPath, subPath;
        if (matchLen == 0) {
          prefixPath = Path.ROOT;
          subPath = path;
        } else {
          prefixPath = path.prefix(matchLen);
          subPath = path.suffix(matchLen);
        }
        return new PathMatch<>(
          prefix,
          prefixPath,
          subPath,
          entry.getValue()
        );
      }
    }
    return null;
  }

  /**
   * Indexed implementation of {@link #get(com.aoapps.net.Path)}.
   * <p>
   * The caller must already hold {@link #readLock}
   * </p>
   */
  PathMatch<V> getIndexed(Path path) {
    // Search the path up to the deepest possibly used
    final String pathStr = path.toString();
    int pathStrLen = pathStr.length();
    // Find the deepest path used for matching
    int deepestPath = Math.max(unboundedIndex.size(), boundedIndex.size());
    if (logger.isLoggable(Level.FINER)) {
      logger.finer("pathStr = " + pathStr + ", pathStrLen = " + pathStrLen + ", deepestPath = " + deepestPath);
    }
    int pathDepth = 0;
    int lastSlashPos = 0;
    while (pathDepth < deepestPath) {
      pathDepth++;
      int slashPos = pathStr.indexOf(Path.SEPARATOR_CHAR, lastSlashPos + 1);
      if (slashPos == -1) {
        lastSlashPos = pathStrLen;
        break;
      } else {
        lastSlashPos = slashPos;
      }
    }
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("pathDepth = " + pathDepth + ", lastSlashPos = " + lastSlashPos);
    }
    // When at end of path, look for an exact-level match in bounded index
    if (lastSlashPos == pathStrLen && pathDepth <= boundedIndex.size()) {
      List<Map<String, ImmutablePair<Prefix, V>>> wildcardDepthIndex = boundedIndex.get(pathDepth - 1);
      if (wildcardDepthIndex != null) {
        int wildcardDepthIndexLen = wildcardDepthIndex.size();
        assert wildcardDepthIndexLen <= pathDepth : "wildcardDepthIndexLen <= pathDepth: " + wildcardDepthIndexLen + " <= " + pathDepth;
        int prevSlashPos1 = pathStr.lastIndexOf(Path.SEPARATOR_CHAR, lastSlashPos - 1);
        if (logger.isLoggable(Level.FINEST)) {
          logger.finest("prevSlashPos1 = " + prevSlashPos1);
        }
        assert prevSlashPos1 != -1 : "prevSlashPos1 != -1: " + prevSlashPos1 + " != -1";
        int searchSlashPos = prevSlashPos1;
        if (logger.isLoggable(Level.FINEST)) {
          logger.finest("wildcardDepthIndexLen = " + wildcardDepthIndexLen + ", pathDepth = " + pathDepth + ", searchSlashPos = " + searchSlashPos);
        }
        for (int i = 0; i < wildcardDepthIndexLen; i++) {
          Map<String, ImmutablePair<Prefix, V>> wildcardDepthMap = wildcardDepthIndex.get(i);
          if (wildcardDepthMap != null) {
            String searchStr1 = pathStr.substring(0, searchSlashPos);
            if (logger.isLoggable(Level.FINEST)) {
              logger.finest("Loop 1: searchStr1 = " + searchStr1);
            }
            ImmutablePair<Prefix, V> match = wildcardDepthMap.get(searchStr1);
            if (match != null) {
              // Return match
              Path prefixPath = (prevSlashPos1 == 0) ? Path.ROOT : path.prefix(prevSlashPos1);
              Path subPath = (prevSlashPos1 == 0) ? path : path.suffix(prevSlashPos1);
              if (logger.isLoggable(Level.FINER)) {
                logger.finer("returning 1: prefixPath = " + prefixPath + ", subPath = " + subPath);
              }
              return new PathMatch<>(
                match.getLeft(),
                prefixPath,
                subPath,
                match.getRight()
              );
            }
          }
          if (i < (wildcardDepthIndexLen -1 )) {
            int prevSearchSlashPos1 = pathStr.lastIndexOf(Path.SEPARATOR_CHAR, searchSlashPos - 1);
            if (logger.isLoggable(Level.FINEST)) {
              logger.finest("Loop 1: prevSearchSlashPos1 = " + prevSearchSlashPos1);
            }
            assert prevSearchSlashPos1 != -1 : "prevSearchSlashPos1 != -1: " + prevSearchSlashPos1 + " != -1";
            searchSlashPos = prevSearchSlashPos1;
          }
        }
      }
    }
    // Search backwards for any matching unbounded index
    int unboundedIndexSize = unboundedIndex.size();
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("unboundedIndexSize = " + unboundedIndexSize);
    }
    while (pathDepth > unboundedIndexSize) {
      lastSlashPos = pathStr.lastIndexOf(Path.SEPARATOR_CHAR, lastSlashPos - 1);
      assert lastSlashPos != -1 : "lastSlashPos != -1: " + lastSlashPos + " != -1";
      pathDepth--;
    }
    while (pathDepth > 0) {
      if (logger.isLoggable(Level.FINEST)) {
        logger.finest("Loop 2: pathDepth = " + pathDepth + ", lastSlashPos = " + lastSlashPos);
      }
      int prevSlashPos2 = pathStr.lastIndexOf(Path.SEPARATOR_CHAR, lastSlashPos - 1);
      if (logger.isLoggable(Level.FINEST)) {
        logger.finest("prevSlashPos2 = " + prevSlashPos2);
      }
      assert prevSlashPos2 != -1 : "prevSlashPos2 != -1: " + prevSlashPos2 + " != -1";
      List<Map<String, ImmutablePair<Prefix, V>>> unboundedDepthIndex = unboundedIndex.get(pathDepth - 1);
      if (unboundedDepthIndex != null) {
        int unboundedDepthIndexLen = unboundedDepthIndex.size();
        assert unboundedDepthIndexLen <= pathDepth : "unboundedDepthIndexLen <= pathDepth: " + unboundedDepthIndexLen + " <= " + pathDepth;
        int searchSlashPos = prevSlashPos2;
        if (logger.isLoggable(Level.FINEST)) {
          logger.finest("unboundedDepthIndexLen = " + unboundedDepthIndexLen + ", pathDepth = " + pathDepth + ", searchSlashPos = " + searchSlashPos);
        }
        for (int i = 0; i < unboundedDepthIndexLen; i++) {
          Map<String, ImmutablePair<Prefix, V>> wildcardDepthMap = unboundedDepthIndex.get(i);
          if (wildcardDepthMap != null) {
            String searchStr = pathStr.substring(0, searchSlashPos);
            if (logger.isLoggable(Level.FINEST)) {
              logger.finest("Loop 2.1: searchStr = " + searchStr);
            }
            ImmutablePair<Prefix, V> match = wildcardDepthMap.get(searchStr);
            if (match != null) {
              // Return match
              Path prefixPath = (prevSlashPos2 == 0) ? Path.ROOT : path.prefix(prevSlashPos2);
              Path subPath = (prevSlashPos2 == 0) ? path : path.suffix(prevSlashPos2);
              if (logger.isLoggable(Level.FINER)) {
                logger.finer("returning 2: prefixPath = " + prefixPath + ", subPath = " + subPath);
              }
              return new PathMatch<>(
                match.getLeft(),
                prefixPath,
                subPath,
                match.getRight()
              );
            }
          }
          if (i < (unboundedDepthIndexLen - 1)) {
            int prevSearchSlashPos2 = pathStr.lastIndexOf(Path.SEPARATOR_CHAR, searchSlashPos - 1);
            assert prevSearchSlashPos2 != -1 : "prevSearchSlashPos2 != -1: " + prevSearchSlashPos2 + " != -1";
            if (logger.isLoggable(Level.FINEST)) {
              logger.finest("Loop 2.1: prevSearchSlashPos2 = " + prevSearchSlashPos2);
            }
            searchSlashPos = prevSearchSlashPos2;
          }
        }
      }
      lastSlashPos = prevSlashPos2;
      pathDepth--;
    }
    if (logger.isLoggable(Level.FINER)) {
      logger.finer("returning null");
    }
    return null;
  }

  /**
   * Gets the prefix associated with the given path.
   *
   * @return  The matching prefix or {@code null} if no match
   */
  public PathMatch<V> get(Path path) {
    readLock.lock();
    try {
      PathMatch<V> indexedMatch = getIndexed(path);
      assert Objects.equals(indexedMatch, getSequential(path))
        : "Indexed get inconsistent with sequential get: path = " + path + ", indexedMatch = " + indexedMatch + ", sequentialMatch = " + getSequential(path);
      return indexedMatch;
    } finally {
      readLock.unlock();
    }
  }
}

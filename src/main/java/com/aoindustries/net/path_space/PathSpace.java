/*
 * ao-net-path-space - Manages allocation of a path space between components.
 * Copyright (C) 2018  AO Industries, Inc.
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
 * along with ao-net-path-space.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.net.path_space;

import com.aoindustries.lang.NotImplementedException;
import com.aoindustries.net.Path;
import com.aoindustries.validation.ValidationException;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Manages a set of {@link Prefix}, identifying conflicts and providing efficient lookup
 * even when many prefixes are in the path space.
 * <p>
 * Each path space has an associated value.
 * </p>
 * <p>
 * This class is not implemented in a thread-safe manner.  External synchronization is required
 * in a multi-threaded use.
 * </p>
 * TODO: Should this implement Map&lt;Prefix, V&gt;?
 *
 * @author  AO Industries, Inc.
 */
public class PathSpace <V> {

	private static final boolean ASSERTIONS_ENABLED = true;
	/* TODO: Selective assertions once fast lookup is implemented:
	static {
		boolean assertionsEnabled = false;
		assert assertionsEnabled = true; // Intentional side-effect
		ASSERTIONS_ENABLED = assertionsEnabled;
	}
	 */

	private final Map<Prefix, V> map = new HashMap<Prefix, V>();

	/**
	 * A sorted set to verify map lookup results are consistent with a sequential
	 * scan, in natural ordering, of prefixes checking for the first match.
	 *
	 * @see  Prefix#compareTo(com.aoindustries.net.path_space.Prefix)
	 * @see  Prefix#matches(com.aoindustries.net.Path)
	 */
	private final SortedMap<Prefix, V> sortedMap = ASSERTIONS_ENABLED ? new TreeMap<Prefix, V>() : null;

	/**
	 * Adds a new prefix to this space while checking for conflicts.
	 *
	 * @implNote  This implementation is very simple and not optimized for performance.
	 *            It does a sequential scan for the conflict check.
	 *
	 * @throws  PrefixConflictException  If the prefix conflicts with an existing entry
	 *
	 * @see  Prefix#conflictsWith(com.aoindustries.net.path_space.Prefix)
	 */
	public void put(Prefix prefix, V value) throws PrefixConflictException {
		// Check for conflict
		for(Prefix existing : map.keySet()) {
			if(existing.conflictsWith(prefix)) {
				throw new PrefixConflictException(existing, prefix);
			}
		}
		// Add to map
		V existingValue = map.put(prefix, value);
		if(existingValue != null) throw new ConcurrentModificationException();
		// Add to sorted map when performing assertions
		if(ASSERTIONS_ENABLED) {
			existingValue = sortedMap.put(prefix, value);
			if(existingValue != null) throw new ConcurrentModificationException();
		}
	}

	/**
	 * The result of a call to {@link #get(com.aoindustries.net.Path)}.
	 */
	public static class PathMatch<V> {
		private final Prefix prefix;
		private final Path prefixPath;
		private final Path subPath;
		private final V value;

		PathMatch(
			Prefix prefix,
			Path prefixPath,
			Path subPath,
			V value
		) {
			this.prefix = prefix;
			this.prefixPath = prefixPath;
			this.subPath = subPath;
			this.value = value;
		}

		@Override
		public String toString() {
			return prefixPath.toString() + '!' + subPath.toString();
		}

		/**
		 * Gets the prefix what matched the lookup.
		 */
		public Prefix getPrefix() {
			return prefix;
		}

		/**
		 * Gets the portion of the lookup path that matches the prefix.
		 */
		public Path getPrefixPath() {
			return prefixPath;
		}

		/**
		 * Gets the portion of the lookup path past the prefix path.
		 */
		public Path getSubPath() {
			return subPath;
		}

		/**
		 * Gets the value associated with the prefix.
		 */
		public V getValue() {
			return value;
		}
	}

	/**
	 * Gets the prefix associated with the given path.
	 *
	 * @return  The matching prefix or {@code null} if no match
	 */
	public PathMatch<V> get(Path path) {
		if(!ASSERTIONS_ENABLED) {
			throw new NotImplementedException("TODO: Implement fast look-up version");
		} else {
			// Perform sequential scan for assertions
			PathMatch<V> sequentialMatch = null;
			for(Map.Entry<Prefix, V> entry : sortedMap.entrySet()) {
				Prefix prefix = entry.getKey();
				int matchLen = prefix.matches(path);
				if(matchLen != -1) {
					Path prefixPath;
					try {
						prefixPath = matchLen == 0 ? Path.ROOT : Path.valueOf(path.toString().substring(0, matchLen));
					} catch(ValidationException e) {
						AssertionError ae = new AssertionError("A path prefix of a valid path is also valid for length " + matchLen + ": " + path);
						ae.initCause(e);
						throw ae;
					}
					Path subPath;
					try {
						subPath = matchLen == 0 ? path : Path.valueOf(path.toString().substring(matchLen));
					} catch(ValidationException e) {
						AssertionError ae = new AssertionError("A sub-path of a valid path is also valid from position " + matchLen + ": " + path);
						ae.initCause(e);
						throw ae;
					}
					sequentialMatch = new PathMatch<V>(
						prefix,
						prefixPath,
						subPath,
						entry.getValue()
					);
					break;
				}
			}
			// TODO: Don't return this, but compare for equality with fast implementation
			return sequentialMatch;
		}
	}
}

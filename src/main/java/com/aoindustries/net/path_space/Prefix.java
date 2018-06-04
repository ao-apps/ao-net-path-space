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
import com.aoindustries.lang.NullArgumentException;
import com.aoindustries.net.Path;
import com.aoindustries.util.ComparatorUtils;
import com.aoindustries.validation.ValidationException;

/**
 * @author  AO Industries, Inc.
 */
public final class Prefix implements Comparable<Prefix> {

	public static final char WILDCARD_CHAR = '*';

	/**
	 * @see  Path#SEPARATOR_STRING
	 * @see  #WILDCARD_CHAR
	 */
	public static final String WILDCARD_SUFFIX = Path.SEPARATOR_STRING + WILDCARD_CHAR;
	private static final int WILDCARD_SUFFIX_LEN = WILDCARD_SUFFIX.length();

	/**
	 * @see  MultiLevelType#UNBOUNDED
	 */
	public static final String UNBOUNDED_SUFFIX = WILDCARD_SUFFIX + WILDCARD_CHAR;
	private static final int UNBOUNDED_SUFFIX_LEN = UNBOUNDED_SUFFIX.length();

	/**
	 * @see  MultiLevelType#GREEDY
	 */
	public static final String GREEDY_SUFFIX = UNBOUNDED_SUFFIX + WILDCARD_CHAR;
	private static final int GREEDY_SUFFIX_LEN = GREEDY_SUFFIX.length();

	/**
	 * @implNote This ordering is important for the implementation of {@link Prefix#compareTo(com.aoindustries.net.path_space.Prefix)}.
	 */
	public enum MultiLevelType {
		NONE("", true),
		UNBOUNDED(UNBOUNDED_SUFFIX, true),
		GREEDY(GREEDY_SUFFIX, false);

		private final String suffix;
		private final boolean allowsSubspaces;

		private MultiLevelType(String suffix, boolean allowsSubspaces) {
			this.suffix = suffix;
			this.allowsSubspaces = allowsSubspaces;
		}

		/**
		 * Gets the suffix applied to the string representation of prefixes
		 * of this multi-level type or {@code ""} if no suffix added.
		 *
		 * @see  Prefix#toString()
		 */
		public String getSuffix() {
			return suffix;
		}

		/**
		 * Does this multi-level type allow subspaces to be created within its space?
		 */
		public boolean getAllowsSubspaces() {
			return allowsSubspaces;
		}
	}

	// TODO: Not allow any other number of asterisk-only path elements, such as /**** or /****/, which is probably a typo?
	private static void checkBase(Path base, int wildcards, MultiLevelType multiLevelType) throws IllegalArgumentException {
		if(base != Path.ROOT) {
			String baseStr = base.toString();
			char lastChar = baseStr.charAt(baseStr.length() - 1);
			if(lastChar == Path.SEPARATOR_CHAR) {
				throw new IllegalArgumentException("Prefix base may not end with slash \"" + Path.SEPARATOR_CHAR + "\" unless it is the root \"" + Path.ROOT + "\" itself: " + base);
			}
			// May not end in "/*", "/**", or "/***".
			if(baseStr.endsWith(WILDCARD_SUFFIX)) {
				throw new IllegalArgumentException("Prefix base may not end in " + WILDCARD_SUFFIX + ": " + base);
			}
			if(baseStr.endsWith(UNBOUNDED_SUFFIX)) {
				throw new IllegalArgumentException("Prefix base may not end in " + UNBOUNDED_SUFFIX + ": " + base);
			}
			if(baseStr.endsWith(GREEDY_SUFFIX)) {
				throw new IllegalArgumentException("Prefix base may not end in " + GREEDY_SUFFIX + ": " + base);
			}
			// May not contain "/*/", "/**/", or "/***/" to avoid any expectations of infix matching, which is not supported.
			int firstPos = baseStr.indexOf(WILDCARD_SUFFIX); // Quick check if any "/*" found
			if(firstPos != -1) {
				if(
					baseStr.indexOf(WILDCARD_SUFFIX + Path.SEPARATOR_CHAR, firstPos) != -1
					|| baseStr.indexOf(UNBOUNDED_SUFFIX + Path.SEPARATOR_CHAR, firstPos) != -1
					|| baseStr.indexOf(GREEDY_SUFFIX + Path.SEPARATOR_CHAR, firstPos) != -1
				) {
					throw new IllegalArgumentException("Infix wildcards not supported: " + base);
				}
			}
		}
	}

	/**
	 * Gets an instance of a prefix given the individual fields.
	 *
	 * @param base  May not be {@code null}.  May not end in "/" unless it is the root "/".
	 *              May not end in "/*", "/**", or "/***".
	 *              May not contain "/*&#47;", "/**&#47;", or "/***&#47;" to avoid any
	 *              expectations of infix matching, which is not supported.
	 *
	 * @param wildcards  Must be {@code >= 0}.  Must be {@code >= 1} when multiLevelType is {@link MultiLevelType#NONE}
	 *
	 * @param multiLevelType May not be {@code null}
	 *
	 * @see  #valueOf(java.lang.String)
	 */
	public static Prefix valueOf(Path base, int wildcards, MultiLevelType multiLevelType) {
		NullArgumentException.checkNotNull(base, "base");
		NullArgumentException.checkNotNull(multiLevelType, "multiLevelType");
		if(multiLevelType == MultiLevelType.NONE) {
			if(wildcards < 1) throw new IllegalArgumentException("wildcards < 1: " + wildcards);
		} else {
			if(wildcards < 0) throw new IllegalArgumentException("wildcards < 0: " + wildcards);
		}
		// May not end in "/" when there is any wildcard or multi-level, unless it is the root "/".
		checkBase(base, wildcards, multiLevelType);
		return new Prefix(
			base,
			wildcards,
			multiLevelType
		);
	}

	/**
	 * Parses the unambiguous string representation of a prefix.
	 * <p>
	 * This is the inverse function of {@link #toString()}.
	 * </p>
	 *
	 * @param  prefix  The prefix to parse.  Must adhere to all rules of {@link  #valueOf(com.aoindustries.net.Path, int, com.aoindustries.net.path_space.Prefix.MultiLevelType)}
	 *
	 * @see  #toString()
	 * @see  #valueOf(com.aoindustries.net.Path, int, com.aoindustries.net.path_space.Prefix.MultiLevelType)
	 */
	public static Prefix valueOf(String prefix) {
		NullArgumentException.checkNotNull(prefix, "prefix");
		// The number of characters left in the parsing
		int prefixLen = prefix.length();
		// Parse multi-level
		MultiLevelType multiLevelType;
		if(prefix.endsWith(GREEDY_SUFFIX)) {
			multiLevelType = MultiLevelType.GREEDY;
			prefixLen -= GREEDY_SUFFIX_LEN;
		} else if(prefix.endsWith(UNBOUNDED_SUFFIX)) {
			multiLevelType = MultiLevelType.UNBOUNDED;
			prefixLen -= UNBOUNDED_SUFFIX_LEN;
		} else {
			multiLevelType = MultiLevelType.NONE;
		}
		// Parse wildcards
		int wildcards = 0;
		while(
			prefixLen >= WILDCARD_SUFFIX_LEN
			&& prefix.regionMatches(prefixLen - WILDCARD_SUFFIX_LEN, WILDCARD_SUFFIX, 0, WILDCARD_SUFFIX_LEN)
		) {
			wildcards++;
			prefixLen -= WILDCARD_SUFFIX_LEN;
		}
		if(multiLevelType == MultiLevelType.NONE) {
			if(wildcards < 1) throw new IllegalArgumentException("prefix does not end with any type of wildcard: " + prefix);
		}
		Path base;
		if(prefixLen == 0) {
			base = Path.ROOT;
		} else {
			try {
				base = Path.valueOf(prefix.substring(0, prefixLen));
			} catch(ValidationException e) {
				throw new IllegalArgumentException(e);
			}
		}
		checkBase(base, wildcards, multiLevelType);
		return new Prefix(base, wildcards, multiLevelType);
	}

	private final Path base;

	private final int wildcards;

	private final MultiLevelType multiLevelType;

	/**
	 * Validity checks are performed in the @{code valueOf} methods because this class is used internally
	 * for map lookups.  These lookups avoid the validity checks on these short-lived key lookup instances.
	 *
	 * @see  #valueOf(com.aoindustries.net.Path, int, com.aoindustries.net.path_space.Prefix.MultiLevelType)
	 * @see  #valueOf(java.lang.String)
	 */
	private Prefix(Path base, int wildcards, MultiLevelType multiLevelType) {
		this.base = base;
		this.wildcards = wildcards;
		this.multiLevelType = multiLevelType;
	}

	@Override
	public int hashCode() {
		int hash = base.hashCode();
		hash = hash * 31 + wildcards;
		hash = hash * 31 + multiLevelType.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Prefix)) return false;
		Prefix other = (Prefix)obj;
		return
			wildcards == other.wildcards
			&& multiLevelType == other.multiLevelType
			&& base.equals(other.base);
	}

	/**
	 * Gets the unambiguous string representation of this prefix.
	 * <p>
	 * This is the inverse function of {@link #valueOf(java.lang.String)}.
	 * </p>
	 * @see  MultiLevelType#getSuffix()
	 *
	 * @see  #valueOf(java.lang.String)
	 */
	@Override
	public String toString() {
		String baseStr = base.toString();
		int baseStrLen = baseStr.length();
		if(baseStr.charAt(baseStrLen - 1) == Path.SEPARATOR_CHAR) {
			assert base == Path.ROOT : "Only the root may end in slash: " + base;
			baseStrLen--;
		}
		String multiLevelSuffix = multiLevelType.suffix;
		int multiLevelSuffixLen = multiLevelSuffix.length();
		int toStringLen = baseStrLen + wildcards * WILDCARD_SUFFIX_LEN + multiLevelSuffixLen;
		StringBuilder sb = new StringBuilder(toStringLen);
		sb.append(baseStr, 0, baseStrLen);
		for(int i = 0; i < wildcards; i++) sb.append(WILDCARD_SUFFIX);
		sb.append(multiLevelSuffix);
		assert toStringLen == sb.length();
		return sb.toString();
	}

	/**
	 * The natural ordering is such that an iterative call to {@link #matches(com.aoindustries.net.Path)} will return.
	 * {@code true} on the most specific matching space.  This match is consistent with TODO: link findSpace.
	 * <p>
	 * This ordering is useful for human review, as it represents the path space conceptually in a top-to-bottom list.
	 * </p>
	 * <p>
	 * The implementation of TODO: Link findSpace, should be much faster than an iterative search, however.
	 * </p>
	 * <p>
	 * There are no ordering guarantees between prefixes that {@link #conflictsWith(com.aoindustries.net.path_space.Prefix) conflict with one another}.
	 * </p>
	 *
	 * @see  #conflictsWith(com.aoindustries.net.path_space.Prefix)
	 * @see  #matches(com.aoindustries.net.Path)
	 */
	@Override
	public int compareTo(Prefix other) {
		// TODO: Throw exception if trying to compare two conflicting paths?

		// base ascending
		int diff = base.compareTo(other.base);
		if(diff != 0) return diff;

		// Ending /*, /**, and /*** all count as the same number of wildcards
		int effectiveWildcards = wildcards;
		if(multiLevelType != MultiLevelType.NONE) effectiveWildcards++;
		int effectiveWildcardsOther = other.wildcards;
		if(other.multiLevelType != MultiLevelType.NONE) effectiveWildcardsOther++;

		// wildcards descending (this means has wildcards before no wildcards)
		diff = ComparatorUtils.compare(effectiveWildcardsOther, effectiveWildcards);
		if(diff != 0) return diff;

		// multiLevelType descending (Order by /***, /**, NONE)
		return other.multiLevelType.compareTo(multiLevelType);
	}

	/**
	 * Gets the base of this path space.  This does not include any trailing
	 * wildcard or multi-level suffixes.  This will not end in a slash (/)
	 * unless this is the root "/" ({@link Path#ROOT}) itself.
	 */
	public Path getBase() {
		return base;
	}

	/**
	 * Gets the number of wildcard levels attached to the base.
	 * This will be at least one when {@link #getMultiLevelType()} is {@link MultiLevelType#NONE}.
	 * Otherwise, may also be zero.
	 */
	public int getWildcards() {
		return wildcards;
	}

	/**
	 * Gets the multi-level type attached to the base and any wildcard levels.
	 */
	public MultiLevelType getMultiLevelType() {
		return multiLevelType;
	}

	/**
	 * Checks if two prefixes are conflicting.
	 * Conflicts include either occupying the same space or a subspace of another greedy space, with all wildcards considered.
	 */
	public boolean conflictsWith(Prefix other) {
		// A simple forward matching implementation that goes one slash at a time
		Prefix prefix1 = this;
		Prefix prefix2 = other;
		String base1 = prefix1.base.toString();
		if(base1.length() == 1) base1 = "";
		int base1Len = base1.length();
		String base2 = prefix2.base.toString();
		if(base2.length() == 1) base2 = "";
		int base2Len = base2.length();
		int effectiveWildcards1 = prefix1.wildcards;
		if(prefix1.multiLevelType != MultiLevelType.NONE) effectiveWildcards1++;
		int effectiveWildcards2 = prefix2.wildcards;
		if(prefix2.multiLevelType != MultiLevelType.NONE) effectiveWildcards2++;

		int lastSlashPos1 = 0;
		int wildcardsUsed1 = 0;
		int lastSlashPos2 = 0;
		int wildcardsUsed2 = 0;
		while(true) {
			// Find actual path elements or go into wildcard space if past base
			int path1Start, path1Len;
			boolean isWildcard1;
			boolean isGreedy1;
			if(lastSlashPos1 < base1Len) {
				int slashPos = base1.indexOf(Path.SEPARATOR_STRING, lastSlashPos1 + 1);
				int nextSlashPos = slashPos == -1 ? base1Len : slashPos;
				path1Start = lastSlashPos1 + 1;
				path1Len = nextSlashPos - path1Start;
				lastSlashPos1 = nextSlashPos;
				isWildcard1 = false;
				isGreedy1 = false;
			} else {
				path1Start = path1Len = -1;
				// Consume wildcards, as long as still have some
				if(wildcardsUsed1 < effectiveWildcards1) {
					isWildcard1 = true;
					wildcardsUsed1++;
					isGreedy1 = false;
				} else {
					isWildcard1 = false;
					isGreedy1 = prefix1.multiLevelType == MultiLevelType.GREEDY;
				}
			}
			int path2Start, path2Len;
			boolean isWildcard2;
			boolean isGreedy2;
			if(lastSlashPos2 < base2Len) {
				int slashPos = base2.indexOf(Path.SEPARATOR_STRING, lastSlashPos2 + 1);
				int nextSlashPos = slashPos == -1 ? base2Len : slashPos;
				path2Start = lastSlashPos2 + 1;
				path2Len = nextSlashPos - path2Start;
				lastSlashPos2 = nextSlashPos;
				isWildcard2 = false;
				isGreedy2 = false;
			} else {
				path2Start = path2Len = -1;
				// Consume wildcards, as long as still have some
				if(wildcardsUsed2 < effectiveWildcards2) {
					isWildcard2 = true;
					wildcardsUsed2++;
					isGreedy2 = false;
				} else {
					isWildcard2 = false;
					isGreedy2 = prefix2.multiLevelType == MultiLevelType.GREEDY;
				}
			}
			if(path1Start != -1) {
				if(path2Start != -1) {
					// Both path elements exist, must match
					assert path1Start != -1;
					assert path2Start != -1;
					if(
						path1Len != path2Len
						|| !base1.regionMatches(path1Start, base2, path2Start, path1Len)
					) return false;
					// Keep searching
				} else {
					assert path1Start != -1;
					assert path2Start == -1;
					if(isGreedy2) return true;
					// Keep searching
				}
			} else {
				if(path2Start != -1) {
					assert path1Start == -1;
					assert path2Start != -1;
					if(isGreedy1) return true;
					// Keep searching
				} else {
					assert path1Start == -1;
					assert path2Start == -1;
					if(isWildcard1) {
						if(isWildcard2) {
							assert isWildcard1;
							assert isWildcard2;
							// Keep searching
						} else {
							assert isWildcard1;
							assert !isWildcard2;
							return isGreedy2;
						}
					} else {
						if(isWildcard2) {
							assert !isWildcard1;
							assert isWildcard2;
							return isGreedy1;
						} else {
							assert !isWildcard1;
							assert !isWildcard2;
							return isGreedy1 || isGreedy2 || (!isGreedy1 && !isGreedy2);
						}
					}
				}
			}
		}
	}

	/**
	 * Checks if a given path matches this prefix.
	 */
	public boolean matches(Path path) {
		throw new NotImplementedException();
	}
}

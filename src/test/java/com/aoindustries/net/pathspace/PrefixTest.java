/*
 * ao-net-path-space - Manages allocation of a path space between components.
 * Copyright (C) 2018, 2020  AO Industries, Inc.
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
package com.aoindustries.net.pathspace;

import com.aoindustries.net.Path;
import com.aoindustries.net.pathspace.Prefix.MultiLevelType;
import static com.aoindustries.net.pathspace.Prefix.valueOf;
import com.aoindustries.validation.ValidationException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @see Prefix
 *
 * @author  AO Industries, Inc.
 */
public class PrefixTest {
	
	// <editor-fold defaultstate="collapsed" desc="Test valueOf both by fields and by parsing String">
	private static void testValueOf(Path base, int wildcards, MultiLevelType multiLevelType, String toString) {
		Prefix p1 = valueOf(base, wildcards, multiLevelType);
		Prefix p2 = valueOf(toString);
		assertEquals(
			p1,
			p2
		);
		assertEquals(toString, p1.toString());
		assertEquals(toString, p2.toString());
	}

	private static void testValueOf(String base, int wildcards, MultiLevelType multiLevelType, String toString) throws ValidationException {
		testValueOf(Path.valueOf(base), wildcards, multiLevelType, toString);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValueOfRoot() {
		testValueOf(
			Path.ROOT, 0, Prefix.MultiLevelType.NONE,
			"/"
		);
	}

	@Test
	public void testValueOfRootUnbounded() {
		testValueOf(
			Path.ROOT, 0, Prefix.MultiLevelType.UNBOUNDED,
			"/**"
		);
	}

	@Test
	public void testValueOfRootGreedy() {
		testValueOf(
			Path.ROOT, 0, Prefix.MultiLevelType.GREEDY,
			"/***"
		);
	}

	@Test
	public void testValueOfRootWildcard() {
		testValueOf(
			Path.ROOT, 1, Prefix.MultiLevelType.NONE,
			"/*"
		);
	}

	@Test
	public void testValueOfRootWildcardUnbounded() {
		testValueOf(
			Path.ROOT, 1, Prefix.MultiLevelType.UNBOUNDED,
			"/*/**"
		);
	}

	@Test
	public void testValueOfRootWildcardGreedy() {
		testValueOf(
			Path.ROOT, 1, Prefix.MultiLevelType.GREEDY,
			"/*/***"
		);
	}

	@Test
	public void testValueOfRootWildcard2() {
		testValueOf(
			Path.ROOT, 2, Prefix.MultiLevelType.NONE,
			"/*/*"
		);
	}

	@Test
	public void testValueOfRootWildcard2Unbounded() {
		testValueOf(
			Path.ROOT, 2, Prefix.MultiLevelType.UNBOUNDED,
			"/*/*/**"
		);
	}

	@Test
	public void testValueOfRootWildcard2Greedy() {
		testValueOf(
			Path.ROOT, 2, Prefix.MultiLevelType.GREEDY,
			"/*/*/***"
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValueOfPath() throws ValidationException {
		testValueOf(
			"/path", 0, Prefix.MultiLevelType.NONE,
			"/path"
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValueOfPathSlash() throws ValidationException {
		testValueOf(
			"/path/", 0, Prefix.MultiLevelType.NONE,
			"/path/"
		);
	}

	@Test
	public void testValueOfPathUnbounded() throws ValidationException {
		testValueOf(
			"/path", 0, Prefix.MultiLevelType.UNBOUNDED,
			"/path/**"
		);
	}

	@Test
	public void testValueOfPathGreedy() throws ValidationException {
		testValueOf(
			"/path", 0, Prefix.MultiLevelType.GREEDY,
			"/path/***"
		);
	}

	@Test
	public void testValueOfPathWildcard() throws ValidationException {
		testValueOf(
			"/path", 1, Prefix.MultiLevelType.NONE,
			"/path/*"
		);
	}

	@Test
	public void testValueOfPathWildcardUnbounded() throws ValidationException {
		testValueOf(
			"/path", 1, Prefix.MultiLevelType.UNBOUNDED,
			"/path/*/**"
		);
	}

	@Test
	public void testValueOfPathWildcardGreedy() throws ValidationException {
		testValueOf(
			"/path", 1, Prefix.MultiLevelType.GREEDY,
			"/path/*/***"
		);
	}

	@Test
	public void testValueOfPathWildcard2() throws ValidationException {
		testValueOf(
			"/path", 2, Prefix.MultiLevelType.NONE,
			"/path/*/*"
		);
	}

	@Test
	public void testValueOfPathWildcard2Unbounded() throws ValidationException {
		testValueOf(
			"/path", 2, Prefix.MultiLevelType.UNBOUNDED,
			"/path/*/*/**"
		);
	}

	@Test
	public void testValueOfPathWildcard2Greedy() throws ValidationException {
		testValueOf(
			"/path", 2, Prefix.MultiLevelType.GREEDY,
			"/path/*/*/***"
		);
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Test valueOf argument checks, both by fields and by parsing String">
	@Test(expected = IllegalArgumentException.class)
	public void testValueOfBaseNotNull() throws ValidationException {
		valueOf(null, 0, MultiLevelType.NONE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValueOfWildcardsNotNegative() throws ValidationException {
		valueOf(Path.ROOT, -1, MultiLevelType.NONE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValueOfMultiLevelTypeNotNull() throws ValidationException {
		valueOf(Path.ROOT, 0, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValueOfPrefixNotNull() {
		valueOf(null);
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Test valueOf base validations, both by fields and by parsing String">
	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseEndSlashWildcardFields() throws ValidationException {
		valueOf(Path.valueOf("/path/"), 1, MultiLevelType.NONE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseEndSlashWildcardString() {
		valueOf("/path//*");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseEndSlashUnboundedFields() throws ValidationException {
		valueOf(Path.valueOf("/path/"), 0, MultiLevelType.UNBOUNDED);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseEndSlashUnboundedString() {
		valueOf("/path//**");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseEndSlashGreedyFields() throws ValidationException {
		valueOf(Path.valueOf("/path/"), 0, MultiLevelType.GREEDY);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseEndSlashGreedyStringWildcard() {
		valueOf("/path//*");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseEndSlashGreedyStringUnbounded() {
		valueOf("/path//**");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseEndSlashGreedyStringGreedy() {
		valueOf("/path//***");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoEndWildcardFields() throws ValidationException {
		valueOf(Path.valueOf("/path/*"), 0, MultiLevelType.NONE);
	}

	@Test
	public void testCheckBaseNoEndWildcardStringWildcard() {
		// No way to check
	}

	@Test
	public void testCheckBaseNoEndWildcardStringUnbounded() {
		// No way to check
	}

	@Test
	public void testCheckBaseNoEndWildcardStringGreedy() {
		// No way to check
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoEndUnboundedFields() throws ValidationException {
		valueOf(Path.valueOf("/path/**"), 0, MultiLevelType.NONE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoEndUnboundedStringWildcard() {
		valueOf("/path/**/*");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoEndUnboundedStringUnbounded() {
		valueOf("/path/**/**");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoEndUnboundedStringGreedy() {
		valueOf("/path/**/***");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoEndGreedyFields() throws ValidationException {
		valueOf(Path.valueOf("/path/***"), 0, MultiLevelType.NONE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoEndGreedyStringWildcard() {
		valueOf("/path/***/*");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoEndGreedyStringUnbounded() {
		valueOf("/path/***/**");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoEndGreedyStringGreedy() {
		valueOf("/path/***/***");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoContainsWildcardFields() throws ValidationException {
		valueOf(Path.valueOf("/path/*/other"), 0, MultiLevelType.NONE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoContainsWildcardStringWildcard() {
		valueOf("/path/*/other/*");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoContainsWildcardStringUnbounded() {
		valueOf("/path/*/other/**");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoContainsWildcardStringGreedy() {
		valueOf("/path/*/other/***");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoContainsUnboundedFields() throws ValidationException {
		valueOf(Path.valueOf("/path/**/other"), 0, MultiLevelType.NONE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoContainsUnboundedStringWildcard() {
		valueOf("/path/**/other/*");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoContainsUnboundedStringUnbounded() {
		valueOf("/path/**/other/**");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoContainsUnboundedStringGreedy() {
		valueOf("/path/**/other/***");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoContainsGreedyFields() throws ValidationException {
		valueOf(Path.valueOf("/path/***/other"), 0, MultiLevelType.NONE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoContainsGreedyStringWildcard() {
		valueOf("/path/***/other/*");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoContainsGreedyStringUnbounded() {
		valueOf("/path/***/other/**");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckBaseNoContainsGreedyStringGreedy() {
		valueOf("/path/***/other/***");
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Test compareTo">
	private static void assertCompareEqual(String prefix1, String prefix2) {
		Prefix p1 = valueOf(prefix1);
		Prefix p2 = valueOf(prefix2);
		assertEquals(
			"Prefixes do not compare as equal: \"" + p1 + "\" and \"" + p2 + '"',
			0,
			p1.compareTo(p2)
		);
		assertEquals(
			"Prefixes do not compare as equal (reversed): \"" + p2 + "\" and \"" + p1 + '"',
			0,
			p2.compareTo(p1)
		);
	}

	private static void assertCompareBefore(String prefix1, String ... others) {
		Prefix p1 = valueOf(prefix1);
		for(String prefix2 : others) {
			Prefix p2 = valueOf(prefix2);
			assertEquals(
				"Prefix does not compare as before: \"" + p1 + "\" and \"" + p2 + '"',
				-1,
				Integer.signum(p1.compareTo(p2))
			);
			assertEquals(
				"Prefix does not compare as after (reversed): \"" + p2 + "\" and \"" + p1 + '"',
				1,
				Integer.signum(p2.compareTo(p1))
			);
		}
	}

	private static void assertCompareAfter(String prefix1, String ... others) {
		Prefix p1 = valueOf(prefix1);
		for(String prefix2 : others) {
			Prefix p2 = valueOf(prefix2);
			assertEquals(
				"Prefix does not compare as after: \"" + p1 + "\" and \"" + p2 + '"',
				1,
				Integer.signum(p1.compareTo(p2))
			);
			assertEquals(
				"Prefix does not compare as before (reversed): \"" + p2 + "\" and \"" + p1 + '"',
				-1,
				Integer.signum(p2.compareTo(p1))
			);
		}
	}

	@Test
	public void testCompareToRootEqualsRootWildcard() {
		assertCompareEqual("/*", "/*");
	}

	@Test
	public void testCompareToRootEqualsRootUnbounded() {
		assertCompareEqual("/**", "/**");
	}

	@Test
	public void testCompareToRootEqualsRootGreedy() {
		assertCompareEqual("/***", "/***");
	}

	@Test
	public void testCompareToRootGreedyBeforeUnbounded() {
		assertCompareBefore("/***", "/**");
	}

	@Test
	public void testCompareToRootUnboundedBeforeWildcard() {
		assertCompareBefore("/**", "/*");
	}

	@Test
	public void testCompareToPathDeeperWildcardsBeforeWildcard() {
		assertCompareAfter(
			"/path/*/*",
			"/path/*/*/*",
			"/path/*/*/**",
			"/path/*/*/***"
		);
	}

	@Test
	public void testCompareToPathDeeperWildcardsBeforeUnbounded() {
		assertCompareAfter(
			"/path/*/**",
			"/path/*/*/*",
			"/path/*/*/**",
			"/path/*/*/***"
		);
	}

	@Test
	public void testCompareToPathDeeperWildcardsBeforeGreedy() {
		assertCompareAfter(
			"/path/*/***",
			"/path/*/*/*",
			"/path/*/*/**",
			"/path/*/*/***"
		);
	}

	@Test
	public void testCompareToPathDeeperPathWildcardsBeforeWildcard() {
		assertCompareAfter(
			"/path/*/*",
			"/path/other/*/*",
			"/path/other/*/**",
			"/path/other/*/***"
		);
	}

	@Test
	public void testCompareToPathDeeperPathWildcardsBeforeUnbounded() {
		assertCompareAfter(
			"/path/*/**",
			"/path/other/*/*",
			"/path/other/*/**",
			"/path/other/*/***"
		);
	}

	@Test
	public void testCompareToPathDeeperPathWildcardsBeforeGreedy() {
		assertCompareAfter(
			"/path/*/***",
			"/path/other/*/*",
			"/path/other/*/**",
			"/path/other/*/***"
		);
	}

	@Test
	public void testCompareToMuchDeeperPathsWildcard() {
		assertCompareBefore(
			"/z/z/z/z/z/*",
			"/**",
			"/a/**",
			"/z/**",
			"/a/a/**",
			"/z/z/**",
			"/a/a/a/**",
			"/z/z/z/**",
			"/a/a/a/a/**",
			"/z/z/z/z/**"
		);
		assertCompareAfter(
			"/z/z/z/z/z/*",
			// TODO: This conflicts, should the conflict throw and exception?
			"/a/a/a/a/a/**",
			// TODO: This conflicts, should the conflict throw and exception?
			"/z/z/z/z/z/**",
			"/a/a/a/a/a/a/**",
			"/z/z/z/z/z/z/**"
		);
	}

	@Test
	public void testCompareToMuchDeeperWildcardsOnly() {
		assertCompareBefore(
			"/z/z/z/z/z/*",
			"/**",
			"/*/**",
			"/*/*/**",
			"/*/*/*/**",
			"/*/*/*/*/**"
		);
		assertCompareAfter(
			"/z/z/z/z/z/*",
			// TODO: This conflicts, should the conflict throw and exception?
			"/*/*/*/*/*/**",
			"/*/*/*/*/*/*/**"
		);
	}

	@Test
	public void testCompareToMuchDeeperPathWildcards() {
		assertCompareBefore(
			"/z/z/z/z/z/*",
			"/**",
			"/a/**",
			"/z/**",
			"/a/*/**",
			"/z/*/**",
			"/a/*/*/**",
			"/z/*/*/**",
			"/a/*/*/*/**",
			"/z/*/*/*/**"
		);
		assertCompareAfter(
			"/z/z/z/z/z/*",
			// TODO: This conflicts, should the conflict throw and exception?
			"/a/*/*/*/*/**",
			// TODO: This conflicts, should the conflict throw and exception?
			"/z/*/*/*/*/**",
			"/a/*/*/*/*/*/**",
			"/z/*/*/*/*/*/**"
		);
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Test conflictsWith">
	/**
	 * Tests that all others conflict with the first
	 */
	private static void testConflicts(Prefix p0, Prefix ... others) {
		assertTrue("p0 must conflict with self: " + p0, p0.conflictsWith(p0));
		for(Prefix other : others) {
			assertTrue("other must conflict with self: " + other, other.conflictsWith(other));
			assertTrue("must conflict: p0 = " + p0 + ", other = " + other, p0.conflictsWith(other));
			assertTrue("must conflict: other = " + other + ", p0 = " + p0, other.conflictsWith(p0));
		}
	}

	/**
	 * Tests that all others do not conflict with the first
	 */
	private static void testNotConflicts(Prefix p0, Prefix ... others) {
		assertTrue("p0 must conflict with self: " + p0, p0.conflictsWith(p0));
		for(Prefix other : others) {
			assertTrue("other must conflict with self: " + other, other.conflictsWith(other));
			assertFalse("must not conflict: p0 = " + p0 + ", other = " + other, p0.conflictsWith(other));
			assertFalse("must not conflict: other = " + other + ", p0 = " + p0, other.conflictsWith(p0));
		}
	}

	@Test
	public void testRootWildcardConflicts() {
		testConflicts(
			valueOf("/*"),
			valueOf("/*"),
			valueOf("/**"),
			valueOf("/***")
		);
	}

	@Test
	public void testRootWildcardNotConflicts() {
		testNotConflicts(
			valueOf("/*"),
			valueOf("/*/*"),
			valueOf("/*/**"),
			valueOf("/*/***"),
			valueOf("/path/*"),
			valueOf("/path/**"),
			valueOf("/path/***")
		);
	}

	@Test
	public void testRootUnboundedConflicts() {
		testConflicts(
			valueOf("/**"),
			valueOf("/*"),
			valueOf("/**"),
			valueOf("/***")
		);
	}

	@Test
	public void testRootUnboundedNotConflicts() {
		testNotConflicts(
			valueOf("/**"),
			valueOf("/*/*"),
			valueOf("/*/**"),
			valueOf("/*/***"),
			valueOf("/path/*"),
			valueOf("/path/**"),
			valueOf("/path/***")
		);
	}

	@Test
	public void testRootGreedyConflicts() {
		testConflicts(
			valueOf("/***"),
			valueOf("/*"),
			valueOf("/**"),
			valueOf("/***"),
			valueOf("/*/*"),
			valueOf("/*/**"),
			valueOf("/*/***"),
			valueOf("/path/*"),
			valueOf("/path/**"),
			valueOf("/path/***")
		);
	}

	@Test
	public void testRootGreedyNotConflicts() {
		testNotConflicts(
			valueOf("/***")
			// All conflict, nothing to do
		);
	}

	@Test
	public void testPathWildcardConflicts() {
		testConflicts(
			valueOf("/path/*"),
			valueOf("/***"),
			valueOf("/*/*"),
			valueOf("/*/**"),
			valueOf("/*/***"),
			valueOf("/path/*"),
			valueOf("/path/**"),
			valueOf("/path/***")
		);
	}

	@Test
	public void testPathWildcardNotConflicts() {
		testNotConflicts(
			valueOf("/path/*"),
			valueOf("/pathy/*"),
			valueOf("/pathy/**"),
			valueOf("/pathy/***"),
			valueOf("/*"),
			valueOf("/**"),
			valueOf("/*/*/*"),
			valueOf("/*/*/**"),
			valueOf("/*/*/***"),
			valueOf("/path/*/*"),
			valueOf("/path/*/**"),
			valueOf("/path/*/***"),
			valueOf("/path/other/*"),
			valueOf("/path/other/**"),
			valueOf("/path/other/***")
		);
	}

	@Test
	public void testPathUnboundedConflicts() {
		testConflicts(
			valueOf("/path/**"),
			valueOf("/***"),
			valueOf("/*/*"),
			valueOf("/*/**"),
			valueOf("/*/***"),
			valueOf("/path/*"),
			valueOf("/path/**"),
			valueOf("/path/***")
		);
	}

	@Test
	public void testPathUnboundedNotConflicts() {
		testNotConflicts(
			valueOf("/path/**"),
			valueOf("/pathy/*"),
			valueOf("/pathy/**"),
			valueOf("/pathy/***"),
			valueOf("/*"),
			valueOf("/**"),
			valueOf("/*/*/*"),
			valueOf("/*/*/**"),
			valueOf("/*/*/***"),
			valueOf("/path/*/*"),
			valueOf("/path/*/**"),
			valueOf("/path/*/***"),
			valueOf("/path/other/*"),
			valueOf("/path/other/**"),
			valueOf("/path/other/***")
		);
	}

	@Test
	public void testPathGreedyConflicts() {
		testConflicts(
			valueOf("/path/***"),
			valueOf("/***"),
			valueOf("/*/*"),
			valueOf("/*/**"),
			valueOf("/*/***"),
			valueOf("/*/*/*"),
			valueOf("/*/*/**"),
			valueOf("/*/*/***"),
			valueOf("/path/*"),
			valueOf("/path/**"),
			valueOf("/path/***"),
			valueOf("/path/*/*"),
			valueOf("/path/*/**"),
			valueOf("/path/*/***"),
			valueOf("/path/other/*"),
			valueOf("/path/other/**"),
			valueOf("/path/other/***")
		);
	}

	@Test
	public void testPathGreedyNotConflicts() {
		testNotConflicts(
			valueOf("/path/***"),
			valueOf("/pathy/*"),
			valueOf("/pathy/**"),
			valueOf("/pathy/***"),
			valueOf("/*"),
			valueOf("/**")
		);
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Test matches">
	/**
	 * Tests that all paths match
	 * <p>
	 * <b>Implementation Note:</b><br />
	 * TODO: Add test of match length
	 * </p>
	 */
	private static void testMatches(Prefix prefix, String ... paths) throws ValidationException {
		for(String path : paths) {
			assertNotEquals(
				"must match: prefix = " + prefix + ", path = " + path,
				-1,
				prefix.matches(Path.valueOf(path))
			);
		}
	}

	/**
	 * Tests that all paths do not match
	 */
	private static void testNotMatches(Prefix prefix, String ... paths) throws ValidationException {
		for(String path : paths) {
			assertEquals(
				"must not match: prefix = " + prefix + ", path = " + path,
				-1,
				prefix.matches(Path.valueOf(path))
			);
		}
	}

	// <editor-fold defaultstate="collapsed" desc="Test matches with .../*...">

	@Test
	public void testRootWildcardMatches() throws ValidationException {
		testMatches(
			valueOf("/*"),
			"/",
			"/path"
		);
	}

	@Test
	public void testRootWildcardNotMatches() throws ValidationException {
		testNotMatches(
			valueOf("/*"),
			"/path/",
			"/path/other",
			"/path/other/"
		);
	}

	@Test
	public void testRootUnboundedMatches() throws ValidationException {
		testMatches(
			valueOf("/**"),
			"/",
			"/path",
			"/path/other",
			"/path/other/",
			"/path/other/more",
			"/path/other/more/"
		);
	}

	@Test
	public void testRootUnboundedNotMatches() throws ValidationException {
		testNotMatches(
			valueOf("/**")
			// All should match, nothing to do
		);
	}

	@Test
	public void testRootGreedyMatches() throws ValidationException {
		testMatches(
			valueOf("/***"),
			"/",
			"/path",
			"/path/",
			"/path/other",
			"/path/other/",
			"/path/other/more",
			"/path/other/more/"
		);
	}

	@Test
	public void testRootGreedyNotMatches() throws ValidationException {
		testNotMatches(
			valueOf("/***")
			// All should match, nothing to do
		);
	}

	@Test
	public void testPathWildcardMatches() throws ValidationException {
		testMatches(
			valueOf("/path/*"),
			"/path/",
			"/path/other"
		);
	}

	@Test
	public void testPathWildcardNotMatches() throws ValidationException {
		testNotMatches(
			valueOf("/path/*"),
			"/",
			"/path",
			"/path/other/",
			"/path/other/more",
			"/path/other/more/",
			"/pathy",
			"/pathy/"
		);
	}

	@Test
	public void testPathUnboundedMatches() throws ValidationException {
		testMatches(
			valueOf("/path/**"),
			"/path/",
			"/path/other",
			"/path/other/",
			"/path/other/more",
			"/path/other/more/"
		);
	}

	@Test
	public void testPathUnboundedNotMatches() throws ValidationException {
		testNotMatches(
			valueOf("/path/**"),
			"/",
			"/path",
			"/pathy",
			"/pathy/"
		);
	}

	@Test
	public void testPathGreedyMatches() throws ValidationException {
		testMatches(
			valueOf("/path/***"),
			"/path/",
			"/path/other",
			"/path/other/",
			"/path/other/more",
			"/path/other/more/"
		);
	}

	@Test
	public void testPathGreedyNotMatches() throws ValidationException {
		testNotMatches(
			valueOf("/path/***"),
			"/",
			"/path",
			"/pathy",
			"/pathy/"
		);
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Test matches with .../*/*...">
	@Test
	public void testRootWildcardWildcardMatches() throws ValidationException {
		testMatches(
			valueOf("/*/*"),
			"/path/",
			"/path/other"
		);
	}

	@Test
	public void testRootWildcardWildcardNotMatches() throws ValidationException {
		testNotMatches(
			valueOf("/*/*"),
			"/",
			"/path",
			"/path/other/",
			"/path/other/more",
			"/path/other/more/"
		);
	}

	@Test
	public void testRootWildcardUnboundedMatches() throws ValidationException {
		testMatches(
			valueOf("/*/**"),
			"/path/",
			"/path/other",
			"/path/other/",
			"/path/other/more",
			"/path/other/more/"
		);
	}

	@Test
	public void testRootWildcardUnboundedNotMatches() throws ValidationException {
		testNotMatches(
			valueOf("/*/**"),
			"/",
			"/path"
		);
	}

	@Test
	public void testRootWildcardGreedyMatches() throws ValidationException {
		testMatches(
			valueOf("/*/***"),
			"/path/",
			"/path/other",
			"/path/other/",
			"/path/other/more",
			"/path/other/more/"
		);
	}

	@Test
	public void testRootWildcardGreedyNotMatches() throws ValidationException {
		testNotMatches(
			valueOf("/*/***"),
			"/",
			"/path"
		);
	}

	@Test
	public void testPathWildcardWildcardMatches() throws ValidationException {
		testMatches(
			valueOf("/path/*/*"),
			"/path/other/",
			"/path/other/more"
		);
	}

	@Test
	public void testPathWildcardWildcardNotMatches() throws ValidationException {
		testNotMatches(
			valueOf("/path/*/*"),
			"/",
			"/path",
			"/path/",
			"/path/other",
			"/path/other/more/",
			"/path/other/more/fruit",
			"/path/other/more/fruit/"
		);
	}

	@Test
	public void testPathWildcardUnboundedMatches() throws ValidationException {
		testMatches(
			valueOf("/path/*/**"),
			"/path/other/",
			"/path/other/more",
			"/path/other/more/",
			"/path/other/more/fruit",
			"/path/other/more/fruit/"
		);
	}

	@Test
	public void testPathWildcardUnboundedNotMatches() throws ValidationException {
		testNotMatches(
			valueOf("/path/*/**"),
			"/",
			"/path",
			"/path/",
			"/path/other"
		);
	}

	@Test
	public void testPathWildcardGreedyMatches() throws ValidationException {
		testMatches(
			valueOf("/path/*/***"),
			"/path/other/",
			"/path/other/more",
			"/path/other/more/",
			"/path/other/more/fruit",
			"/path/other/more/fruit/"
		);
	}

	@Test
	public void testPathWildcardGreedyNotMatches() throws ValidationException {
		testNotMatches(
			valueOf("/path/*/***"),
			"/",
			"/path",
			"/path/",
			"/path/other"
		);
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Test matches with deeper nested /*/*/*...">
	@Test
	public void testRootWildcard3() throws ValidationException {
		testMatches(
			valueOf("/*/*/*"),
			"/path/other/",
			"/path/other/more"
		);
	}

	@Test
	public void testRootWildcard3NotMatches() throws ValidationException {
		testNotMatches(
			valueOf("/*/*/*"),
			"/",
			"/path",
			"/path/",
			"/path/other",
			"/path/other/more/",
			"/path/other/more/fruit",
			"/path/other/more/fruit/"
		);
	}

	@Test
	public void testRootWildcard4() throws ValidationException {
		testMatches(
			valueOf("/*/*/*/*"),
			"/path/other/more/",
			"/path/other/more/fruit"
		);
	}

	@Test
	public void testRootWildcard4NotMatches() throws ValidationException {
		testNotMatches(
			valueOf("/*/*/*/*"),
			"/",
			"/path",
			"/path/",
			"/path/other",
			"/path/other/",
			"/path/other/more",
			"/path/other/more/fruit/",
			"/path/other/more/fruit/loops",
			"/path/other/more/fruit/loops/"
		);
	}
	// </editor-fold>

	// </editor-fold>
}

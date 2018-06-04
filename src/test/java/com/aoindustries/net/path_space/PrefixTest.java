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

import com.aoindustries.net.Path;
import org.junit.Test;
import static com.aoindustries.net.path_space.Prefix.*;
import com.aoindustries.validation.ValidationException;
import static org.junit.Assert.*;

/**
 * @see Prefix
 *
 * @author  AO Industries, Inc.
 */
public class PrefixTest {
	
	public PrefixTest() {
	}

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

	@Test
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

	@Test
	public void testValueOfPath() throws ValidationException {
		testValueOf(
			"/path", 0, Prefix.MultiLevelType.NONE,
			"/path"
		);
	}

	@Test
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
}

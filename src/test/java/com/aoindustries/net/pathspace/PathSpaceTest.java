/*
 * ao-net-path-space - Manages allocation of a path space between components.
 * Copyright (C) 2018, 2019  AO Industries, Inc.
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
import com.aoindustries.validation.ValidationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * @see PathSpace
 *
 * @author  AO Industries, Inc.
 */
public class PathSpaceTest {

	private static PathSpace<Void> newTestSpace(String... prefixes) {
		// Add in a random order
		List<String> prefixList = new ArrayList<>(Arrays.asList(prefixes));
		Collections.shuffle(prefixList);
		PathSpace<Void> testSpace = new PathSpace<>();
		for(String prefix : prefixList) {
			testSpace.put(Prefix.valueOf(prefix), null);
		}
		return testSpace;
	}

	@Test(expected = PrefixConflictException.class)
	public void testPrefixConflicts1() {
		newTestSpace(
			"/*",
			"/***"
		);
	}

	@Test(expected = PrefixConflictException.class)
	public void testPrefixConflicts2() {
		newTestSpace(
			"/*/*",
			"/path/other/*",
			"/other/path/*",
			"/path/*"
		);
	}

	private static <V> void assertEquals(PathMatch<V> expected, PathMatch<V> actual) {
		Assert.assertEquals("prefix", expected.getPrefix(), actual.getPrefix());
		Assert.assertEquals("prefixPath", expected.getPrefixPath(), actual.getPrefixPath());
		Assert.assertEquals("subPath", expected.getPath(), actual.getPath());
		Assert.assertSame("value", expected.getValue(), actual.getValue());
		Assert.assertEquals(expected, actual);
	}

	private static <V> PathMatch<V> newPathMatch(String prefix, String prefixPath, String subPath, V value) throws ValidationException {
		return new PathMatch<>(
			Prefix.valueOf(prefix),
			Path.valueOf(prefixPath),
			Path.valueOf(subPath),
			value
		);
	}

	@Test
	public void testGetWithRoot() throws ValidationException {
		PathSpace<Void> testSpace = newTestSpace(
			"/**",
			"/path/*",
			"/path/other/*",
			"/other/path/***",
			"/bicycle/*/***",
			"/deeper/*/*/*",
			"/deeper/1/2/3/4/5/6/**", // Just trying to get in the way
			"/deeper/1/2/3/4/5/6/7/*" // Just trying to get in the way
		);
		assertEquals(
			newPathMatch("/**", "/", "/", (Void)null),
			testSpace.get(Path.ROOT)
		);
		assertEquals(
			newPathMatch("/**", "/", "/path", (Void)null),
			testSpace.get(Path.valueOf("/path"))
		);
		assertEquals(
			newPathMatch("/path/*", "/path", "/", (Void)null),
			testSpace.get(Path.valueOf("/path/"))
		);
		assertEquals(
			newPathMatch("/path/*", "/path", "/other", (Void)null),
			testSpace.get(Path.valueOf("/path/other"))
		);
		assertEquals(
			newPathMatch("/path/other/*", "/path/other", "/", (Void)null),
			testSpace.get(Path.valueOf("/path/other/"))
		);
		assertEquals(
			newPathMatch("/path/other/*", "/path/other", "/apple", (Void)null),
			testSpace.get(Path.valueOf("/path/other/apple"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/path/other/apple/", (Void)null),
			testSpace.get(Path.valueOf("/path/other/apple/"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/path/other/apple/pie", (Void)null),
			testSpace.get(Path.valueOf("/path/other/apple/pie"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/path/other/apple/pie/", (Void)null),
			testSpace.get(Path.valueOf("/path/other/apple/pie/"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/other", (Void)null),
			testSpace.get(Path.valueOf("/other"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/other/", (Void)null),
			testSpace.get(Path.valueOf("/other/"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/other/path", (Void)null),
			testSpace.get(Path.valueOf("/other/path"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/", (Void)null),
			testSpace.get(Path.valueOf("/other/path/"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/banana", (Void)null),
			testSpace.get(Path.valueOf("/other/path/banana"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/banana/", (Void)null),
			testSpace.get(Path.valueOf("/other/path/banana/"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/banana/bread", (Void)null),
			testSpace.get(Path.valueOf("/other/path/banana/bread"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/banana/bread/other/path/banana/bread/other/path/banana/bread/other/path/banana/bread/", (Void)null),
			testSpace.get(Path.valueOf("/other/path/banana/bread/other/path/banana/bread/other/path/banana/bread/other/path/banana/bread/"))
		);
		assertEquals(
			newPathMatch("/bicycle/*/***", "/bicycle/path", "/", (Void)null),
			testSpace.get(Path.valueOf("/bicycle/path/"))
		);
		assertEquals(
			newPathMatch("/bicycle/*/***", "/bicycle/path", "/banana", (Void)null),
			testSpace.get(Path.valueOf("/bicycle/path/banana"))
		);
		assertEquals(
			newPathMatch("/bicycle/*/***", "/bicycle/path", "/banana/", (Void)null),
			testSpace.get(Path.valueOf("/bicycle/path/banana/"))
		);
		assertEquals(
			newPathMatch("/bicycle/*/***", "/bicycle/path", "/banana/bread", (Void)null),
			testSpace.get(Path.valueOf("/bicycle/path/banana/bread"))
		);
		assertEquals(
			newPathMatch("/bicycle/*/***", "/bicycle/path", "/banana/bread/bicycle/path/banana/bread/bicycle/path/banana/bread/bicycle/path/banana/bread/", (Void)null),
			testSpace.get(Path.valueOf("/bicycle/path/banana/bread/bicycle/path/banana/bread/bicycle/path/banana/bread/bicycle/path/banana/bread/"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/deeper", (Void)null),
			testSpace.get(Path.valueOf("/deeper"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/deeper/", (Void)null),
			testSpace.get(Path.valueOf("/deeper/"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/deeper/1", (Void)null),
			testSpace.get(Path.valueOf("/deeper/1"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/deeper/1/", (Void)null),
			testSpace.get(Path.valueOf("/deeper/1/"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/deeper/1/2", (Void)null),
			testSpace.get(Path.valueOf("/deeper/1/2"))
		);
		assertEquals(
			newPathMatch("/deeper/*/*/*", "/deeper/1/2", "/", (Void)null),
			testSpace.get(Path.valueOf("/deeper/1/2/"))
		);
		assertEquals(
			newPathMatch("/deeper/*/*/*", "/deeper/1/2", "/3", (Void)null),
			testSpace.get(Path.valueOf("/deeper/1/2/3"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/deeper/1/2/3/", (Void)null),
			testSpace.get(Path.valueOf("/deeper/1/2/3/"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/deeper/1/2/3/4", (Void)null),
			testSpace.get(Path.valueOf("/deeper/1/2/3/4"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/deeper/1/2/3/4/", (Void)null),
			testSpace.get(Path.valueOf("/deeper/1/2/3/4/"))
		);
		assertEquals(
			newPathMatch("/**", "/", "/deeper/1/2/3/4/5", (Void)null),
			testSpace.get(Path.valueOf("/deeper/1/2/3/4/5"))
		);
	}


	@Test
	public void testGetWithoutRoot() throws ValidationException {
		PathSpace<Void> testSpace = newTestSpace(
			"/path/*",
			"/path/other/*",
			"/other/path/***",
			"/bicycle/*/***",
			"/deeper/*/*/*",
			"/deeper/1/2/3/4/5/6/**", // Just trying to get in the way
			"/deeper/1/2/3/4/5/6/7/*" // Just trying to get in the way
		);
		assertNull(
			testSpace.get(Path.ROOT)
		);
		assertNull(
			testSpace.get(Path.valueOf("/path"))
		);
		assertEquals(
			newPathMatch("/path/*", "/path", "/", (Void)null),
			testSpace.get(Path.valueOf("/path/"))
		);
		assertEquals(
			newPathMatch("/path/*", "/path", "/other", (Void)null),
			testSpace.get(Path.valueOf("/path/other"))
		);
		assertEquals(
			newPathMatch("/path/other/*", "/path/other", "/", (Void)null),
			testSpace.get(Path.valueOf("/path/other/"))
		);
		assertEquals(
			newPathMatch("/path/other/*", "/path/other", "/apple", (Void)null),
			testSpace.get(Path.valueOf("/path/other/apple"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/path/other/apple/"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/path/other/apple/pie"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/path/other/apple/pie/"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/other"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/other/"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/other/path"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/", (Void)null),
			testSpace.get(Path.valueOf("/other/path/"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/banana", (Void)null),
			testSpace.get(Path.valueOf("/other/path/banana"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/banana/", (Void)null),
			testSpace.get(Path.valueOf("/other/path/banana/"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/banana/bread", (Void)null),
			testSpace.get(Path.valueOf("/other/path/banana/bread"))
		);
		assertEquals(
			newPathMatch("/other/path/***", "/other/path", "/banana/bread/other/path/banana/bread/other/path/banana/bread/other/path/banana/bread/", (Void)null),
			testSpace.get(Path.valueOf("/other/path/banana/bread/other/path/banana/bread/other/path/banana/bread/other/path/banana/bread/"))
		);
		assertEquals(
			newPathMatch("/bicycle/*/***", "/bicycle/path", "/", (Void)null),
			testSpace.get(Path.valueOf("/bicycle/path/"))
		);
		assertEquals(
			newPathMatch("/bicycle/*/***", "/bicycle/path", "/banana", (Void)null),
			testSpace.get(Path.valueOf("/bicycle/path/banana"))
		);
		assertEquals(
			newPathMatch("/bicycle/*/***", "/bicycle/path", "/banana/", (Void)null),
			testSpace.get(Path.valueOf("/bicycle/path/banana/"))
		);
		assertEquals(
			newPathMatch("/bicycle/*/***", "/bicycle/path", "/banana/bread", (Void)null),
			testSpace.get(Path.valueOf("/bicycle/path/banana/bread"))
		);
		assertEquals(
			newPathMatch("/bicycle/*/***", "/bicycle/path", "/banana/bread/bicycle/path/banana/bread/bicycle/path/banana/bread/bicycle/path/banana/bread/", (Void)null),
			testSpace.get(Path.valueOf("/bicycle/path/banana/bread/bicycle/path/banana/bread/bicycle/path/banana/bread/bicycle/path/banana/bread/"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/deeper"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/deeper/"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/deeper/1"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/deeper/1/"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/deeper/1/2"))
		);
		assertEquals(
			newPathMatch("/deeper/*/*/*", "/deeper/1/2", "/", (Void)null),
			testSpace.get(Path.valueOf("/deeper/1/2/"))
		);
		assertEquals(
			newPathMatch("/deeper/*/*/*", "/deeper/1/2", "/3", (Void)null),
			testSpace.get(Path.valueOf("/deeper/1/2/3"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/deeper/1/2/3/"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/deeper/1/2/3/4"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/deeper/1/2/3/4/"))
		);
		assertNull(
			testSpace.get(Path.valueOf("/deeper/1/2/3/4/5"))
		);
	}
}

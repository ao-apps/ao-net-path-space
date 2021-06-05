/*
 * ao-net-path-space - Manages allocation of a path space between components.
 * Copyright (C) 2018, 2019, 2021  AO Industries, Inc.
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
package com.aoapps.net.pathspace;

import com.aoapps.net.Path;
import java.util.Objects;

/**
 * The result of a call to {@link PathSpace#get(com.aoapps.net.Path)}.
 */
public class PathMatch<V> {

	private final Prefix prefix;
	private final Path prefixPath;
	private final Path path;
	private final V value;

	PathMatch(
		Prefix prefix,
		Path prefixPath,
		Path path,
		V value
	) {
		this.prefix = prefix;
		this.prefixPath = prefixPath;
		this.path = path;
		this.value = value;
	}

	@Override
	public String toString() {
		return prefixPath.toString() + '!' + path.toString();
	}

	/**
	 * Two matches are equal when they have the same prefix (by .equals),
	 * prefixPath (by .equals), subPath (by .equals), and value (by identity).
	 */
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof PathMatch<?>)) return false;
		PathMatch<?> other = (PathMatch<?>)o;
		return
			value == other.value
			&& prefix.equals(other.prefix)
			&& prefixPath.equals(other.prefixPath)
			&& path.equals(other.path)
		;
	}

	@Override
	public int hashCode() {
		int hash = prefix.hashCode();
		hash = hash * 31 + prefixPath.hashCode();
		hash = hash * 31 + path.hashCode();
		hash = hash * 31 + Objects.hashCode(value);
		return hash;
	}

	/**
	 * Gets the prefix that matched the lookup.
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
	public Path getPath() {
		return path;
	}

	/**
	 * Gets the value associated with the prefix.
	 */
	public V getValue() {
		return value;
	}
}

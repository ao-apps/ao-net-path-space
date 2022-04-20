/*
 * ao-net-path-space - Manages allocation of a path space between components.
 * Copyright (C) 2018, 2020, 2021, 2022  AO Industries, Inc.
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

import com.aoapps.lang.Throwables;

/**
 * Exception thrown when conflicting prefixes are detected.
 *
 * @see  PathSpace#put(com.aoapps.net.pathspace.Prefix, java.lang.Object)
 *
 * @author  AO Industries, Inc.
 */
public class PrefixConflictException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final Prefix existing;
  private final Prefix adding;

  PrefixConflictException(Prefix existing, Prefix adding) {
    this.existing = existing;
    this.adding = adding;
  }

  PrefixConflictException(Prefix existing, Prefix adding, Throwable cause) {
    super(cause);
    this.existing = existing;
    this.adding = adding;
  }

  @Override
  public String getMessage() {
    return "Prefix \"" + adding + "\" conflicts with existing prefix \"" + existing + '"';
  }

  public Prefix getExisting() {
    return existing;
  }

  public Prefix getAdding() {
    return adding;
  }

  static {
    Throwables.registerSurrogateFactory(PrefixConflictException.class, (template, cause) ->
      new PrefixConflictException(template.existing, template.adding, cause)
    );
  }
}

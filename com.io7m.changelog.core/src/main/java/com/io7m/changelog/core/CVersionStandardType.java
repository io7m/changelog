/*
 * Copyright © 2016 <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.changelog.core;

import org.immutables.value.Value;

/**
 * <p> The type of standard version numbers of the form {@code M.N.P-S}, where
 * {@code M} is the major version, {@code N} is the minor version, {@code P} is
 * the patch number, and {@code S} is an arbitrary qualifier string. </p>
 */

@CImmutableStyleType
@Value.Immutable
public interface CVersionStandardType extends CVersionType
{
  @Override
  default <A> A versionAccept(final CVersionVisitorType<A> v)
    throws Exception
  {
    return v.standard(this);
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    if (this.major() < 0) {
      throw new IllegalArgumentException(
        "Major version must be positive (received" + this.major() + ")");
    }
    if (this.minor() < 0) {
      throw new IllegalArgumentException(
        "Minor version must be positive (received" + this.minor() + ")");
    }
    if (this.patch() < 0) {
      throw new IllegalArgumentException(
        "Patch version must be positive (received" + this.patch() + ")");
    }
  }

  @Override
  default int compareTo(
    final CVersionType o)
  {
    try {
      return o.versionAccept(new CVersionStandardComparator(this)).intValue();
    } catch (final Exception e) {
      throw new AssertionError(e);
    }
  }

  /**
   * @return The major version number
   */

  @Value.Parameter
  int major();

  /**
   * @return The minor version number
   */

  @Value.Parameter
  int minor();

  /**
   * @return The patch number
   */

  @Value.Parameter
  int patch();

  /**
   * @return The qualifier component of the version number
   */

  @Value.Parameter
  String qualifier();

  @Override
  default String toVersionString()
  {
    final StringBuilder builder = new StringBuilder(64);
    builder.append(this.major());
    builder.append('.');
    builder.append(this.minor());
    builder.append('.');
    builder.append(this.patch());
    if (!this.qualifier().isEmpty()) {
      builder.append('-');
      builder.append(this.qualifier());
    }
    return builder.toString();
  }

}

/*
 * Copyright © 2018 <code@io7m.com> http://io7m.com
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

import java.util.Objects;

final class CVersionStandardComparator implements CVersionVisitorType<Integer>
{
  private final CVersionStandardType current;

  CVersionStandardComparator(
    final CVersionStandardType in_current)
  {
    this.current = Objects.requireNonNull(in_current, "Current");
  }

  @SuppressWarnings({"synthetic-access", "boxing"})
  @Override
  public Integer
  standard(
    final CVersionStandardType other)
  {
    final int r_major = Integer.compare(this.current.major(), other.major());
    if (r_major != 0) {
      return r_major;
    }

    final int r_minor = Integer.compare(this.current.minor(), other.minor());
    if (r_minor != 0) {
      return r_minor;
    }

    final int r_patch = Integer.compare(this.current.patch(), other.patch());
    if (r_patch != 0) {
      return r_patch;
    }

    return this.compareQualifiers(other);
  }

  private Integer compareQualifiers(
    final CVersionStandardType other)
  {
    /*
     * Comparing qualifiers works slightly strangely: An empty qualifier is
     * considered to be "less than" a non-empty qualifier. This is because, for
     * example, version 1.0.0-SNAPSHOT is less than version 1.0.0. Two non-empty
     * qualifiers are simply compared lexicographically.
     */

    final String c_qual = this.current.qualifier();
    final String o_qual = other.qualifier();
    if (c_qual.isEmpty() && !o_qual.isEmpty()) {
      return 1;
    }
    if (!c_qual.isEmpty() && o_qual.isEmpty()) {
      return -1;
    }

    return c_qual.compareTo(o_qual);
  }

  @SuppressWarnings("boxing")
  @Override
  public Integer text(
    final CVersionTextType s)
  {
    return this.current.toString().compareTo(s.text());
  }
}

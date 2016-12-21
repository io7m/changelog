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

import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;

/**
 * Functions for parsing and processing versions.
 */

public final class CVersions
{
  private CVersions()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Attempt to parse a version number.
   *
   * @param version The version string
   *
   * @return A new version number
   */

  public static CVersionType parse(
    final String version)
  {
    NullCheck.notNull(version, "Version");

    String ptr = version;
    int dot = ptr.indexOf('.');
    if (dot == -1) {
      return CVersionText.of(version);
    }

    final Integer mj = Integer.valueOf(ptr.substring(0, dot));
    ptr = version.substring(dot + 1);

    dot = ptr.indexOf('.');
    if (dot == -1) {
      return CVersionText.of(version);
    }

    final Integer mn = Integer.valueOf(ptr.substring(0, dot));
    ptr = ptr.substring(dot + 1);

    if (ptr.matches("[0-9]+")) {
      return CVersionStandard.builder()
        .setMajor(mj.intValue())
        .setMinor(mn.intValue())
        .setPatch(Integer.valueOf(ptr).intValue())
        .setQualifier("")
        .build();
    }

    dot = ptr.indexOf('-');
    if (dot == -1) {
      return CVersionText.of(version);
    }

    final Integer mp = Integer.valueOf(ptr.substring(0, dot));
    ptr = ptr.substring(dot + 1);

    return CVersionStandard.builder()
      .setMajor(mj.intValue())
      .setMinor(mn.intValue())
      .setPatch(mp.intValue())
      .setQualifier(ptr)
      .build();
  }
}

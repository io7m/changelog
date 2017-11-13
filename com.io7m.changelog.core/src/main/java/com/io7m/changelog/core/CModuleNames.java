/*
 * Copyright © 2017 <code@io7m.com> http://io7m.com
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

import com.io7m.junreachable.UnreachableCodeException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Functions over module names.
 */

public final class CModuleNames
{
  /**
   * The maximum length of a name.
   */

  public static final int VALID_NAME_LENGTH;

  /**
   * The pattern that defines a valid name, not including the length of the
   * name.
   */

  public static final Pattern VALID_NAMES;

  static {
    VALID_NAME_LENGTH = 128;
    VALID_NAMES = Pattern.compile(
      "([\\p{Ll}][\\p{Ll}\\p{Nd}_]*)(\\.[\\p{Ll}][\\p{Ll}\\p{Nd}_]*)*");
  }

  private CModuleNames()
  {
    throw new UnreachableCodeException();
  }

  /**
   * @param name The name
   *
   * @return {@code true} iff the module name is valid
   */

  public static boolean isValid(
    final String name)
  {
    Objects.requireNonNull(name, "Name");
    return VALID_NAMES.matcher(name).matches() && name.length() <= VALID_NAME_LENGTH;
  }
}

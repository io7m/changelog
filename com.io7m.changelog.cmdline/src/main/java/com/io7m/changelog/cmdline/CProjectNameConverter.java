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

package com.io7m.changelog.cmdline;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import com.io7m.changelog.core.CProjectName;
import com.io7m.changelog.core.CProjectNames;

import java.util.Objects;

final class CProjectNameConverter implements IStringConverter<CProjectName>
{
  CProjectNameConverter()
  {

  }

  @Override
  public CProjectName convert(
    final String value)
  {
    Objects.requireNonNull(value, "Value");

    if (!CProjectNames.isValid(value)) {
      throw new ParameterException(
        new StringBuilder(128)
          .append("Project name is not valid.")
          .append(System.lineSeparator())
          .append("  Received: ")
          .append(value)
          .append(System.lineSeparator())
          .append("  Expected: ")
          .append(CProjectNames.VALID_NAMES.pattern())
          .append(" <= ")
          .append(CProjectNames.VALID_NAME_LENGTH)
          .append(" characters")
          .append(System.lineSeparator())
          .toString());
    }

    return CProjectName.of(value);
  }
}

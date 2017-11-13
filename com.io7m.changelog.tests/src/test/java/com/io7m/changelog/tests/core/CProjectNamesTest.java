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

package com.io7m.changelog.tests.core;

import com.io7m.changelog.core.CProjectName;
import com.io7m.jaffirm.core.PreconditionViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public final class CProjectNamesTest
{
  @Test
  public void testProjectNamesValid()
  {
    Assertions.assertAll(
      () -> CProjectName.of("a"),
      () -> CProjectName.of("a.b"),
      () -> CProjectName.of("a1.b2_.c"),
      () -> CProjectName.of("A")
    );
  }

  @Test
  public void testProjectNamesInvalid()
  {
    Assertions.assertAll(
      () -> Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> CProjectName.of("1")),
      () -> Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> CProjectName.of("$")),
      () -> Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> CProjectName.of("1.2")),
      () -> Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> CProjectName.of(
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
    );
  }
}

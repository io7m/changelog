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

import com.io7m.changelog.core.CTicketID;
import com.io7m.jaffirm.core.PreconditionViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class CTicketIDsTest
{
  @Test
  public void testTicketIDNamesValid()
  {
    Assertions.assertAll(
      () -> CTicketID.of("a"),
      () -> CTicketID.of("a1"),
      () -> CTicketID.of("1"),
      () -> CTicketID.of(
        "5891b5b522d5df086d0ff0b110fbd9d21bb4fc7163af34d08286a2e846f6be03"),
      () -> CTicketID.of("SIMPLY-1870")
    );
  }

  @Test
  public void testTicketIDNamesInvalid()
  {
    Assertions.assertAll(
      () -> Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> CTicketID.of("a.b")),

      () -> Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> CTicketID.of("a1.b2_.c")),

      () -> Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> CTicketID.of("A.B")),

      () -> Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> CTicketID.of("1.2")),

      () -> Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> CTicketID.of(
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
    );
  }
}

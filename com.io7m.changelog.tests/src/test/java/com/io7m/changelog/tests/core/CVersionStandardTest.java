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

package com.io7m.changelog.tests.core;

import com.io7m.changelog.core.CVersion;
import com.io7m.changelog.core.CVersions;
import com.io7m.jaffirm.core.PreconditionViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigInteger.ZERO;

public final class CVersionStandardTest
{
  @Test
  public void testInvalid_0()
  {
    Assertions.assertThrows(PreconditionViolationException.class, () -> {
      CVersion.of(new BigInteger("-1"), ZERO, ZERO);
    });
  }

  @Test
  public void testInvalid_1()
  {
    Assertions.assertThrows(PreconditionViolationException.class, () -> {
      CVersion.of(ZERO, new BigInteger("-1"), ZERO);
    });
  }

  @Test
  public void testInvalid_2()
  {
    Assertions.assertThrows(PreconditionViolationException.class, () -> {
      CVersion.of(ZERO, ZERO, new BigInteger("-1"));
    });
  }

  @Test
  public void testOrdering()
  {
    final CVersion p000 = CVersions.parse("0.0.0");
    final CVersion p010 = CVersions.parse("0.1.0");
    final CVersion p011 = CVersions.parse("0.1.1");
    final CVersion p100 = CVersions.parse("1.0.0");
    final CVersion p110 = CVersions.parse("1.1.0");
    final CVersion p111 = CVersions.parse("1.1.1");

    final List<CVersion> numbers = new ArrayList<CVersion>();
    numbers.add(p000);
    numbers.add(p010);
    numbers.add(p011);
    numbers.add(p100);
    numbers.add(p110);
    numbers.add(p111);

    for (int index = 0; index < numbers.size(); ++index) {
      for (int j = 0; j < numbers.size(); ++j) {
        final CVersion left = numbers.get(index);
        final CVersion right = numbers.get(j);

        System.out.println("Compare "
                             + left
                             + " "
                             + right
                             + " == "
                             + left.compareTo(right));

        if (j == index) {
          Assertions.assertEquals(0L, left.compareTo(right));
        }
        if (j < index) {
          Assertions.assertEquals(1L, left.compareTo(right));
          Assertions.assertEquals(-1L, right.compareTo(left));
        }
        if (j > index) {
          Assertions.assertEquals(-1L, left.compareTo(right));
          Assertions.assertEquals(1L, right.compareTo(left));
        }
      }
    }

    Assertions.assertEquals(0L, p000.compareTo(p000));
    Assertions.assertEquals(-1L, p000.compareTo(p100));
    Assertions.assertEquals(1L, p100.compareTo(p000));

    Assertions.assertEquals(0L, p010.compareTo(p010));
    Assertions.assertEquals(-1L, p000.compareTo(p010));
    Assertions.assertEquals(-1L, p010.compareTo(p110));
    Assertions.assertEquals(1L, p010.compareTo(p000));

    Assertions.assertEquals(0L, p111.compareTo(p111));
    Assertions.assertEquals(-1L, p011.compareTo(p111));
    Assertions.assertEquals(-1L, p010.compareTo(p110));
    Assertions.assertEquals(1L, p010.compareTo(p000));
  }

  @Test
  public void testParseNull()
  {
    Assertions.assertThrows(NullPointerException.class, () -> {
      CVersions.parse(null);
    });
  }

  @Test
  public void testValid_1()
  {
    final CVersion vs = CVersions.parse("1.2.3");
    Assertions.assertEquals(1L, vs.major().longValue());
    Assertions.assertEquals(2L, vs.minor().longValue());
    Assertions.assertEquals(3L, vs.patch().longValue());
  }
}

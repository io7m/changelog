package com.io7m.changelog.tests.core;/*
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

import com.io7m.changelog.core.CVersionStandard;
import com.io7m.changelog.core.CVersionText;
import com.io7m.changelog.core.CVersionType;
import com.io7m.changelog.core.CVersions;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public final class CVersionStandardTest
{
  @Test(
    expected = IllegalArgumentException.class)
  public void testInvalid_0()
  {
    CVersionStandard.of(-1, 0, 0, "");
  }

  @Test(
    expected = IllegalArgumentException.class)
  public void testInvalid_1()
  {
    CVersionStandard.of(0, -1, 0, "");
  }

  @Test(
    expected = IllegalArgumentException.class)
  public void testInvalid_2()
  {
    CVersionStandard.of(0, 0, -1, "");
  }

  @Test(
    expected = NullPointerException.class)
  public void testInvalid_3()
  {
    CVersionStandard.of(0, 0, 0, null);
  }

  @Test
  public void testOrdering()
  {
    final CVersionType p000 = CVersions.parse("0.0.0");
    final CVersionType p010 = CVersions.parse("0.1.0");
    final CVersionType p011 = CVersions.parse("0.1.1");
    final CVersionType p100 = CVersions.parse("1.0.0");
    final CVersionType p110 = CVersions.parse("1.1.0");
    final CVersionType p111 = CVersions.parse("1.1.1");

    final List<CVersionType> numbers = new ArrayList<CVersionType>();
    numbers.add(p000);
    numbers.add(p010);
    numbers.add(p011);
    numbers.add(p100);
    numbers.add(p110);
    numbers.add(p111);

    for (int index = 0; index < numbers.size(); ++index) {
      for (int j = 0; j < numbers.size(); ++j) {
        final CVersionType left = numbers.get(index);
        final CVersionType right = numbers.get(j);

        System.out.println("Compare "
                             + left
                             + " "
                             + right
                             + " == "
                             + left.compareTo(right));

        if (j == index) {
          Assert.assertEquals(0L, (long) left.compareTo(right));
        }
        if (j < index) {
          Assert.assertEquals(1L, (long) left.compareTo(right));
          Assert.assertEquals(-1L, (long) right.compareTo(left));
        }
        if (j > index) {
          Assert.assertEquals(-1L, (long) left.compareTo(right));
          Assert.assertEquals(1L, (long) right.compareTo(left));
        }
      }
    }

    Assert.assertEquals(0L, (long) p000.compareTo(p000));
    Assert.assertEquals(-1L, (long) p000.compareTo(p100));
    Assert.assertEquals(1L, (long) p100.compareTo(p000));

    Assert.assertEquals(0L, (long) p010.compareTo(p010));
    Assert.assertEquals(-1L, (long) p000.compareTo(p010));
    Assert.assertEquals(-1L, (long) p010.compareTo(p110));
    Assert.assertEquals(1L, (long) p010.compareTo(p000));

    Assert.assertEquals(0L, (long) p111.compareTo(p111));
    Assert.assertEquals(-1L, (long) p011.compareTo(p111));
    Assert.assertEquals(-1L, (long) p010.compareTo(p110));
    Assert.assertEquals(1L, (long) p010.compareTo(p000));
  }

  @Test
  public void testOrderQualifier()
  {
    final int mj = (int) (Math.random() * 100.0);
    final int mn = (int) (Math.random() * 100.0);
    final int mp = (int) (Math.random() * 100.0);

    final CVersionType v0 = CVersionStandard.of(mj, mn, mp, "");
    final CVersionType v1 = CVersionStandard.of(mj, mn, mp, "qasdasd");

    Assert.assertTrue(v0.compareTo(v1) > 0);
    Assert.assertTrue(v1.compareTo(v0) < 0);
  }

  @Test(
    expected = NullPointerException.class)
  public void testParseNull()
  {
    CVersions.parse(null);
  }

  @Test
  public void testValid_0()
  {
    final CVersionType v = CVersions.parse("1.2.3-xyz");
    Assert.assertTrue(v instanceof CVersionStandard);
    final CVersionStandard vs = (CVersionStandard) v;
    Assert.assertEquals(1L, (long) vs.major());
    Assert.assertEquals(2L, (long) vs.minor());
    Assert.assertEquals(3L, (long) vs.patch());
    Assert.assertEquals("xyz", vs.qualifier());
  }

  @Test
  public void testValid_1()
  {
    final CVersionType v = CVersions.parse("1.2.3");
    Assert.assertTrue(v instanceof CVersionStandard);
    final CVersionStandard vs = (CVersionStandard) v;
    Assert.assertEquals(1L, (long) vs.major());
    Assert.assertEquals(2L, (long) vs.minor());
    Assert.assertEquals(3L, (long) vs.patch());
    Assert.assertEquals("", vs.qualifier());
  }

  @Test
  public void testValid_2()
  {
    final CVersionType v = CVersions.parse("1");
    Assert.assertTrue(v instanceof CVersionText);
    final CVersionText vs = (CVersionText) v;
    Assert.assertEquals("1", vs.toVersionString());
  }

  @Test
  public void testValid_3()
  {
    final CVersionType v = CVersions.parse("1.2");
    Assert.assertTrue(v instanceof CVersionText);
    final CVersionText vs = (CVersionText) v;
    Assert.assertEquals("1.2", vs.toVersionString());
  }

  @Test
  public void testValid_4()
  {
    final CVersionType v = CVersions.parse("1.2.x");
    Assert.assertTrue(v instanceof CVersionText);
    final CVersionText vs = (CVersionText) v;
    Assert.assertEquals("1.2.x", vs.toVersionString());
  }
}

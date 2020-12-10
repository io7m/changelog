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

package com.io7m.changelog.tests.xml.api;

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.parser.api.CParseErrorHandlers;
import com.io7m.changelog.xml.api.CXMLChangelogParserProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogParserType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public abstract class CXMLChangelogParserContract
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CXMLChangelogParserContract.class);

  protected abstract CXMLChangelogParserProviderType parsers();

  @Test
  public final void testUnparseable0()
    throws Exception
  {
    Assertions.assertThrows(
      IOException.class,
      () -> this.parse("/com/io7m/changelog/tests/xml/unparseable0.xml"));
  }

  @Test
  public final void testInvalid0()
    throws Exception
  {
    Assertions.assertThrows(
      IOException.class,
      () -> this.parse("/com/io7m/changelog/tests/xml/invalid0.xml"));
  }

  @Test
  public final void testInvalid1()
    throws Exception
  {
    Assertions.assertThrows(
      IOException.class,
      () -> this.parse("/com/io7m/changelog/tests/xml/invalid1.xml"));
  }

  @Test
  public final void testInvalid2()
    throws Exception
  {
    Assertions.assertThrows(
      IOException.class,
      () -> this.parse("/com/io7m/changelog/tests/xml/basicTooManyOpen.xml"));
  }

  private CChangelog parse(
    final String name)
    throws IOException, URISyntaxException
  {
    final CXMLChangelogParserProviderType pp = this.parsers();
    final URL u = CXMLChangelogParserContract.class.getResource(name);
    final CXMLChangelogParserType p =
      pp.create(
        u.toURI(),
        u.openStream(),
        CParseErrorHandlers.loggingHandler(LOG));
    return p.parse();
  }
}

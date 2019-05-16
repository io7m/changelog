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

package com.io7m.changelog.tests.xml;

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.parser.api.CParseErrorHandlers;
import com.io7m.changelog.xml.api.CXMLChangelogParserProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogParserType;
import com.io7m.changelog.xml.api.CXMLChangelogWriterProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogWriterType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class CRoundTripContract
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CRoundTripContract.class);

  protected abstract CXMLChangelogParserProviderType parsers();

  protected abstract CXMLChangelogWriterProviderType writers();

  @Test
  public final void testRoundTrip0()
    throws Exception
  {
    this.runRoundTrip("/com/io7m/changelog/tests/xml/basic0.xml");
  }

  @Test
  public final void testRoundTrip1()
    throws Exception
  {
    this.runRoundTrip("/com/io7m/changelog/tests/xml/full.xml");
  }

  @Test
  public final void testRoundTrip2()
    throws Exception
  {
    this.runRoundTrip("/com/io7m/changelog/tests/xml/audiobook.xml");
  }

  private void runRoundTrip(
    final String name)
    throws IOException, URISyntaxException
  {
    final CXMLChangelogParserProviderType pp = this.parsers();
    final CXMLChangelogWriterProviderType wp = this.writers();

    final URL u = CRoundTripContract.class.getResource(name);

    final CChangelog c0;
    {
      final CXMLChangelogParserType p =
        pp.create(
          u.toURI(),
          u.openStream(),
          CParseErrorHandlers.loggingHandler(LOG));
      c0 = p.parse();
    }

    final Path tmp =
      Files.createTempFile("changelog-tests-", ".xml");

    try (OutputStream out = Files.newOutputStream(tmp)) {
      final CXMLChangelogWriterType w = wp.create(u.toURI(), out);
      w.write(c0);
    }

    final CChangelog c1;
    {
      final CXMLChangelogParserType p =
        pp.create(
          u.toURI(),
          u.openStream(),
          CParseErrorHandlers.loggingHandler(LOG));
      c1 = p.parse();
    }

    Assertions.assertEquals(c0, c1);
  }
}

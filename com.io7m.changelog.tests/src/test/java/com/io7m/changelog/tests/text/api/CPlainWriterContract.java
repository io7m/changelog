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

package com.io7m.changelog.tests.text.api;

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.parser.api.CParseErrorHandlers;
import com.io7m.changelog.text.api.CPlainChangelogWriterConfiguration;
import com.io7m.changelog.text.api.CPlainChangelogWriterProviderType;
import com.io7m.changelog.text.api.CPlainChangelogWriterType;
import com.io7m.changelog.xml.api.CXMLChangelogParserProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogParserType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class CPlainWriterContract
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CPlainWriterContract.class);

  protected abstract CXMLChangelogParserProviderType parsers();

  protected abstract CPlainChangelogWriterProviderType writers();

  @Test
  public final void testFull()
    throws Exception
  {
    this.checkExpected(
      "/com/io7m/changelog/tests/xml/full_plain.txt",
      "/com/io7m/changelog/tests/xml/full.xml");
  }

  private void checkExpected(
    final String plain_name,
    final String xml_name)
    throws IOException, URISyntaxException
  {
    final CChangelog clog = this.parse(xml_name);
    final ByteArrayOutputStream bao = new ByteArrayOutputStream();
    final CPlainChangelogWriterType writer =
      this.writers().createWithConfiguration(
        CPlainChangelogWriterConfiguration.builder()
          .setShowDates(true)
          .build(),
        URI.create("urn:stdout"),
        bao);

    writer.write(clog);

    final String text =
      bao.toString(StandardCharsets.UTF_8).trim();
    final URL u =
      CPlainWriterContract.class.getResource(plain_name);

    try (InputStream stream = u.openStream()) {
      final String expected =
        new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      Assertions.assertEquals(expected, text);
    }
  }

  private CChangelog parse(
    final String name)
    throws IOException, URISyntaxException
  {
    final CXMLChangelogParserProviderType pp = this.parsers();
    final URL u = CPlainWriterContract.class.getResource(name);
    final CXMLChangelogParserType p =
      pp.create(
        u.toURI(),
        u.openStream(),
        CParseErrorHandlers.loggingHandler(LOG));
    return p.parse();
  }
}

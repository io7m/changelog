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
import com.io7m.changelog.xml.api.CAtomChangelogWriterConfiguration;
import com.io7m.changelog.xml.api.CAtomChangelogWriterProviderType;
import com.io7m.changelog.xml.api.CAtomChangelogWriterType;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public abstract class CAtomWriterContract
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CAtomWriterContract.class);

  protected abstract CXMLChangelogParserProviderType parsers();

  protected abstract CAtomChangelogWriterProviderType writers();

  @Test
  public final void testFull()
    throws Exception
  {
    this.checkExpected(
      "/com/io7m/changelog/tests/xml/full.atom",
      "/com/io7m/changelog/tests/xml/full.xml");
  }

  private void checkExpected(
    final String plain_name,
    final String xml_name)
    throws IOException, URISyntaxException
  {
    final ZonedDateTime updated =
      ZonedDateTime.of(
        LocalDate.EPOCH,
        LocalTime.MIDNIGHT,
        ZoneId.of("UTC"));

    final CChangelog clog = this.parse(xml_name);
    final ByteArrayOutputStream bao = new ByteArrayOutputStream();
    final CAtomChangelogWriterType writer =
      this.writers().createWithConfiguration(
        CAtomChangelogWriterConfiguration.builder()
          .setUpdated(updated)
          .setTitle("Example")
          .setAuthorEmail("someone@example.com")
          .setAuthorName("Someone")
          .setUri(URI.create("http://example.com"))
          .build(),
        URI.create("urn:stdout"),
        bao);

    writer.write(clog);

    final String text =
      bao.toString(StandardCharsets.UTF_8).trim();
    final URL u =
      CAtomWriterContract.class.getResource(plain_name);

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
    final URL u = CAtomWriterContract.class.getResource(name);
    final CXMLChangelogParserType p =
      pp.create(
        u.toURI(),
        u.openStream(),
        CParseErrorHandlers.loggingHandler(LOG));
    return p.parse();
  }
}

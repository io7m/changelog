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

package com.io7m.changelog.tests.xom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.xom.CChangelogXMLReader;
import com.io7m.changelog.xom.CChangelogXMLWriter;

public final class RoundTripTest
{
  @SuppressWarnings("static-method") @Test public void testRoundTripJaux()
    throws URISyntaxException,
      MalformedURLException,
      ValidityException,
      IOException,
      SAXException,
      ParserConfigurationException,
      ParsingException,
      ParseException
  {
    RoundTripTest.roundTrip("jaux.xml");
  }

  private static void roundTrip(
    final String name)
    throws URISyntaxException,
      MalformedURLException,
      IOException,
      ValidityException,
      SAXException,
      ParserConfigurationException,
      ParsingException,
      ParseException
  {
    final String path = "/com/io7m/changelog/tests/xom/" + name;
    final URI uri = RoundTripTest.class.getResource(path).toURI();
    final CChangelog cc0 = CChangelogXMLReader.readFromURI(uri);
    final Element root = CChangelogXMLWriter.writeElement(cc0);
    final Document doc = new Document(root);

    {
      final Serializer s = new Serializer(System.out);
      s.setIndent(2);
      s.write(doc);
    }

    final ByteArrayOutputStream bytes = new ByteArrayOutputStream(8192);
    final Serializer s = new Serializer(bytes);
    s.setIndent(2);
    s.setMaxLength(80);
    s.write(doc);
    final ByteArrayInputStream bi =
      new ByteArrayInputStream(bytes.toByteArray());
    final CChangelog cc1 = CChangelogXMLReader.readFromStream(uri, bi);
    Assert.assertEquals(cc0, cc1);
  }
}

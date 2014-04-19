/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

import org.junit.Assume;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.io7m.changelog.core.CChangeType;
import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CChangelogBuilderType;
import com.io7m.changelog.core.CItem;
import com.io7m.changelog.core.CRelease;
import com.io7m.changelog.core.CVersionStandard;
import com.io7m.changelog.xom.CAtomFeedMeta;
import com.io7m.changelog.xom.CAtomFeedMetaBuilderType;
import com.io7m.changelog.xom.CChangelogAtomWriter;
import com.io7m.changelog.xom.CChangelogXMLReader;

public class CChangelogAtomWriterTest
{
  @SuppressWarnings("static-method") @Test public void testLog()
    throws MalformedURLException,
      ValidityException,
      IOException,
      SAXException,
      ParserConfigurationException,
      ParsingException,
      URISyntaxException,
      ParseException
  {
    Assume.assumeTrue(false);

    final File file = new File("../README-CHANGES.xml");
    final CChangelog cc0 = CChangelogXMLReader.readFromURI(file.toURI());

    final CAtomFeedMetaBuilderType builder = CAtomFeedMeta.newBuilder();
    builder.setAuthorEmail("someone@example.com");
    builder.setAuthorName("Someone");
    builder.setTitle("Example feed");
    builder.setURI(new URI("http://example.com"));
    final CAtomFeedMeta meta = builder.build();

    final Element e = CChangelogAtomWriter.writeElement(meta, cc0);
    final Document d = new Document(e);
    final Serializer s = new Serializer(System.out);
    s.setIndent(2);
    s.write(d);
  }

  @SuppressWarnings("static-method") @Test public void testPlain()
    throws URISyntaxException,
      ParseException,
      IOException
  {
    final CChangelogBuilderType b = CChangelog.newBuilder();
    b.setProjectName("example");
    b.addTicketSystem("t", new URI("http://example.com/tickets/"));
    final Date date = new SimpleDateFormat("yyyy-MM-dd").parse("2014-01-01");
    final List<CItem> items = new ArrayList<CItem>();

    items.add(CItem.newItem(
      new ArrayList<String>(),
      "Summary 0",
      date,
      CChangeType.CHANGE_TYPE_CODE_FIX));

    final ArrayList<String> tickets = new ArrayList<String>();
    tickets.add("23");
    tickets.add("48");
    items.add(CItem.newItem(
      tickets,
      "Summary 1",
      date,
      CChangeType.CHANGE_TYPE_CODE_CHANGE));

    items.add(CItem.newItem(
      new ArrayList<String>(),
      "Summary 2",
      date,
      CChangeType.CHANGE_TYPE_CODE_NEW));

    b.addRelease(CRelease.newRelease(
      "t",
      date,
      CVersionStandard.parse("1.0.0"),
      items));

    final CAtomFeedMetaBuilderType builder = CAtomFeedMeta.newBuilder();
    builder.setAuthorEmail("someone@example.com");
    builder.setAuthorName("Someone");
    builder.setTitle("Example feed");
    builder.setURI(new URI("http://example.com"));
    final CAtomFeedMeta meta = builder.build();

    final Element e = CChangelogAtomWriter.writeElement(meta, b.build());
    final Document d = new Document(e);
    final Serializer s = new Serializer(System.out);
    s.setIndent(2);
    s.write(d);
  }
}

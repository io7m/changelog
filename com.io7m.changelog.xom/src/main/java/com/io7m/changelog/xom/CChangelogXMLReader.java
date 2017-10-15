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

package com.io7m.changelog.xom;

import com.io7m.changelog.core.CChangeType;
import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CItem;
import com.io7m.changelog.core.CRelease;
import com.io7m.changelog.core.CVersionType;
import com.io7m.changelog.core.CVersions;
import com.io7m.changelog.schema.CSchema;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * A changelog XML writer.
 */

public final class CChangelogXMLReader
{
  private CChangelogXMLReader()
  {
    throw new AssertionError("Unreachable");
  }

  private static LocalDate date(
    final Element e,
    final DateTimeFormatter df)
    throws ParseException
  {
    final String cs_uri = CSchema.XML_URI.toString();
    final Element ed = e.getFirstChildElement("date", cs_uri);
    return LocalDate.parse(ed.getValue(), df);
  }

  static Document fromStreamValidate(
    final InputStream stream,
    final URI uri)
    throws SAXException,
    ParserConfigurationException,
    ValidityException,
    ParsingException,
    IOException
  {
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(false);
    factory.setNamespaceAware(true);
    factory.setXIncludeAware(true);
    factory.setFeature("http://apache.org/xml/features/xinclude", true);

    final InputStream xml_xsd =
      new URL(CSchema.getURIXMLXSD().toString()).openStream();

    try {
      final InputStream schema_xsd =
        new URL(CSchema.getURISchemaXSD().toString()).openStream();

      try {
        final SchemaFactory schema_factory =
          SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        final Source[] sources = new Source[2];
        sources[0] = new StreamSource(xml_xsd);
        sources[1] = new StreamSource(schema_xsd);
        factory.setSchema(schema_factory.newSchema(sources));

        final TrivialErrorHandler handler = new TrivialErrorHandler();
        final SAXParser parser = factory.newSAXParser();
        final XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(handler);

        final Builder builder = new Builder(reader);
        final Document doc = builder.build(stream, uri.toString());

        if (handler.getException() != null) {
          throw handler.getException();
        }

        return doc;
      } finally {
        schema_xsd.close();
      }
    } finally {
      xml_xsd.close();
    }
  }

  private static Attribute getXMLIDAttribute(
    final Element e)
  {
    return e.getAttribute("id", "http://www.w3.org/XML/1998/namespace");
  }

  private static CItem item(
    final Element e,
    final DateTimeFormatter df)
    throws ParseException
  {
    final String cs_uri = CSchema.XML_URI.toString();

    final String summary =
      e.getFirstChildElement("summary", cs_uri).getValue();
    final String date_text =
      e.getFirstChildElement("date", cs_uri).getValue();
    final LocalDate date =
      LocalDate.parse(date_text, df);

    final List<String> tickets = tickets(e);

    {
      final Element et = e.getFirstChildElement("type-code-new", cs_uri);
      if (et != null) {
        return CItem.builder()
          .setDate(date)
          .setSummary(summary)
          .setTickets(tickets)
          .setType(CChangeType.CHANGE_TYPE_CODE_NEW)
          .build();
      }
    }

    {
      final Element et = e.getFirstChildElement("type-code-fix", cs_uri);
      if (et != null) {
        return CItem.builder()
          .setDate(date)
          .setSummary(summary)
          .setTickets(tickets)
          .setType(CChangeType.CHANGE_TYPE_CODE_FIX)
          .build();
      }
    }

    {
      final Element et = e.getFirstChildElement("type-code-change", cs_uri);
      if (et != null) {
        return CItem.builder()
          .setDate(date)
          .setSummary(summary)
          .setTickets(tickets)
          .setType(CChangeType.CHANGE_TYPE_CODE_CHANGE)
          .build();
      }
    }

    throw new AssertionError("Unreachable");
  }

  private static List<CItem> items(
    final Element e,
    final DateTimeFormatter df)
    throws ParseException
  {
    final String cs_uri = CSchema.XML_URI.toString();
    final Elements ei = e.getChildElements("item", cs_uri);
    final List<CItem> r = new ArrayList<CItem>();

    for (int index = 0; index < ei.size(); ++index) {
      r.add(item(ei.get(index), df));
    }

    return r;
  }

  private static void project(
    final Element root,
    final CChangelog.Builder builder)
  {
    final String cs_uri = CSchema.XML_URI.toString();
    final Element e = root.getFirstChildElement("project", cs_uri);
    builder.setProject(e.getValue());
  }

  /**
   * Construct a changelog from the given URI.
   *
   * @param uri    The URI
   * @param stream The stream
   *
   * @return A new changelog
   *
   * @throws SAXException                 On XML parse errors
   * @throws ParserConfigurationException On parser configuration errors
   * @throws ValidityException            On XML validation errors
   * @throws ParsingException             On parser errors
   * @throws IOException                  On I/O errors
   * @throws URISyntaxException           If a URI is malformed
   * @throws ParseException               If a value cannot be parsed as a date
   */

  public static CChangelog readFromStream(
    final URI uri,
    final InputStream stream)
    throws SAXException,
    ParserConfigurationException,
    ValidityException,
    ParsingException,
    IOException,
    URISyntaxException,
    ParseException
  {
    if (uri == null) {
      throw new NullPointerException("URI");
    }
    if (stream == null) {
      throw new NullPointerException("Stream");
    }

    final Document doc = fromStreamValidate(stream, uri);
    final Element root = doc.getRootElement();

    final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    final CChangelog.Builder builder = CChangelog.builder();
    project(root, builder);
    ticketSystems(root, builder);
    releases(root, df, builder);

    return builder.build();
  }

  /**
   * Construct a changelog from the given URI.
   *
   * @param uri The URI
   *
   * @return A new changelog
   *
   * @throws SAXException                 On XML parse errors
   * @throws ParserConfigurationException On parser configuration errors
   * @throws ValidityException            On XML validation errors
   * @throws ParsingException             On parser errors
   * @throws IOException                  On I/O errors
   * @throws MalformedURLException        If a URL is malformed
   * @throws URISyntaxException           If a URI is malformed
   * @throws ParseException               If a value cannot be parsed as a date
   */

  public static CChangelog readFromURI(
    final URI uri)
    throws MalformedURLException,
    IOException,
    ValidityException,
    SAXException,
    ParserConfigurationException,
    ParsingException,
    URISyntaxException,
    ParseException
  {
    if (uri == null) {
      throw new NullPointerException("URI");
    }

    final InputStream stream = uri.toURL().openStream();
    try {
      return readFromStream(uri, stream);
    } finally {
      stream.close();
    }
  }

  private static CRelease release(
    final Element e,
    final DateTimeFormatter df)
    throws ParseException
  {
    final String cs_uri = CSchema.XML_URI.toString();
    final Attribute eid = e.getAttribute("ticket-system", cs_uri);
    final LocalDate date = date(e, df);
    final CVersionType version = version(e);
    final List<CItem> items = items(e, df);
    return CRelease.of(date, items, eid.getValue(), version);
  }

  private static void releases(
    final Element root,
    final DateTimeFormatter df,
    final CChangelog.Builder builder)
    throws ParseException
  {
    final String cs_uri = CSchema.XML_URI.toString();
    final Elements ers = root.getChildElements("release", cs_uri);
    for (int index = 0; index < ers.size(); ++index) {
      final Element e = ers.get(index);
      builder.addReleases(release(e, df));
    }
  }

  private static List<String> tickets(
    final Element e)
  {
    final String cs_uri = CSchema.XML_URI.toString();
    final List<String> rs = new ArrayList<String>();
    final Elements ets = e.getChildElements("ticket", cs_uri);
    for (int index = 0; index < ets.size(); ++index) {
      rs.add(ets.get(index).getValue());
    }
    return rs;
  }

  private static void ticketSystems(
    final Element root,
    final CChangelog.Builder builder)
    throws URISyntaxException
  {
    final String cs_uri = CSchema.XML_URI.toString();
    final Elements ets = root.getChildElements("ticket-system", cs_uri);
    for (int index = 0; index < ets.size(); ++index) {
      final Element e = ets.get(index);
      final Attribute eid = getXMLIDAttribute(e);
      final Element eus = e.getFirstChildElement("ticket-url", cs_uri);
      final URI tu = new URI(eus.getValue());
      builder.putTicketSystems(eid.getValue(), tu);
    }
  }

  private static CVersionType version(
    final Element e)
  {
    final String cs_uri = CSchema.XML_URI.toString();
    final Element ev = e.getFirstChildElement("version", cs_uri);
    return CVersions.parse(ev.getValue());
  }

  /**
   * A trivial error handler that records exceptions.
   */

  private static class TrivialErrorHandler implements ErrorHandler
  {
    private SAXParseException exception;

    TrivialErrorHandler()
    {
      // Nothing
    }

    @Override
    public void error(
      final SAXParseException e)
      throws SAXException
    {
      this.exception = e;
    }

    @Override
    public void fatalError(
      final SAXParseException e)
      throws SAXException
    {
      this.exception = e;
    }

    public SAXParseException getException()
    {
      return this.exception;
    }

    @Override
    public void warning(
      final SAXParseException e)
      throws SAXException
    {
      this.exception = e;
    }
  }
}

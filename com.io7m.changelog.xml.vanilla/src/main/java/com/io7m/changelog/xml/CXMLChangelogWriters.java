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

package com.io7m.changelog.xml;

import com.io7m.changelog.core.CChange;
import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CRelease;
import com.io7m.changelog.core.CTicketSystem;
import com.io7m.changelog.core.CVersionType;
import com.io7m.changelog.schema.CSchema;
import com.io7m.changelog.xml.api.CXMLChangelogWriterProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogWriterType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * A provider for XML writers.
 */

public final class CXMLChangelogWriters
  implements CXMLChangelogWriterProviderType
{
  private final SchemaFactory schema_factory;
  private final DocumentBuilderFactory doc_factory;

  /**
   * Instantiate a provider.
   */

  public CXMLChangelogWriters()
  {
    this.schema_factory =
      SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    this.doc_factory =
      DocumentBuilderFactory.newInstance();
  }

  @Override
  public CXMLChangelogWriterType create(
    final URI in_uri,
    final OutputStream in_stream)
    throws IOException
  {
    Objects.requireNonNull(in_uri, "URI");
    Objects.requireNonNull(in_stream, "Stream");

    try {
      final Schema schema =
        this.schema_factory.newSchema(CSchema.getURISchemaXSD().toURL());

      this.doc_factory.setNamespaceAware(true);
      this.doc_factory.setSchema(schema);

      return new Writer(this.doc_factory, in_uri, in_stream);
    } catch (final SAXException e) {
      throw new IOException(e);
    }
  }

  private static final class Writer implements CXMLChangelogWriterType
  {
    private final URI uri;
    private final OutputStream stream;
    private final DocumentBuilderFactory doc_factory;
    private final String schema_uri;
    private final DateTimeFormatter date_formatter;

    Writer(
      final DocumentBuilderFactory in_doc_factory,
      final URI in_uri,
      final OutputStream in_stream)
    {
      this.doc_factory =
        Objects.requireNonNull(in_doc_factory, "Document Factory");
      this.uri =
        Objects.requireNonNull(in_uri, "URI");
      this.stream =
        Objects.requireNonNull(in_stream, "Stream");
      this.schema_uri =
        CSchema.XML_URI.toString();
      this.date_formatter =
        CDateFormatters.newDateFormatter();
    }

    @Override
    public void write(
      final CChangelog changelog)
      throws IOException
    {
      Objects.requireNonNull(changelog, "Changelog");

      try {
        final DocumentBuilder doc_builder =
          this.doc_factory.newDocumentBuilder();
        final Document doc =
          doc_builder.newDocument();

        final Element root =
          doc.createElementNS(this.schema_uri, "c:changelog");

        doc.appendChild(root);

        root.setAttribute("project", changelog.project());

        final Element releases =
          doc.createElementNS(this.schema_uri, "c:releases");
        final Element ticket_systems =
          doc.createElementNS(this.schema_uri, "c:ticket-systems");

        changelog.releases().forEach(
          (version, release) ->
            this.writeRelease(doc, releases, version, release));

        changelog.ticketSystems().forEach(
          (name, ticket_uri) ->
            this.writeTicketSystem(doc, ticket_systems, name, ticket_uri));

        root.appendChild(releases);
        root.appendChild(ticket_systems);

        this.serializeDocument(doc);
      } catch (final ParserConfigurationException e) {
        throw new IOException(e);
      }
    }

    private void writeTicketSystem(
      final Document doc,
      final Element ticket_systems,
      final String name,
      final CTicketSystem ticket_system)
    {
      final Element e_system =
        doc.createElementNS(this.schema_uri, "c:ticket-system");

      e_system.setAttribute("id", name);
      e_system.setAttribute("url", ticket_system.uri().toString());
      e_system.setAttribute(
        "default",
        String.valueOf(ticket_system.isDefault()));

      ticket_systems.appendChild(e_system);
    }

    private void writeRelease(
      final Document doc,
      final Element releases,
      final CVersionType version,
      final CRelease release)
    {
      final Element e_release =
        doc.createElementNS(this.schema_uri, "c:release");

      e_release.setAttribute(
        "date",
        this.date_formatter.format(release.date()));
      e_release.setAttribute(
        "version",
        version.toVersionString());
      e_release.setAttribute(
        "ticket-system",
        release.ticketSystemID());

      final Element changes =
        doc.createElementNS(this.schema_uri, "c:changes");

      release.changes().forEach(
        change -> this.writeChange(doc, changes, change));

      e_release.appendChild(changes);
      releases.appendChild(e_release);
    }

    private void writeChange(
      final Document doc,
      final Element changes,
      final CChange change)
    {
      final Element e_change =
        doc.createElementNS(this.schema_uri, "c:change");

      e_change.setAttribute(
        "date",
        this.date_formatter.format(change.date()));

      e_change.setAttribute(
        "summary",
        change.summary());

      change.module().ifPresent(
        module -> e_change.setAttribute("module", module));

      if (!change.backwardsCompatible()) {
        e_change.setAttribute("compatible", "false");
      }

      if (!change.tickets().isEmpty()) {
        final Element tickets =
          doc.createElementNS(this.schema_uri, "c:tickets");

        change.tickets().forEach(ticket -> {
          final Element e_ticket =
            doc.createElementNS(this.schema_uri, "c:ticket");
          e_ticket.setAttribute("id", ticket);
          tickets.appendChild(e_ticket);
        });

        e_change.appendChild(tickets);
      }

      changes.appendChild(e_change);
    }

    private void serializeDocument(
      final Document doc)
      throws IOException
    {
      try {
        final TransformerFactory transformer_factory =
          TransformerFactory.newInstance();
        final Transformer transformer =
          transformer_factory.newTransformer();

        transformer.setOutputProperty(
          OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(
          OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
        transformer.setOutputProperty(
          "{http://xml.apache.org/xslt}indent-amount",
          "2");

        transformer.transform(
          new DOMSource(doc),
          new StreamResult(this.stream));

      } catch (final TransformerException e) {
        throw new IOException(e);
      }
    }
  }
}

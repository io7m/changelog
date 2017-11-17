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
import com.io7m.changelog.core.CTicketID;
import com.io7m.changelog.core.CTicketSystem;
import com.io7m.changelog.xml.api.CXHTMLChangelogWriterProviderType;
import com.io7m.changelog.xml.api.CXHTMLChangelogWriterType;
import io.vavr.collection.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * A provider for XHTML writers.
 */

public final class CXHTMLChangelogWriters
  implements CXHTMLChangelogWriterProviderType
{
  private final DocumentBuilderFactory doc_factory;

  /**
   * Instantiate a provider.
   */

  public CXHTMLChangelogWriters()
  {
    this.doc_factory =
      DocumentBuilderFactory.newInstance();
  }

  @Override
  public CXHTMLChangelogWriterType create(
    final URI in_uri,
    final OutputStream in_stream)
    throws IOException
  {
    Objects.requireNonNull(in_uri, "URI");
    Objects.requireNonNull(in_stream, "Stream");

    this.doc_factory.setNamespaceAware(true);
    return new Writer(this.doc_factory, in_uri, in_stream);
  }

  private static final class Writer implements CXHTMLChangelogWriterType
  {
    private static final String XHTML_NS = "http://www.w3.org/1999/xhtml";

    private final URI uri;
    private final OutputStream stream;
    private final DocumentBuilderFactory doc_factory;
    private final DateTimeFormatter date_terse_formatter;

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
      this.date_terse_formatter =
        CDateFormatters.newDateTerseFormatter();
    }

    private static List<Node> transformTickets(
      final CChangelog changelog,
      final Document doc,
      final CRelease release,
      final List<CTicketID> tickets)
    {
      return tickets.map(
        ticket -> transformTicket(changelog, doc, release, ticket));
    }

    private static Node transformTicket(
      final CChangelog changelog,
      final Document doc,
      final CRelease release,
      final CTicketID ticket)
    {
      final CTicketSystem ticket_system =
        changelog.ticketSystems().get(release.ticketSystemID()).get();
      final Element a =
        doc.createElement("a");
      final URI target =
        URI.create(ticket_system.uri().toString() + ticket.value());
      a.setAttribute("href", target.toString());
      a.setTextContent(ticket.value());
      return a;
    }

    private static void row(
      final Document doc,
      final Element releases,
      final String date_text,
      final List<Node> td_text_nodes)
    {
      final Element tr =
        doc.createElementNS(XHTML_NS, "tr");

      final Element td_date =
        doc.createElementNS(XHTML_NS, "td");
      td_date.setTextContent(date_text);

      final Element td_text =
        doc.createElementNS(XHTML_NS, "td");
      td_text_nodes.forEach(td_text::appendChild);

      tr.appendChild(td_date);
      tr.appendChild(td_text);

      releases.appendChild(tr);
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
          doc.createElementNS(XHTML_NS, "table");
        root.setAttribute(
          "summary",
          "Changes for project " + changelog.project().value());
        root.setAttribute("class", "changelog");

        for (final CRelease release : changelog.releases().values().reverse()) {
          this.writeRelease(changelog, doc, root, release);
        }

        doc.appendChild(root);

        this.serializeDocument(doc);
      } catch (final ParserConfigurationException e) {
        throw new IOException(e);
      }
    }

    private void writeRelease(
      final CChangelog changelog,
      final Document doc,
      final Element releases,
      final CRelease release)
    {
      row(
        doc,
        releases,
        this.date_terse_formatter.format(release.date().toLocalDate()),
        List.of(doc.createTextNode(
          new StringBuilder()
            .append("Release: ")
            .append(changelog.project().value())
            .append(" ")
            .append(release.version().toVersionString())
            .toString())));

      for (final CChange change : release.changes()) {
        row(
          doc,
          releases,
          this.date_terse_formatter.format(change.date().toLocalDate()),
          this.transformChange(changelog, doc, release, change));
      }
    }

    private List<Node> transformChange(
      final CChangelog changelog,
      final Document doc,
      final CRelease release,
      final CChange change)
    {
      List<Node> nodes = List.empty();

      final StringBuilder sb = new StringBuilder(128);
      sb.append("Change: ");
      if (!change.backwardsCompatible()) {
        sb.append("(Backwards incompatible) ");
      }
      change.module().ifPresent(module -> {
        sb.append(module);
        sb.append(": ");
      });

      sb.append(change.summary());
      nodes = nodes.append(doc.createTextNode(sb.toString()));

      if (!change.tickets().isEmpty()) {
        nodes = nodes.append(doc.createTextNode(" (tickets: "));
        nodes = nodes.appendAll(
          transformTickets(changelog, doc, release, change.tickets()));
        nodes = nodes.append(doc.createTextNode(")"));
      }

      return nodes;
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

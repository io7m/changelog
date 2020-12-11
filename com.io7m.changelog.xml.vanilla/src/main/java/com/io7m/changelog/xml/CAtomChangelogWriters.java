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

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CRelease;
import com.io7m.changelog.xml.api.CAtomChangelogWriterConfiguration;
import com.io7m.changelog.xml.api.CAtomChangelogWriterProviderType;
import com.io7m.changelog.xml.api.CAtomChangelogWriterType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A provider for Atom feed writers.
 */

public final class CAtomChangelogWriters
  implements CAtomChangelogWriterProviderType
{
  private static final String ATOM_NS = "http://www.w3.org/2005/Atom";

  private final DocumentBuilderFactory doc_factory;

  /**
   * Instantiate a provider.
   */

  public CAtomChangelogWriters()
  {
    this.doc_factory =
      DocumentBuilderFactory.newInstance();
  }

  @Override
  public CAtomChangelogWriterType createWithConfiguration(
    final CAtomChangelogWriterConfiguration in_configuration,
    final URI in_uri,
    final OutputStream in_stream)
    throws IOException
  {
    Objects.requireNonNull(in_configuration, "Configuration");
    Objects.requireNonNull(in_uri, "URI");
    Objects.requireNonNull(in_stream, "Stream");

    this.doc_factory.setNamespaceAware(true);
    return new Writer(this.doc_factory, in_configuration, in_stream);
  }

  private static final class Writer implements CAtomChangelogWriterType
  {
    private final OutputStream stream;
    private final DocumentBuilderFactory doc_factory;
    private final DateTimeFormatter date_formatter;
    private final CAtomChangelogWriterConfiguration configuration;

    Writer(
      final DocumentBuilderFactory in_doc_factory,
      final CAtomChangelogWriterConfiguration in_configuration,
      final OutputStream in_stream)
    {
      this.doc_factory =
        Objects.requireNonNull(in_doc_factory, "Document Factory");
      this.configuration =
        Objects.requireNonNull(in_configuration, "Configuration");
      this.stream =
        Objects.requireNonNull(in_stream, "Stream");
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

        final Element e_root =
          doc.createElementNS(ATOM_NS, "a:feed");

        doc.appendChild(e_root);

        final Element e_author =
          doc.createElementNS(ATOM_NS, "a:author");
        final Element e_author_name =
          doc.createElementNS(ATOM_NS, "a:name");
        final Element e_author_email =
          doc.createElementNS(ATOM_NS, "a:email");

        e_author_email.setTextContent(this.configuration.authorEmail());
        e_author_name.setTextContent(this.configuration.authorName());
        e_author.appendChild(e_author_name);
        e_author.appendChild(e_author_email);
        e_root.appendChild(e_author);

        final Element e_id =
          doc.createElementNS(ATOM_NS, "a:id");
        e_id.setTextContent(this.configuration.uri().toString());
        e_root.appendChild(e_id);

        final Element e_title =
          doc.createElementNS(ATOM_NS, "a:title");
        e_title.setTextContent(this.configuration.title());
        e_root.appendChild(e_title);

        final Element e_updated =
          doc.createElementNS(ATOM_NS, "a:updated");
        e_updated.setTextContent(
          this.date_formatter.format(this.configuration.updated()));
        e_root.appendChild(e_updated);

        final var versions =
          changelog.releaseVersions()
            .stream()
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());

        for (final var version : versions) {
          final CRelease release = changelog.releases().get(version);
          this.writeRelease(changelog, doc, e_root, release);
        }

        this.serializeDocument(doc);
      } catch (final ParserConfigurationException e) {
        throw new IOException(e);
      }
    }

    private void writeRelease(
      final CChangelog changelog,
      final Document doc,
      final Element e_root,
      final CRelease r)
    {
      final Element e_release =
        doc.createElementNS(ATOM_NS, "a:entry");

      final Element e_id =
        doc.createElementNS(ATOM_NS, "a:id");
      e_id.setTextContent(String.format("%s", r.version()));

      final Element e_updated =
        doc.createElementNS(ATOM_NS, "a:updated");
      final Element e_published =
        doc.createElementNS(ATOM_NS, "a:published");

      final String time = this.date_formatter.format(r.date());
      e_updated.setTextContent(time);
      e_published.setTextContent(time);

      final Element e_title =
        doc.createElementNS(ATOM_NS, "a:title");

      final String text =
        new StringBuilder(64)
          .append(changelog.project().value())
          .append(" ")
          .append(String.format("%s", r.version()))
          .append(" released")
          .toString();

      e_title.setTextContent(text);

      final Element e_content =
        doc.createElementNS(ATOM_NS, "a:content");
      e_content.setAttribute("type", "text");
      e_content.setTextContent(text);

      e_release.appendChild(e_id);
      e_release.appendChild(e_updated);
      e_release.appendChild(e_published);
      e_release.appendChild(e_title);
      e_release.appendChild(e_content);
      e_root.appendChild(e_release);
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

        this.stream.write(
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes(UTF_8));
        this.stream.write(System.lineSeparator().getBytes(UTF_8));
        this.stream.flush();

        transformer.setOutputProperty(
          OutputKeys.OMIT_XML_DECLARATION,
          "yes");
        transformer.setOutputProperty(
          OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(
          OutputKeys.ENCODING, UTF_8.name());
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

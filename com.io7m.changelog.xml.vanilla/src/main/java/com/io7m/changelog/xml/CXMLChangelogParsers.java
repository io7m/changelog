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
import com.io7m.changelog.core.CModuleName;
import com.io7m.changelog.core.CProjectName;
import com.io7m.changelog.core.CRelease;
import com.io7m.changelog.core.CTicketID;
import com.io7m.changelog.core.CTicketSystem;
import com.io7m.changelog.core.CVersions;
import com.io7m.changelog.parser.api.CParseError;
import com.io7m.changelog.parser.api.CParseErrorType;
import com.io7m.changelog.schema.CSchema;
import com.io7m.changelog.xml.api.CXMLChangelogParserProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogParserType;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jxe.core.JXEHardenedSAXParsers;
import com.io7m.jxe.core.JXESchemaDefinition;
import com.io7m.jxe.core.JXESchemaResolutionMappings;
import com.io7m.jxe.core.JXEXInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A provider for changelog parsers.
 */

public final class CXMLChangelogParsers
  implements CXMLChangelogParserProviderType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CXMLChangelogParsers.class);

  private final JXEHardenedSAXParsers parsers;
  private final JXESchemaResolutionMappings schemas;

  /**
   * Instantiate a parser provider.
   */

  public CXMLChangelogParsers()
  {
    final JXESchemaDefinition schema;
    try {
      schema = JXESchemaDefinition.of(
        CSchema.XML_URI,
        "changelog-4.0.xsd",
        CSchema.getURISchemaXSD().toURL());
    } catch (final MalformedURLException e) {
      throw new IllegalStateException(e);
    }

    this.schemas =
      JXESchemaResolutionMappings.builder()
        .putMappings(CSchema.XML_URI, schema)
        .build();

    this.parsers = new JXEHardenedSAXParsers();
  }

  @Override
  public CXMLChangelogParserType create(
    final URI uri,
    final InputStream stream,
    final Consumer<CParseError> receiver)
    throws IOException
  {
    Objects.requireNonNull(uri, "URI");
    Objects.requireNonNull(stream, "Stream");
    Objects.requireNonNull(receiver, "Receiver");

    try {
      final XMLReader parser =
        this.parsers.createXMLReader(
          Optional.empty(),
          JXEXInclude.XINCLUDE_DISABLED,
          this.schemas);
      return new Parser(uri, stream, receiver, parser);
    } catch (final ParserConfigurationException | SAXException e) {
      throw new IOException(e);
    }
  }

  private static final class Parser
    extends DefaultHandler implements CXMLChangelogParserType
  {
    private final XMLReader parser;
    private final URI uri;
    private final InputStream stream;
    private final CChangelog.Builder changelog_builder;
    private final ArrayDeque<CurrentElement> elements;
    private final CRelease.Builder release_builder;
    private final DateTimeFormatter date_format;
    private final CChange.Builder change_builder;
    private final CTicketSystem.Builder ticket_system_builder;
    private final Consumer<CParseError> receiver;
    private Locator locator;
    private boolean failed;
    private int openCount;

    Parser(
      final URI in_uri,
      final InputStream in_stream,
      final Consumer<CParseError> in_receiver,
      final XMLReader in_parser)
    {
      this.uri =
        Objects.requireNonNull(in_uri, "URI");
      this.stream =
        Objects.requireNonNull(in_stream, "Stream");
      this.receiver =
        Objects.requireNonNull(in_receiver, "Receiver");
      this.parser =
        Objects.requireNonNull(in_parser, "Parser");

      this.changelog_builder = CChangelog.builder();
      this.release_builder = CRelease.builder();
      this.change_builder = CChange.builder();
      this.ticket_system_builder = CTicketSystem.builder();
      this.elements = new ArrayDeque<>();
      this.date_format = CDateFormatters.newDateFormatter();
    }

    @Override
    public InputSource resolveEntity(
      final String in_public_id,
      final String in_system_id)
      throws IOException, SAXException
    {
      LOG.trace("resolveEntity: {} {}", in_public_id, in_system_id);
      return super.resolveEntity(in_public_id, in_system_id);
    }

    @Override
    public void notationDecl(
      final String in_name,
      final String in_public_id,
      final String in_system_id)
    {
      LOG.trace("notationDecl: {} {} {}", in_name, in_public_id, in_system_id);
    }

    @Override
    public void unparsedEntityDecl(
      final String in_name,
      final String in_public_id,
      final String in_system_id,
      final String in_notation)
    {
      LOG.trace(
        "unparsedEntityDecl: {} {} {} {}",
        in_name,
        in_public_id,
        in_system_id,
        in_notation);
    }

    @Override
    public void setDocumentLocator(
      final Locator in_locator)
    {
      LOG.trace("setDocumentLocator: {}", in_locator);
      this.locator = Objects.requireNonNull(in_locator, "Locator");
    }

    @Override
    public void startDocument()
    {
      LOG.trace("startDocument");
    }

    @Override
    public void endDocument()
    {
      LOG.trace("endDocument");
    }

    @Override
    public void startPrefixMapping(
      final String in_prefix,
      final String in_uri)
      throws SAXException
    {
      LOG.trace("startPrefixMapping: {} {}", in_prefix, in_uri);

      final String uri_expected = CSchema.XML_URI.toString();
      if (!Objects.equals(in_uri, uri_expected)) {
        final String separator = System.lineSeparator();
        throw new SAXParseException(
          new StringBuilder(64)
            .append("Unexpected document type.")
            .append(separator)
            .append("  Expected: ")
            .append(uri_expected)
            .append(separator)
            .append("  Received: ")
            .append(in_uri)
            .append(separator)
            .toString(),
          this.locator);
      }
    }

    @Override
    public void endPrefixMapping(
      final String prefix)
    {
      LOG.trace("endPrefixMapping: {}", prefix);
    }

    @Override
    public void startElement(
      final String in_uri,
      final String in_local_name,
      final String in_q_name,
      final Attributes attributes)
    {
      LOG.trace("startElement: {} {} {} {}",
                in_uri, in_local_name, in_q_name, attributes);

      switch (in_local_name) {
        case "changelog": {
          this.onStartChangelog(attributes);
          break;
        }
        case "changes": {
          this.onStartChanges();
          break;
        }
        case "change": {
          this.onStartChange(attributes);
          break;
        }
        case "releases": {
          this.onStartReleases();
          break;
        }
        case "release": {
          this.onStartRelease(attributes);
          break;
        }
        case "ticket-systems": {
          this.onStartTicketSystems();
          break;
        }
        case "ticket-system": {
          this.onStartTicketSystem(attributes);
          break;
        }
        case "tickets": {
          this.onStartTickets();
          break;
        }
        case "ticket": {
          this.onStartTicket(attributes);
          break;
        }
        default: {
          break;
        }
      }
    }

    private void onStartTicketSystem(
      final Attributes attributes)
    {
      this.elements.push(CurrentElement.TICKET_SYSTEM);
      this.ticket_system_builder.setDefault(false);

      for (int index = 0; index < attributes.getLength(); ++index) {
        switch (attributes.getLocalName(index)) {
          case "id": {
            this.ticket_system_builder.setId(attributes.getValue(index));
            break;
          }
          case "default": {
            this.ticket_system_builder.setDefault(
              Boolean.parseBoolean(attributes.getValue(index)));
            break;
          }
          case "url": {
            this.ticket_system_builder.setUri(
              URI.create(attributes.getValue(index)));
            break;
          }
          default: {
            break;
          }
        }
      }
    }

    private void onStartTicketSystems()
    {
      this.elements.push(CurrentElement.TICKET_SYSTEMS);
    }

    private void onStartTicket(
      final Attributes attributes)
    {
      this.elements.push(CurrentElement.TICKET);

      for (int index = 0; index < attributes.getLength(); ++index) {
        switch (attributes.getLocalName(index)) {
          case "id": {
            this.change_builder.addTickets(
              CTicketID.of(attributes.getValue(index)));
            break;
          }
          default: {
            break;
          }
        }
      }
    }

    private void onStartTickets()
    {
      this.elements.push(CurrentElement.TICKETS);
    }

    private void onStartChange(
      final Attributes attributes)
    {
      this.elements.push(CurrentElement.CHANGE);

      this.change_builder.setModule(Optional.empty());
      this.change_builder.setBackwardsCompatible(true);
      this.change_builder.setTickets(List.of());

      for (int index = 0; index < attributes.getLength(); ++index) {
        switch (attributes.getLocalName(index)) {
          case "module": {
            this.change_builder.setModule(
              CModuleName.of(attributes.getValue(index)));
            break;
          }
          case "date": {
            this.change_builder.setDate(
              ZonedDateTime.of(
                LocalDate.parse(attributes.getValue(index), this.date_format),
                LocalTime.MIDNIGHT,
                ZoneId.of("UTC")));
            break;
          }
          case "summary": {
            this.change_builder.setSummary(attributes.getValue(index));
            break;
          }
          case "compatible": {
            this.change_builder.setBackwardsCompatible(
              Boolean.parseBoolean(attributes.getValue(index)));
            break;
          }
          default: {
            break;
          }
        }
      }
    }

    private void onStartChanges()
    {
      this.elements.push(CurrentElement.CHANGES);
    }

    private void onStartRelease(
      final Attributes attributes)
    {
      this.elements.push(CurrentElement.RELEASE);
      this.release_builder.setChanges(List.of());
      this.release_builder.setOpen(false);

      for (int index = 0; index < attributes.getLength(); ++index) {
        switch (attributes.getLocalName(index)) {
          case "date": {
            this.release_builder.setDate(
              ZonedDateTime.of(
                LocalDate.parse(attributes.getValue(index), this.date_format),
                LocalTime.MIDNIGHT,
                ZoneId.of("UTC")));
            break;
          }
          case "is-open": {
            final var isOpen =
              Boolean.parseBoolean(attributes.getValue(index));
            this.release_builder.setOpen(isOpen);

            if (isOpen) {
              ++this.openCount;
              if (this.openCount > 1) {
                this.error(new SAXParseException(
                  "At most one release is allowed to be open at any given time.",
                  this.locator
                ));
              }
            }
            break;
          }
          case "version": {
            this.release_builder.setVersion(
              CVersions.parse(attributes.getValue(index)));
            break;
          }
          case "ticket-system": {
            this.release_builder.setTicketSystemID(
              attributes.getValue(index));
            break;
          }
          default: {
            break;
          }
        }
      }
    }

    private void onStartReleases()
    {
      this.elements.push(CurrentElement.RELEASES);
    }

    private void onStartChangelog(
      final Attributes attributes)
    {
      this.elements.push(CurrentElement.CHANGELOG);

      for (int index = 0; index < attributes.getLength(); ++index) {
        switch (attributes.getLocalName(index)) {
          case "project": {
            this.changelog_builder.setProject(
              CProjectName.of(attributes.getValue(index)));
            break;
          }
          default: {
            break;
          }
        }
      }
    }

    @Override
    public void endElement(
      final String in_uri,
      final String in_local_name,
      final String in_qname)
    {
      LOG.trace("endElement: {} {} {}", in_uri, in_local_name, in_qname);

      if (this.elements.isEmpty()) {
        return;
      }

      switch (this.elements.peek()) {
        case CHANGELOG: {
          break;
        }
        case RELEASES: {
          break;
        }
        case RELEASE: {
          this.onEndRelease();
          break;
        }
        case CHANGES: {
          break;
        }
        case CHANGE: {
          this.onEndChange();
          break;
        }
        case TICKETS: {
          break;
        }
        case TICKET: {
          break;
        }
        case TICKET_SYSTEMS: {
          break;
        }
        case TICKET_SYSTEM: {
          this.onEndTicketSystem();
          break;
        }
      }

      this.elements.pop();
    }

    private void onEndTicketSystem()
    {
      final CTicketSystem ts = this.ticket_system_builder.build();
      this.changelog_builder.putTicketSystems(ts.id(), ts);
    }

    private void onEndChange()
    {
      final CChange c = this.change_builder.build();
      this.release_builder.addChanges(c);
    }

    private void onEndRelease()
    {
      final CRelease r = this.release_builder.build();
      this.changelog_builder.putReleases(r.version(), r);
    }

    @Override
    public void characters(
      final char[] ch,
      final int start,
      final int length)
    {
      LOG.trace(
        "characters: {} {}",
        Integer.valueOf(start),
        Integer.valueOf(length));
    }

    @Override
    public void ignorableWhitespace(
      final char[] ch,
      final int start,
      final int length)
    {
      LOG.trace(
        "ignorableWhitespace: {} {}",
        Integer.valueOf(start),
        Integer.valueOf(length));
    }

    @Override
    public void processingInstruction(
      final String target,
      final String data)
    {
      LOG.trace("processingInstruction: {} {}", target, data);
    }

    @Override
    public void skippedEntity(
      final String name)
    {
      LOG.trace("skippedEntity: {}", name);
    }

    @Override
    public void warning(
      final SAXParseException e)
    {
      this.receiver.accept(
        CParseError.builder()
          .setLexical(LexicalPosition.<URI>builder()
                        .setLine(e.getLineNumber())
                        .setColumn(e.getColumnNumber())
                        .setFile(this.uri)
                        .build())
          .setSeverity(CParseErrorType.Severity.WARNING)
          .setMessage(e.getMessage())
          .setException(e)
          .build());
    }

    @Override
    public void error(
      final SAXParseException e)
    {
      this.receiver.accept(
        CParseError.builder()
          .setLexical(LexicalPosition.<URI>builder()
                        .setLine(e.getLineNumber())
                        .setColumn(e.getColumnNumber())
                        .setFile(this.uri)
                        .build())
          .setSeverity(CParseErrorType.Severity.ERROR)
          .setMessage(e.getMessage())
          .setException(e)
          .build());

      this.failed = true;
    }

    @Override
    public void fatalError(
      final SAXParseException e)
      throws SAXException
    {
      this.receiver.accept(
        CParseError.builder()
          .setLexical(LexicalPosition.<URI>builder()
                        .setLine(e.getLineNumber())
                        .setColumn(e.getColumnNumber())
                        .setFile(this.uri)
                        .build())
          .setSeverity(CParseErrorType.Severity.CRITICAL)
          .setMessage(e.getMessage())
          .setException(e)
          .build());

      this.failed = true;
      throw e;
    }

    @Override
    public CChangelog parse()
      throws IOException
    {
      try {
        this.parser.setContentHandler(this);
        this.parser.setErrorHandler(this);
        this.parser.parse(new InputSource(this.stream));

        if (this.failed) {
          throw new IOException(
            this.uri + " - At least one error was encountered during parsing and/or validation");
        }
        return this.changelog_builder.build();
      } catch (final SAXException e) {
        throw new IOException(e);
      }
    }

    private enum CurrentElement
    {
      CHANGELOG,
      RELEASES,
      RELEASE,
      CHANGES,
      CHANGE,
      TICKETS,
      TICKET,
      TICKET_SYSTEMS,
      TICKET_SYSTEM
    }
  }
}

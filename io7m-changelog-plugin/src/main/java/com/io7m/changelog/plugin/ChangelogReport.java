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

package com.io7m.changelog.plugin;

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CItem;
import com.io7m.changelog.core.CRelease;
import com.io7m.changelog.xom.CAtomFeedMeta;
import com.io7m.changelog.xom.CChangelogAtomWriter;
import com.io7m.changelog.xom.CChangelogXMLReader;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;
import org.apache.maven.doxia.sink.Sink;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

final class ChangelogReport
{
  private final File feed_file;
  private final CAtomFeedMeta feed_meta;
  private final URI file;
  private final Sink sink;

  ChangelogReport(
    final CAtomFeedMeta in_feed_meta,
    final File in_feed_file,
    final URI in_file,
    final Sink in_sink)
  {
    this.feed_meta = in_feed_meta;
    this.feed_file = in_feed_file;
    this.file = in_file;
    this.sink = in_sink;
  }

  void run()
    throws MalformedURLException,
    ValidityException,
    IOException,
    SAXException,
    ParserConfigurationException,
    ParsingException,
    URISyntaxException,
    ParseException
  {
    final CChangelog c = CChangelogXMLReader.readFromURI(this.file);
    this.write(c);
    this.writeFeed(c);
  }

  private void write(
    final CChangelog c)
  {
    if (c == null) {
      throw new NullPointerException("Changelog");
    }

    this.sink.head();
    this.sink.title();
    this.sink.text("Changes");
    this.sink.title_();
    this.sink.head_();

    this.sink.body();
    this.sink.section1();
    this.sink.sectionTitle1();
    this.sink.text("Changes");
    this.sink.sectionTitle1_();

    this.sink.paragraph();
    this.sink.text("Subscribe to the ");
    this.sink.link("releases.atom");
    this.sink.text("atom feed.");
    this.sink.link_();
    this.sink.paragraph_();

    this.sink.table();

    final Map<String, URI> ticket_systems = c.ticketSystems();

    final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

    for (final CRelease r : c.releases()) {
      this.sink.tableRow();
      this.sink.tableCell();
      this.sink.text(formatter.format(r.date()));
      this.sink.tableCell_();
      this.sink.tableCell();
      this.sink.text(String.format(
        "Release: %s %s",
        c.project(),
        r.version()));
      this.sink.tableCell_();
      this.sink.tableRow_();

      final URI sys = ticket_systems.get(r.ticketSystemID());

      for (final CItem i : r.items()) {
        this.sink.tableRow();
        this.sink.tableCell();
        this.sink.text(formatter.format(i.date()));
        this.sink.tableCell_();
        this.sink.tableCell();

        switch (i.type()) {
          case CHANGE_TYPE_CODE_CHANGE: {
            this.sink.text("Code change: ");
            break;
          }
          case CHANGE_TYPE_CODE_FIX: {
            this.sink.text("Code fix: ");
            break;
          }
          case CHANGE_TYPE_CODE_NEW: {
            this.sink.text("Code new: ");
            break;
          }
        }

        this.sink.text(i.summary());

        final List<String> tickets = i.tickets();
        if (tickets.size() > 0) {
          this.sink.text(" (tickets: ");

          for (int index = 0; index < tickets.size(); ++index) {
            final String ticket = tickets.get(index);

            final StringBuilder ticket_uri = new StringBuilder();
            ticket_uri.append(sys);
            ticket_uri.append(ticket);

            this.sink.link(ticket_uri.toString());
            this.sink.text(ticket);
            this.sink.link_();

            if ((index + 1) < tickets.size()) {
              this.sink.text(", ");
            }
          }

          this.sink.text(")");
        }

        this.sink.tableCell_();
        this.sink.tableRow_();
      }
    }

    this.sink.table_();
    this.sink.section1_();
    this.sink.body_();

    this.sink.flush();
    this.sink.close();
  }

  private void writeFeed(
    final CChangelog c)
    throws IOException
  {
    if (c == null) {
      throw new NullPointerException("Changelog");
    }

    final Element e = CChangelogAtomWriter.writeElement(this.feed_meta, c);
    final FileOutputStream stream = new FileOutputStream(this.feed_file);
    try {
      final Serializer s = new Serializer(stream, "UTF-8");
      s.setIndent(2);
      s.setMaxLength(80);
      s.write(new Document(e));
      s.flush();
    } finally {
      stream.close();
    }
  }
}

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

package com.io7m.changelog.xom;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import nu.xom.Attribute;
import nu.xom.Element;

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CItem;
import com.io7m.changelog.core.CRelease;

/**
 * A changelog XHTML writer.
 */

public final class CChangelogXHTMLWriter
{
  private static final URI XHTML_URI;

  static {
    try {
      XHTML_URI = new URI("http://www.w3.org/1999/xhtml");
    } catch (final URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private CChangelogXHTMLWriter()
  {
    throw new AssertionError("Unreachable");
  }

  /**
   * Serialize the given changelog to XHTML.
   * 
   * @param c
   *          The changelog
   * @return An XHTML changelog
   */

  public static Element writeElement(
    final CChangelog c)
  {
    if (c == null) {
      throw new NullPointerException("Changelog");
    }

    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    final Element et =
      new Element("table", CChangelogXHTMLWriter.XHTML_URI.toString());
    et.addAttribute(new Attribute("class", "changelog"));
    et.addAttribute(new Attribute("summary", String.format(
      "Changes for project %s",
      c.getProject())));

    final Map<String, URI> ticket_systems = c.getTicketSystemsMap();
    final StringBuilder line = new StringBuilder();

    for (final CRelease r : c.getReleases()) {

      {
        final Element etdate =
          new Element("td", CChangelogXHTMLWriter.XHTML_URI.toString());
        etdate.appendChild(df.format(r.getDate()));

        line.setLength(0);
        line.append("Release: ");
        line.append(c.getProject());
        line.append(" ");
        line.append(r.getVersion());

        final Element etrest =
          new Element("td", CChangelogXHTMLWriter.XHTML_URI.toString());
        etrest.appendChild(line.toString());

        final Element etr =
          new Element("tr", CChangelogXHTMLWriter.XHTML_URI.toString());
        etr.appendChild(etdate);
        etr.appendChild(etrest);
        et.appendChild(etr);
      }

      final URI sys = ticket_systems.get(r.getTicketSystemID());

      for (final CItem i : r.getItems()) {

        final Element etdate =
          new Element("td", CChangelogXHTMLWriter.XHTML_URI.toString());
        etdate.appendChild(df.format(i.getDate()));

        final Element etrest =
          new Element("td", CChangelogXHTMLWriter.XHTML_URI.toString());

        switch (i.getType()) {
          case CHANGE_TYPE_CODE_CHANGE:
          {
            etrest.appendChild("Code change: ");
            break;
          }
          case CHANGE_TYPE_CODE_FIX:
          {
            etrest.appendChild("Code fix: ");
            break;
          }
          case CHANGE_TYPE_CODE_NEW:
          {
            etrest.appendChild("Code new: ");
            break;
          }
        }

        etrest.appendChild(i.getSummary());

        final List<String> tickets = i.getTickets();
        if (tickets.size() > 0) {
          etrest.appendChild(" (tickets: ");

          for (int index = 0; index < tickets.size(); ++index) {
            final String ticket = tickets.get(index);

            final StringBuilder ticket_uri = new StringBuilder();
            ticket_uri.append(sys);
            ticket_uri.append(ticket);

            final Attribute attr =
              new Attribute("href", null, ticket_uri.toString());

            final Element a =
              new Element("a", CChangelogXHTMLWriter.XHTML_URI.toString());
            a.addAttribute(attr);
            a.appendChild(ticket);
            etrest.appendChild(a);

            if ((index + 1) < tickets.size()) {
              etrest.appendChild(", ");
            }
          }

          etrest.appendChild(")");
        }

        final Element etr =
          new Element("tr", CChangelogXHTMLWriter.XHTML_URI.toString());
        etr.appendChild(etdate);
        etr.appendChild(etrest);
        et.appendChild(etr);
      }
    }

    return et;
  }
}
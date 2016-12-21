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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import nu.xom.Attribute;
import nu.xom.Element;

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CItem;
import com.io7m.changelog.core.CRelease;
import com.io7m.changelog.core.CVersionType;
import com.io7m.changelog.schema.CSchema;

/**
 * A changelog XML writer.
 */

public final class CChangelogXMLWriter
{
  private static Element item(
    final String uri,
    final DateFormat df,
    final CItem i)
  {
    final Element e = new Element("c:item", uri);

    for (final String t : i.getTickets()) {
      final Element et = new Element("c:ticket", uri);
      et.appendChild(t);
      e.appendChild(et);
    }

    final Element es = new Element("c:summary", uri);
    es.appendChild(i.getSummary());
    final Element ed = new Element("c:date", uri);
    ed.appendChild(df.format(i.getDate()));

    switch (i.getType()) {
      case CHANGE_TYPE_CODE_CHANGE:
      {
        final Element ety = new Element("c:type-code-change", uri);
        e.appendChild(ety);
        break;
      }
      case CHANGE_TYPE_CODE_FIX:
      {
        final Element ety = new Element("c:type-code-fix", uri);
        e.appendChild(ety);
        break;
      }
      case CHANGE_TYPE_CODE_NEW:
      {
        final Element ety = new Element("c:type-code-new", uri);
        e.appendChild(ety);
        break;
      }
    }

    e.appendChild(es);
    e.appendChild(ed);
    return e;
  }

  private static Element release(
    final String uri,
    final DateFormat df,
    final CRelease r)
  {
    final Date d = r.getDate();
    final CVersionType v = r.getVersion();
    final Attribute at =
      new Attribute("c:ticket-system", uri, r.getTicketSystemID());

    final Element ed = new Element("c:date", uri);
    ed.appendChild(df.format(d));
    final Element ev = new Element("c:version", uri);
    ev.appendChild(v.toString());

    final Element er = new Element("c:release", uri);
    er.addAttribute(at);
    er.appendChild(ed);
    er.appendChild(ev);

    final List<CItem> items = r.getItems();
    for (final CItem i : items) {
      er.appendChild(CChangelogXMLWriter.item(uri, df, i));
    }
    return er;
  }

  private static Element ticketSystem(
    final String uri,
    final Map<String, URI> m,
    final String id)
  {
    final Attribute aid =
      new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", id);
    final Element et = new Element("c:ticket-system", uri);
    et.addAttribute(aid);
    final Element etu = new Element("c:ticket-url", uri);
    etu.appendChild(m.get(id).toString());
    et.appendChild(etu);
    return et;
  }

  /**
   * Serialize the given changelog to XML.
   * 
   * @param c
   *          The changelog
   * @return An XML changelog
   */

  public static Element writeElement(
    final CChangelog c)
  {
    if (c == null) {
      throw new NullPointerException("Changelog");
    }

    final String uri = CSchema.XML_URI.toString();
    final Element e = new Element("c:changelog", uri);

    {
      final Element ep = new Element("c:project", uri);
      ep.appendChild(c.getProject());
      e.appendChild(ep);
    }

    {
      final Map<String, URI> m = c.getTicketSystemsMap();
      for (final String id : m.keySet()) {
        final Element et = CChangelogXMLWriter.ticketSystem(uri, m, id);
        e.appendChild(et);
      }
    }

    {
      final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
      for (final CRelease r : c.getReleases()) {
        final Element er = CChangelogXMLWriter.release(uri, df, r);
        e.appendChild(er);
      }
    }

    return e;
  }

  private CChangelogXMLWriter()
  {
    throw new AssertionError("Unreachable");
  }
}

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

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CItem;
import com.io7m.changelog.core.CRelease;
import com.io7m.changelog.core.CVersionType;
import com.io7m.changelog.schema.CSchema;
import nu.xom.Attribute;
import nu.xom.Element;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * A changelog XML writer.
 */

public final class CChangelogXMLWriter
{
  private CChangelogXMLWriter()
  {
    throw new AssertionError("Unreachable");
  }

  private static Element item(
    final String uri,
    final DateTimeFormatter df,
    final CItem i)
  {
    final Element e = new Element("c:item", uri);

    for (final String t : i.tickets()) {
      final Element et = new Element("c:ticket", uri);
      et.appendChild(t);
      e.appendChild(et);
    }

    final Element es = new Element("c:summary", uri);
    es.appendChild(i.summary());
    final Element ed = new Element("c:date", uri);
    ed.appendChild(df.format(i.date()));

    switch (i.type()) {
      case CHANGE_TYPE_CODE_CHANGE: {
        final Element ety = new Element("c:type-code-change", uri);
        e.appendChild(ety);
        break;
      }
      case CHANGE_TYPE_CODE_FIX: {
        final Element ety = new Element("c:type-code-fix", uri);
        e.appendChild(ety);
        break;
      }
      case CHANGE_TYPE_CODE_NEW: {
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
    final DateTimeFormatter df,
    final CRelease r)
  {
    final LocalDate d = r.date();
    final CVersionType v = r.version();
    final Attribute at =
      new Attribute("c:ticket-system", uri, r.ticketSystemID());

    final Element ed = new Element("c:date", uri);
    ed.appendChild(df.format(d));
    final Element ev = new Element("c:version", uri);
    ev.appendChild(v.toVersionString());

    final Element er = new Element("c:release", uri);
    er.addAttribute(at);
    er.appendChild(ed);
    er.appendChild(ev);

    final List<CItem> items = r.items();
    for (final CItem i : items) {
      er.appendChild(item(uri, df, i));
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
   * @param c The changelog
   *
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
      ep.appendChild(c.project());
      e.appendChild(ep);
    }

    {
      final Map<String, URI> m = c.ticketSystems();
      for (final String id : m.keySet()) {
        final Element et = ticketSystem(uri, m, id);
        e.appendChild(et);
      }
    }

    {
      final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      for (final CRelease r : c.releases()) {
        final Element er = release(uri, df, r);
        e.appendChild(er);
      }
    }

    return e;
  }
}

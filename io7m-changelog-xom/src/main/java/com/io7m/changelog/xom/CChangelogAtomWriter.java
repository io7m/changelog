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
import com.io7m.changelog.core.CRelease;
import nu.xom.Attribute;
import nu.xom.Element;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A changelog Atom feed writer.
 */

public final class CChangelogAtomWriter
{
  private static final URI XHTML_URI;
  private static final URI ATOM_URI;

  static {
    try {
      XHTML_URI = new URI("http://www.w3.org/1999/xhtml");
      ATOM_URI = new URI("http://www.w3.org/2005/Atom");
    } catch (final URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private CChangelogAtomWriter()
  {
    throw new AssertionError("Unreachable");
  }

  /**
   * Serialize the given changelog to Atom.
   *
   * @param meta Information about the feed
   * @param c    The changelog
   *
   * @return An Atom feed
   */

  public static Element writeElement(
    final CAtomFeedMeta meta,
    final CChangelog c)
  {
    if (c == null) {
      throw new NullPointerException("Changelog");
    }

    final Element ef =
      new Element("a:feed", ATOM_URI.toString());

    {
      final Element ea = authorMeta(meta);
      ef.appendChild(ea);
    }

    {
      final Element ei = feedId(meta);
      final Element et = feedTitle(meta);
      ef.appendChild(ei);
      ef.appendChild(et);
    }

    for (int index = 0; index < c.releases().size(); ++index) {
      final CRelease r = c.releases().get(index);

      final LocalDate date = r.date();
      final LocalDateTime date_time = date.atStartOfDay();

      if (index == 0) {
        final Element eu =
          new Element("a:updated", ATOM_URI.toString());
        eu.appendChild(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(date_time));
        ef.appendChild(eu);
      }

      final Element er =
        new Element("a:entry", ATOM_URI.toString());

      {
        final Element ei =
          new Element("a:id", ATOM_URI.toString());
        ei.appendChild(Integer.toString(index));
        er.appendChild(ei);
      }

      {
        final Element eu =
          new Element("a:updated", ATOM_URI.toString());
        eu.appendChild(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(date_time));
        er.appendChild(eu);
      }

      {
        final Element ep =
          new Element("a:published", ATOM_URI.toString());
        ep.appendChild(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(date_time));
        er.appendChild(ep);
      }

      {
        final Element et = releaseTitle(c, r);
        er.appendChild(et);
      }

      {
        final Element ec =
          new Element("a:content", ATOM_URI.toString());
        ec.addAttribute(new Attribute("type", "text"));

        final StringBuilder text = new StringBuilder();
        text.append("\n");
        text.append(c.project());
        text.append(" ");
        text.append(r.version().toVersionString());
        text.append(" released\n");
        text.append("\n");
        ec.appendChild(text.toString());
        er.appendChild(ec);
      }

      ef.appendChild(er);
    }

    return ef;
  }

  private static Element releaseTitle(
    final CChangelog c,
    final CRelease r)
  {
    final Element et =
      new Element("a:title", ATOM_URI.toString());
    final StringBuilder text = new StringBuilder();
    text.append(c.project());
    text.append(" ");
    text.append(r.version());
    text.append(" released");

    et.appendChild(text.toString());
    return et;
  }

  private static Element feedTitle(
    final CAtomFeedMeta meta)
  {
    final Element et =
      new Element("a:title", ATOM_URI.toString());
    et.appendChild(meta.title());
    return et;
  }

  private static Element feedId(
    final CAtomFeedMeta meta)
  {
    final Element ei =
      new Element("a:id", ATOM_URI.toString());
    ei.appendChild(meta.uri().toString());
    return ei;
  }

  private static Element authorMeta(
    final CAtomFeedMeta meta)
  {
    final Element ea =
      new Element("a:author", ATOM_URI.toString());
    final Element ean =
      new Element("a:name", ATOM_URI.toString());
    ean.appendChild(meta.authorName());
    final Element eae =
      new Element("a:email", ATOM_URI.toString());
    eae.appendChild(meta.authorEmail());

    ea.appendChild(ean);
    ea.appendChild(eae);
    return ea;
  }
}

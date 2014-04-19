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

import nu.xom.Attribute;
import nu.xom.Element;

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CRelease;

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
   * @param meta
   *          Information about the feed
   * @param c
   *          The changelog
   * @return An Atom feed
   */

  public static Element writeElement(
    final CAtomFeedMeta meta,
    final CChangelog c)
  {
    if (c == null) {
      throw new NullPointerException("Changelog");
    }

    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

    final Element ef =
      new Element("a:feed", CChangelogAtomWriter.ATOM_URI.toString());

    {
      final Element ea = CChangelogAtomWriter.authorMeta(meta);
      ef.appendChild(ea);
    }

    {
      final Element ei = CChangelogAtomWriter.feedId(meta);
      final Element et = CChangelogAtomWriter.feedTitle(meta);
      ef.appendChild(ei);
      ef.appendChild(et);
    }

    for (int index = 0; index < c.getReleases().size(); ++index) {
      final CRelease r = c.getReleases().get(index);

      if (index == 0) {
        final Element eu =
          new Element("a:updated", CChangelogAtomWriter.ATOM_URI.toString());
        eu.appendChild(df.format(r.getDate()));
        ef.appendChild(eu);
      }

      final Element er =
        new Element("a:entry", CChangelogAtomWriter.ATOM_URI.toString());

      {
        final Element ei =
          new Element("a:id", CChangelogAtomWriter.ATOM_URI.toString());
        ei.appendChild(Integer.toString(index));
        er.appendChild(ei);
      }

      {
        final Element eu =
          new Element("a:updated", CChangelogAtomWriter.ATOM_URI.toString());
        eu.appendChild(df.format(r.getDate()));
        er.appendChild(eu);
      }

      {
        final Element ep =
          new Element("a:published", CChangelogAtomWriter.ATOM_URI.toString());
        ep.appendChild(df.format(r.getDate()));
        er.appendChild(ep);
      }

      {
        final Element et = CChangelogAtomWriter.releaseTitle(c, r);
        er.appendChild(et);
      }

      {
        final Element ec =
          new Element("a:content", CChangelogAtomWriter.ATOM_URI.toString());
        ec.addAttribute(new Attribute("type", "text"));

        final StringBuilder text = new StringBuilder();
        text.append("\n");
        text.append(c.getProject());
        text.append(" ");
        text.append(r.getVersion());
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
      new Element("a:title", CChangelogAtomWriter.ATOM_URI.toString());
    final StringBuilder text = new StringBuilder();
    text.append(c.getProject());
    text.append(" ");
    text.append(r.getVersion());
    text.append(" released");

    et.appendChild(text.toString());
    return et;
  }

  private static Element feedTitle(
    final CAtomFeedMeta meta)
  {
    final Element et =
      new Element("a:title", CChangelogAtomWriter.ATOM_URI.toString());
    et.appendChild(meta.getTitle());
    return et;
  }

  private static Element feedId(
    final CAtomFeedMeta meta)
  {
    final Element ei =
      new Element("a:id", CChangelogAtomWriter.ATOM_URI.toString());
    ei.appendChild(meta.getURI().toString());
    return ei;
  }

  private static Element authorMeta(
    final CAtomFeedMeta meta)
  {
    final Element ea =
      new Element("a:author", CChangelogAtomWriter.ATOM_URI.toString());
    final Element ean =
      new Element("a:name", CChangelogAtomWriter.ATOM_URI.toString());
    ean.appendChild(meta.getAuthorName());
    final Element eae =
      new Element("a:email", CChangelogAtomWriter.ATOM_URI.toString());
    eae.appendChild(meta.getAuthorEmail());

    ea.appendChild(ean);
    ea.appendChild(eae);
    return ea;
  }
}

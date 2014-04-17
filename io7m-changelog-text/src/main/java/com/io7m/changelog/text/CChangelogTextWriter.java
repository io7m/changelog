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

package com.io7m.changelog.text;

import java.io.PrintWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CItem;
import com.io7m.changelog.core.CRelease;

/**
 * A plain-text changelog serializer.
 */

public final class CChangelogTextWriter
{
  /**
   * Serialize the given changelog to plain text.
   * 
   * @param c
   *          The changelog
   * @param out
   *          The writer
   */

  public static void writeChangelog(
    final CChangelog c,
    final PrintWriter out)
  {
    if (c == null) {
      throw new NullPointerException("Changelog");
    }
    if (out == null) {
      throw new NullPointerException("Output");
    }

    final Map<String, URI> ticket_systems = c.getTicketSystemsMap();
    final StringBuilder line = new StringBuilder();
    final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    for (final CRelease r : c.getReleases()) {
      line.setLength(0);
      line.append(df.format(r.getDate()));
      line.append(" Release: ");
      line.append(c.getProject());
      line.append(" ");
      line.append(r.getVersion());
      out.println(line.toString());

      final URI sys = ticket_systems.get(r.getTicketSystemID());

      for (final CItem i : r.getItems()) {

        line.setLength(0);
        line.append(df.format(r.getDate()));
        line.append(" ");

        switch (i.getType()) {
          case CHANGE_TYPE_CODE_CHANGE:
          {
            line.append("Code change: ");
            break;
          }
          case CHANGE_TYPE_CODE_FIX:
          {
            line.append("Code fix: ");
            break;
          }
          case CHANGE_TYPE_CODE_NEW:
          {
            line.append("Code new: ");
            break;
          }
        }

        line.append(i.getSummary());

        final List<String> tickets = i.getTickets();
        if (tickets.size() > 0) {
          line.append(" (tickets: ");
          for (int index = 0; index < tickets.size(); ++index) {
            final String ticket = tickets.get(index);
            line.append(sys);
            line.append(ticket);
            if ((index + 1) < tickets.size()) {
              line.append(", ");
            }
          }
          line.append(")");
        }

        out.println(line.toString());
      }
    }

    out.flush();
  }

  private CChangelogTextWriter()
  {
    throw new AssertionError("Unreachable");
  }
}

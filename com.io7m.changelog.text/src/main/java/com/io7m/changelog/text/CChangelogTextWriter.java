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

package com.io7m.changelog.text;

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CItem;
import com.io7m.changelog.core.CRelease;
import com.io7m.changelog.core.CVersionType;

import java.io.PrintWriter;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A plain-text changelog serializer.
 */

public final class CChangelogTextWriter
{
  private CChangelogTextWriter()
  {
    throw new AssertionError("Unreachable");
  }

  /**
   * Serialize the given changelog to plain text.
   *
   * @param c      The changelog
   * @param config The writer configuration
   * @param out    The writer
   */

  public static void writeChangelog(
    final CChangelog c,
    final CChangelogTextWriterConfiguration config,
    final PrintWriter out)
  {
    Objects.requireNonNull(c, "Changelog");
    Objects.requireNonNull(config, "Configuration");
    Objects.requireNonNull(out, "Output");

    final StringBuilder line = new StringBuilder();
    for (final CRelease r : c.releases()) {
      final Optional<CVersionType> release_opt = config.release();

      final boolean show;
      if (release_opt.isPresent()) {
        show = Objects.equals(r.version(), release_opt.get());
      } else {
        show = true;
      }

      if (show) {
        showRelease(c, config, out, line, r);
      }
    }

    out.flush();
  }

  private static void showRelease(
    final CChangelog c,
    final CChangelogTextWriterConfiguration config,
    final PrintWriter out,
    final StringBuilder line,
    final CRelease r)
  {
    final Map<String, URI> ticket_systems = c.ticketSystems();
    line.setLength(0);
    if (config.showDates()) {
      line.append(DateTimeFormatter.ISO_DATE.format(r.date()));
      line.append(" ");
    }

    line.append("Release: ");
    line.append(c.project());
    line.append(" ");
    line.append(r.version().toVersionString());
    out.println(line.toString());

    final URI sys = ticket_systems.get(r.ticketSystemID());

    for (final CItem i : r.items()) {

      line.setLength(0);
      if (config.showDates()) {
        line.append(DateTimeFormatter.ISO_DATE.format(r.date()));
        line.append(" ");
      }

      switch (i.type()) {
        case CHANGE_TYPE_CODE_CHANGE: {
          line.append("Code change: ");
          break;
        }
        case CHANGE_TYPE_CODE_FIX: {
          line.append("Code fix: ");
          break;
        }
        case CHANGE_TYPE_CODE_NEW: {
          line.append("Code new: ");
          break;
        }
      }

      line.append(i.summary());

      final List<String> tickets = i.tickets();
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
}

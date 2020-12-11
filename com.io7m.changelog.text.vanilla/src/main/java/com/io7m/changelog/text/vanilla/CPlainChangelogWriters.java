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

package com.io7m.changelog.text.vanilla;

import com.io7m.changelog.core.CChange;
import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CRelease;
import com.io7m.changelog.core.CVersion;
import com.io7m.changelog.text.api.CPlainChangelogWriterConfiguration;
import com.io7m.changelog.text.api.CPlainChangelogWriterProviderType;
import com.io7m.changelog.text.api.CPlainChangelogWriterType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A provider of plain-text changelog writers.
 */

public final class CPlainChangelogWriters
  implements CPlainChangelogWriterProviderType
{
  /**
   * Instantiate a writer provider.
   */

  public CPlainChangelogWriters()
  {

  }

  @Override
  public CPlainChangelogWriterType createWithConfiguration(
    final CPlainChangelogWriterConfiguration config,
    final URI uri,
    final OutputStream stream)
  {
    Objects.requireNonNull(config, "Configuration");
    Objects.requireNonNull(uri, "URI");
    Objects.requireNonNull(stream, "Stream");
    return new Writer(config, stream);
  }

  private static final class Writer implements CPlainChangelogWriterType
  {
    private final BufferedWriter writer;
    private final DateTimeFormatter date_formatter;
    private final CPlainChangelogWriterConfiguration config;

    Writer(
      final CPlainChangelogWriterConfiguration in_config,
      final OutputStream in_stream)
    {
      this.config =
        Objects.requireNonNull(in_config, "Config");
      this.writer = new BufferedWriter(
        new OutputStreamWriter(in_stream, StandardCharsets.UTF_8));
      this.date_formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    @Override
    public void write(
      final CChangelog changelog)
      throws IOException
    {
      try {
        final Map<CVersion, CRelease> releases = changelog.releases();
        final List<CVersion> versions =
          releases.keySet()
            .stream()
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());

        for (final CVersion v : versions) {
          final CRelease release = releases.get(v);
          this.writeRelease(changelog, release);
        }

        this.writer.flush();
      } catch (final UncheckedIOException e) {
        throw e.getCause();
      }
    }

    private void writeRelease(
      final CChangelog changelog,
      final CRelease release)
      throws IOException
    {
      if (this.config.showDates()) {
        this.writer.append(this.date_formatter.format(release.date()));
        this.writer.append(" ");
      }

      this.writer.append("Release: ");
      this.writer.append(changelog.project().value());
      this.writer.append(" ");
      this.writer.append(String.format("%s", release.version()));
      this.writer.newLine();

      final var changes =
        release.changes()
          .stream()
          .sorted(Comparator.comparing(CChange::date).reversed())
          .collect(Collectors.toList());

      try {
        changes.forEach(this::writeChange);
      } catch (final UncheckedIOException e) {
        throw e.getCause();
      }
    }

    private void writeChange(
      final CChange change)
    {
      try {
        if (this.config.showDates()) {
          this.writer.append(this.date_formatter.format(change.date()));
          this.writer.append(" ");
        }

        this.writer.append("Change: ");
        change.module().ifPresent(module -> {
          try {
            this.writer.append(module.value());
            this.writer.append(": ");
          } catch (final IOException e) {
            throw new UncheckedIOException(e);
          }
        });

        if (!change.backwardsCompatible()) {
          this.writer.append("(Backwards incompatible) ");
        }

        this.writer.append(change.summary());

        if (!change.tickets().isEmpty()) {
          if (change.tickets().size() == 1) {
            this.writer.append(" (Ticket:");
          } else {
            this.writer.append(" (Tickets:");
          }

          this.writer.append(
            change.tickets()
              .stream()
              .map(t -> String.format(" #%s", t.value()))
              .collect(Collectors.joining(",")));
          this.writer.append(")");
        }

        this.writer.newLine();
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }
}

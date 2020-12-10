/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> http://io7m.com
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

package com.io7m.changelog.tests.core;

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CProjectName;
import com.io7m.changelog.core.CRelease;
import com.io7m.changelog.core.CTicketSystem;
import com.io7m.changelog.core.CVersion;
import com.io7m.changelog.core.CVersions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class CChangelogTest
{
  @Test
  public void testEmpty()
  {
    final var c =
      CChangelog.builder()
        .setProject(CProjectName.of("changelog"))
        .build();

    assertEquals(0, c.ticketSystems().size());
    assertEquals(0, c.releases().size());
    assertEquals(Optional.empty(), c.findTicketSystem(Optional.empty()));
  }

  @Test
  public void testNextVersionEmpty()
  {
    final var c =
      CChangelog.builder()
        .setProject(CProjectName.of("changelog"))
        .build();

    assertEquals("1.0.0", String.format("%s", c.suggestNextRelease()));
  }

  @Test
  public void testNextVersionSemantic()
  {
    final var version =
      CVersions.parse("1.0.0");

    final var release =
      CRelease.builder()
        .setVersion(version)
        .setDate(ZonedDateTime.now(Clock.systemUTC()))
        .setTicketSystemID("x")
        .setOpen(false)
        .build();

    final var ticketSystem =
      CTicketSystem.builder()
        .setDefault(true)
        .setId("x")
        .setUri(URI.create("http://www.example.com"))
        .build();

    final var c =
      CChangelog.builder()
        .setProject(CProjectName.of("changelog"))
        .putReleases(version, release)
        .putTicketSystems("x", ticketSystem)
        .build();

    final var next =
      (CVersion) c.suggestNextRelease();

    assertEquals(1, next.major().intValue());
    assertEquals(1, next.minor().intValue());
    assertEquals(0, next.patch().intValue());
  }

  @Test
  public void testFindTicketSystemDefault()
  {
    final var version =
      CVersions.parse("1.0.0");

    final var release =
      CRelease.builder()
        .setVersion(version)
        .setDate(ZonedDateTime.now(Clock.systemUTC()))
        .setTicketSystemID("x")
        .setOpen(false)
        .build();

    final var ticketSystem0 =
      CTicketSystem.builder()
        .setDefault(true)
        .setId("x")
        .setUri(URI.create("http://www.example.com"))
        .build();

    final var ticketSystem1 =
      CTicketSystem.builder()
        .setDefault(false)
        .setId("y")
        .setUri(URI.create("http://www.example.com"))
        .build();

    final var c =
      CChangelog.builder()
        .setProject(CProjectName.of("changelog"))
        .putReleases(version, release)
        .putTicketSystems("x", ticketSystem0)
        .putTicketSystems("y", ticketSystem1)
        .build();

    assertEquals(
      Optional.of(ticketSystem0.id()),
      c.findTicketSystem(Optional.empty())
    );
    assertEquals(
      Optional.of(ticketSystem0.id()),
      c.findTicketSystem(Optional.of("x"))
    );
    assertEquals(
      Optional.of(ticketSystem1.id()),
      c.findTicketSystem(Optional.of("y"))
    );
  }
}

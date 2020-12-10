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

package com.io7m.changelog.cmdline.internal;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CRelease;
import com.io7m.changelog.core.CVersion;
import com.io7m.changelog.core.CVersions;
import com.io7m.changelog.parser.api.CParseErrorHandlers;
import com.io7m.changelog.xml.api.CXMLChangelogParserProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogWriterProviderType;
import com.io7m.claypot.core.CLPCommandContextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.ServiceLoader;

import static com.io7m.claypot.core.CLPCommandType.Status.FAILURE;
import static com.io7m.claypot.core.CLPCommandType.Status.SUCCESS;

@Parameters(commandDescription = "Start the development of a new release.")
public final class CLCommandReleaseBegin extends CLAbstractCommand
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CLCommandReleaseBegin.class);

  @Parameter(
    names = "--file",
    required = false,
    description = "The changelog file")
  private Path path = Paths.get("README-CHANGES.xml");

  @Parameter(
    names = "--version",
    required = false,
    description = "The new release version")
  private String versionText;

  @Parameter(
    names = "--ticket-system",
    description = "The new release ticket system name")
  private String ticketSystem;

  /**
   * Construct a command.
   *
   * @param inContext The command context
   */

  public CLCommandReleaseBegin(
    final CLPCommandContextType inContext)
  {
    super(LOG, inContext);
  }

  @Override
  public Status executeActual()
    throws Exception
  {
    final var parsersOpt =
      ServiceLoader.load(CXMLChangelogParserProviderType.class).findFirst();

    if (parsersOpt.isEmpty()) {
      LOG.error("No XML parser providers are available");
      return FAILURE;
    }

    final var writersOpt =
      ServiceLoader.load(CXMLChangelogWriterProviderType.class).findFirst();

    if (writersOpt.isEmpty()) {
      LOG.error("No XML writer providers are available");
      return FAILURE;
    }

    final var parsers =
      parsersOpt.get();
    final var writers =
      writersOpt.get();

    final var pathTemp =
      Paths.get(this.path + ".tmp");
    final var changelog =
      parsers.parse(this.path, CParseErrorHandlers.loggingHandler(LOG));

    final CVersion nextVersion;
    if (this.versionText == null) {
      nextVersion = changelog.suggestNextRelease();
    } else {
      nextVersion = CVersions.parse(this.versionText);
    }

    final Optional<String> ticketSystemOpt =
      changelog.findTicketSystem(Optional.ofNullable(this.ticketSystem));

    if (ticketSystemOpt.isEmpty()) {
      if (this.ticketSystem != null) {
        LOG.error("No ticket system named {} is defined", this.ticketSystem);
      } else {
        LOG.error("No default ticket system is available");
      }
      return FAILURE;
    }

    final var releases = changelog.releases();
    if (releases.containsKey(nextVersion)) {
      LOG.error("A release with version {} already exists", String.format("%s", nextVersion));
      return FAILURE;
    }

    final var openRelease =
      releases.values()
        .stream()
        .filter(CRelease::isOpen)
        .findFirst();

    if (openRelease.isPresent()) {
      LOG.error("A release with version {} is already open", String.format("%s", openRelease.get().version()));
      return FAILURE;
    }

    final var release =
      CRelease.builder()
        .setOpen(true)
        .setTicketSystemID(ticketSystemOpt.get())
        .setDate(ZonedDateTime.now(Clock.systemUTC()))
        .setVersion(nextVersion)
        .build();

    final var newChangelog =
      CChangelog.builder()
        .from(changelog)
        .putReleases(release.version(), release)
        .build();

    writers.write(this.path, pathTemp, newChangelog);
    return SUCCESS;
  }

  @Override
  public String name()
  {
    return "release-begin";
  }

  @Override
  public String extendedHelp()
  {
    return this.messages().format("helpReleaseBegin");
  }
}

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
import com.io7m.changelog.core.CChange;
import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CModuleName;
import com.io7m.changelog.core.CRelease;
import com.io7m.changelog.core.CTicketID;
import com.io7m.changelog.parser.api.CParseErrorHandlers;
import com.io7m.changelog.xml.api.CXMLChangelogParserProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogWriterProviderType;
import com.io7m.claypot.core.CLPCommandContextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

@Parameters(commandDescription = "Add a change to the current release")
public final class CLCommandChangeAdd extends CLAbstractCommand
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CLCommandChangeAdd.class);

  @Parameter(
    names = "--file",
    required = false,
    description = "The changelog file")
  private Path path = Paths.get("README-CHANGES.xml");

  @Parameter(
    names = "--summary",
    required = true,
    description = "The change summary")
  private String summary;

  @Parameter(
    names = "--module",
    required = false,
    converter = CModuleNameConverter.class,
    description = "The affected module")
  private CModuleName module;

  @Parameter(
    names = "--ticket",
    required = false,
    converter = CTicketIDConverter.class,
    description = "The list of tickets (Can be specified multiple times)")
  private List<CTicketID> tickets = new ArrayList<>();

  @Parameter(
    names = "--incompatible",
    required = false,
    description = "Indicates that the change is backwards incompatible")
  private boolean incompatible;

  /**
   * Construct a command.
   *
   * @param inContext The command context
   */

  public CLCommandChangeAdd(
    final CLPCommandContextType inContext)
  {
    super(LOG, inContext);
  }

  @Override
  public String name()
  {
    return "change-add";
  }

  @Override
  public String extendedHelp()
  {
    return this.messages().format("helpChangeAdd");
  }

  @Override
  public Status executeActual()
    throws Exception
  {
    final var parsersOpt =
      ServiceLoader.load(CXMLChangelogParserProviderType.class).findFirst();

    if (parsersOpt.isEmpty()) {
      LOG.error("No XML parser providers are available");
      return Status.FAILURE;
    }

    final var writersOpt =
      ServiceLoader.load(CXMLChangelogWriterProviderType.class).findFirst();

    if (writersOpt.isEmpty()) {
      LOG.error("No XML writer providers are available");
      return Status.FAILURE;
    }

    final var parsers = parsersOpt.get();
    final var writers = writersOpt.get();

    final var changelog =
      parsers.parse(this.path, CParseErrorHandlers.loggingHandler(LOG));

    final var latest =
      changelog.latestRelease();

    if (latest.isEmpty()) {
      LOG.error("No current release exists.");
      return Status.FAILURE;
    }

    final var now =
      ZonedDateTime.now(ZoneId.of("UTC"));

    final var change =
      CChange.builder()
        .setModule(Optional.ofNullable(this.module))
        .setBackwardsCompatible(!this.incompatible)
        .setDate(now)
        .setSummary(this.summary)
        .setTickets(List.copyOf(this.tickets))
        .build();

    final var release = latest.get();
    if (!release.isOpen()) {
      LOG.error("The current release is not open for modification.");
      return Status.FAILURE;
    }

    final var releaseWrite =
      CRelease.builder()
        .from(release)
        .addChanges(change)
        .setDate(now)
        .build();

    final var changelogWrite =
      CChangelog.builder()
        .from(changelog)
        .putReleases(releaseWrite.version(), releaseWrite)
        .build();

    final var pathTemp = Paths.get(this.path + ".tmp");
    writers.write(this.path, pathTemp, changelogWrite);
    return Status.SUCCESS;
  }
}

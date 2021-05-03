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
import com.io7m.changelog.core.CVersions;
import com.io7m.changelog.parser.api.CParseErrorHandlers;
import com.io7m.changelog.xml.api.CXMLChangelogParserProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogWriterProviderType;
import com.io7m.claypot.core.CLPCommandContextType;
import com.io7m.jaffirm.core.Invariants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * The "release-finish" command.
 */

@Parameters(commandDescription = "Finish a release.")
public final class CLCommandReleaseFinish extends CLAbstractCommand
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CLCommandReleaseFinish.class);

  @Parameter(
    names = "--file",
    required = false,
    description = "The changelog file")
  private Path path = Paths.get("README-CHANGES.xml");

  @Parameter(
    names = "--version",
    required = false,
    description = "The target release version")
  private String versionText;

  /**
   * Construct a command.
   *
   * @param inContext The command context
   */

  public CLCommandReleaseFinish(
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
      return Status.FAILURE;
    }

    final var writersOpt =
      ServiceLoader.load(CXMLChangelogWriterProviderType.class).findFirst();

    if (writersOpt.isEmpty()) {
      LOG.error("No XML writer providers are available");
      return Status.FAILURE;
    }

    final var parsers =
      parsersOpt.get();
    final var writers =
      writersOpt.get();

    final var pathTemp =
      Paths.get(this.path + ".tmp");
    final var changelog =
      parsers.parse(this.path, CParseErrorHandlers.loggingHandler(LOG));

    final var targetVersionOpt =
      Optional.ofNullable(this.versionText)
        .map(CVersions::parse);

    final var targetReleaseOpt =
      changelog.findTargetReleaseOrLatestOpen(targetVersionOpt);

    if (targetReleaseOpt.isEmpty()) {
      LOG.error("No release is currently open");
      return Status.FAILURE;
    }

    final var targetRelease = targetReleaseOpt.get();
    Invariants.checkInvariant(targetRelease.isOpen(), "Release must be open");

    final var closedRelease =
      CRelease.builder()
        .from(targetRelease)
        .setOpen(false)
        .setDate(ZonedDateTime.now(Clock.systemUTC()))
        .build();

    final var newChangelog =
      CChangelog.builder()
        .from(changelog)
        .putReleases(closedRelease.version(), closedRelease)
        .build();

    writers.write(this.path, pathTemp, newChangelog);
    return Status.SUCCESS;
  }

  @Override
  public String name()
  {
    return "release-finish";
  }

  @Override
  public String extendedHelp()
  {
    return this.messages().format("helpReleaseFinish");
  }
}

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.ServiceLoader;

@Parameters(commandDescription = "Set the version number of the current release.")
public final class CLCommandReleaseSetVersion extends CLAbstractCommand
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CLCommandReleaseSetVersion.class);

  @Parameter(
    names = "--file",
    required = false,
    description = "The changelog file")
  private Path path = Paths.get("README-CHANGES.xml");

  @Parameter(
    names = "--version",
    required = true,
    description = "The version to which to set the current release")
  private String versionText;

  /**
   * Construct a command.
   *
   * @param inContext The command context
   */

  public CLCommandReleaseSetVersion(
    final CLPCommandContextType inContext)
  {
    super(LOG, inContext);
  }

  @Override
  public Status executeActual()
    throws Exception
  {
    final var version =
      CVersions.parse(this.versionText);

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

    final var currentReleaseOpt = changelog.latestRelease();
    if (currentReleaseOpt.isEmpty()) {
      LOG.error("No release is currently open");
      return Status.FAILURE;
    }

    final var currentRelease = currentReleaseOpt.get();
    if (!currentRelease.isOpen()) {
      LOG.error("No release is currently open");
      return Status.FAILURE;
    }

    final var updatedRelease =
      CRelease.builder()
        .from(currentRelease)
        .setDate(ZonedDateTime.now(Clock.systemUTC()))
        .setVersion(version)
        .build();

    final var oldReleases = new HashMap<>(changelog.releases());
    oldReleases.remove(currentRelease.version());
    oldReleases.put(version, updatedRelease);

    final var newChangelog =
      CChangelog.builder()
        .from(changelog)
        .setReleases(oldReleases)
        .build();

    writers.write(this.path, pathTemp, newChangelog);
    return Status.SUCCESS;
  }

  @Override
  public String name()
  {
    return "release-set-version";
  }

  @Override
  public String extendedHelp()
  {
    return this.messages().format("helpReleaseSetVersion");
  }
}

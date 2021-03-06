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
import com.io7m.changelog.core.CChangelogFilters;
import com.io7m.changelog.core.CVersion;
import com.io7m.changelog.core.CVersions;
import com.io7m.changelog.parser.api.CParseErrorHandlers;
import com.io7m.changelog.xml.api.CXHTMLChangelogWriterProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogParserProviderType;
import com.io7m.claypot.core.CLPCommandContextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * The "write-xhtml" command.
 */

@Parameters(commandDescription = "Generate an XHTML log.")
public final class CLCommandWriteXHTML extends CLAbstractCommand
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CLCommandWriteXHTML.class);

  @Parameter(
    names = "--file",
    required = false,
    description = "The changelog file")
  private Path path = Paths.get("README-CHANGES.xml");

  @Parameter(
    names = "--version",
    description = "The version for which to produce output")
  private String versionText;

  @Parameter(
    names = "--count",
    required = false,
    description = "The total number of releases to display")
  private int count = Integer.MAX_VALUE;

  /**
   * Construct a command.
   *
   * @param inContext The command context
   */

  public CLCommandWriteXHTML(
    final CLPCommandContextType inContext)
  {
    super(LOG, inContext);
  }

  @Override
  public Status executeActual()
    throws Exception
  {
    final Optional<CVersion> version;
    if (this.versionText != null) {
      version = Optional.of(CVersions.parse(this.versionText));
    } else {
      version = Optional.empty();
    }

    final var parsersOpt =
      ServiceLoader.load(CXMLChangelogParserProviderType.class).findFirst();

    if (parsersOpt.isEmpty()) {
      LOG.error("No XML parser providers are available");
      return Status.FAILURE;
    }

    final var writersOpt =
      ServiceLoader.load(CXHTMLChangelogWriterProviderType.class).findFirst();

    if (writersOpt.isEmpty()) {
      LOG.error("No XHTML writer providers are available");
      return Status.FAILURE;
    }

    final var parsers = parsersOpt.get();
    final var writers = writersOpt.get();
    final var changelog =
      parsers.parse(this.path, CParseErrorHandlers.loggingHandler(LOG));

    final CChangelog changelogWrite;
    if (version.isPresent()) {
      final var c_opt =
        CChangelogFilters.upToAndIncluding(
          changelog, version.get(), this.count);
      if (c_opt.isEmpty()) {
        LOG.error("Changelog does not contain release {}", this.versionText);
        return Status.FAILURE;
      }
      changelogWrite = c_opt.get();
    } else {
      changelogWrite = CChangelogFilters.limit(changelog, this.count);
    }

    final var writer =
      writers.create(URI.create("urn:stdout"), System.out);

    writer.write(changelogWrite);
    return Status.SUCCESS;
  }

  @Override
  public String name()
  {
    return "write-xhtml";
  }

  @Override
  public String extendedHelp()
  {
    return this.messages().format("helpWriteXHTML");
  }
}

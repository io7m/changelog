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

package com.io7m.changelog.cmdline;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CRelease;
import com.io7m.changelog.core.CTicketSystem;
import com.io7m.changelog.core.CVersionType;
import com.io7m.changelog.core.CVersions;
import com.io7m.changelog.parser.api.CParseErrorHandlers;
import com.io7m.changelog.xml.api.CXMLChangelogParserProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogParserType;
import com.io7m.changelog.xml.api.CXMLChangelogWriterProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogWriterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.ServiceLoader;

@Parameters(commandDescription = "Add a new release")
final class CLCommandAddRelease extends CLCommandRoot
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CLCommandAddRelease.class);

  @Parameter(
    names = "-file",
    required = false,
    description = "The changelog file")
  private Path path = Paths.get("README-CHANGES.xml");

  @Parameter(
    names = "-version",
    required = true,
    description = "The new release version")
  private String version_text;

  @Parameter(
    names = "-ticket-system",
    description = "The new release ticket system name")
  private String ticket_system;

  CLCommandAddRelease()
  {

  }

  @Override
  public Status execute()
    throws Exception
  {
    if (super.execute() == Status.FAILURE) {
      return Status.FAILURE;
    }

    final CVersionType version = CVersions.parse(this.version_text);

    final Optional<CXMLChangelogParserProviderType> parser_provider_opt =
      ServiceLoader.load(CXMLChangelogParserProviderType.class).findFirst();

    if (!parser_provider_opt.isPresent()) {
      LOG.error("No XML parser providers are available");
      return Status.FAILURE;
    }

    final Optional<CXMLChangelogWriterProviderType> writer_provider_opt =
      ServiceLoader.load(CXMLChangelogWriterProviderType.class).findFirst();

    if (!writer_provider_opt.isPresent()) {
      LOG.error("No XML writer providers are available");
      return Status.FAILURE;
    }

    final CXMLChangelogParserProviderType parser_provider =
      parser_provider_opt.get();
    final CXMLChangelogWriterProviderType writer_provider =
      writer_provider_opt.get();

    final Path path_tmp = Paths.get(this.path + ".tmp");

    try (InputStream stream = Files.newInputStream(this.path)) {
      final CXMLChangelogParserType parser = parser_provider.create(
        this.path.toUri(),
        stream,
        CParseErrorHandlers.loggingHandler(LOG));

      final CChangelog changelog = parser.parse();

      if (changelog.releases().containsKey(version)) {
        LOG.error("Release {} already exists", version);
        return Status.FAILURE;
      }

      final Optional<String> ticket_system_opt = this.findTicketSystem(changelog);
      if (!ticket_system_opt.isPresent()) {
        if (this.ticket_system != null) {
          LOG.error("No ticket system named {} is defined", this.ticket_system);
        } else {
          LOG.error("No default ticket system is available");
        }
        return Status.FAILURE;
      }

      final ZonedDateTime date = ZonedDateTime.now(ZoneId.of("UTC"));

      final CChangelog changelog_write = changelog.withReleases(
        changelog.releases()
          .put(
            version,
            CRelease.builder()
              .setTicketSystemID(ticket_system_opt.get())
              .setVersion(version)
              .setDate(date)
              .build()));

      try (OutputStream output = Files.newOutputStream(path_tmp)) {
        final CXMLChangelogWriterType writer =
          writer_provider.create(this.path.toUri(), output);
        writer.write(changelog_write);
      }

      Files.move(path_tmp, this.path, StandardCopyOption.ATOMIC_MOVE);
    }

    return Status.SUCCESS;
  }

  private Optional<String> findTicketSystem(
    final CChangelog changelog)
  {
    /*
     * If there's an explicitly defined ticket system, use that.
     */

    if (this.ticket_system != null) {
      return changelog.ticketSystems()
        .get(this.ticket_system)
        .toJavaOptional()
        .map(CTicketSystem::id);
    }

    /*
     * Otherwise, if there's only one defined, use that.
     */

    if (changelog.ticketSystems().size() == 1) {
      return Optional.of(changelog.ticketSystems().keySet().get());
    }

    /*
     * Otherwise, try to find the default.
     */

    return changelog.ticketSystems()
      .find(p -> p._2.isDefault())
      .toJavaOptional()
      .map(p -> p._1);
  }
}

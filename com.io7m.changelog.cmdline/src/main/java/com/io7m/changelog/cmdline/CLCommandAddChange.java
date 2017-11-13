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
import com.io7m.changelog.core.CChange;
import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CModuleName;
import com.io7m.changelog.core.CRelease;
import com.io7m.changelog.core.CTicketID;
import com.io7m.changelog.core.CVersionType;
import com.io7m.changelog.parser.api.CParseErrorHandlers;
import com.io7m.changelog.xml.api.CXMLChangelogParserProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogParserType;
import com.io7m.changelog.xml.api.CXMLChangelogWriterProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogWriterType;
import io.vavr.collection.SortedMap;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

@Parameters(commandDescription = "Add a change to the current release")
final class CLCommandAddChange extends CLCommandRoot
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CLCommandAddChange.class);

  @Parameter(
    names = "-file",
    required = false,
    description = "The changelog file")
  private Path path = Paths.get("README-CHANGES.xml");

  @Parameter(
    names = "-summary",
    required = true,
    description = "The change summary")
  private String summary;

  @Parameter(
    names = "-module",
    required = false,
    description = "The affected module")
  private CModuleName module;

  @Parameter(
    names = "-tickets",
    required = false,
    converter = CTicketIDConverter.class,
    description = "The list of tickets")
  private List<CTicketID> tickets = new ArrayList<>();

  @Parameter(
    names = "-incompatible",
    required = false,
    description = "Indicates that the change is backwards incompatible")
  private boolean incompatible;

  CLCommandAddChange()
  {

  }

  @Override
  public Status execute()
    throws Exception
  {
    if (super.execute() == Status.FAILURE) {
      return Status.FAILURE;
    }

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

      final SortedMap<CVersionType, CRelease> latest =
        changelog.releases().takeRight(1);

      if (latest.isEmpty()) {
        LOG.error("No current release exists");
        return Status.FAILURE;
      }

      final ZonedDateTime now =
        ZonedDateTime.now(ZoneId.of("UTC"));

      final CChange change =
        CChange.builder()
          .setModule(Optional.ofNullable(this.module))
          .setBackwardsCompatible(!this.incompatible)
          .setDate(now)
          .setSummary(this.summary)
          .setTickets(io.vavr.collection.List.ofAll(this.tickets))
          .build();

      final CRelease release = latest.get()._2;
      final CRelease release_write = release
        .withChanges(release.changes().append(change))
        .withDate(now);

      final CChangelog changelog_write =
        changelog.withReleases(
          changelog.releases().put(release_write.version(), release_write));

      try (OutputStream output = Files.newOutputStream(path_tmp)) {
        final CXMLChangelogWriterType writer =
          writer_provider.create(this.path.toUri(), output);

        writer.write(changelog_write);
      }

      Files.move(path_tmp, this.path, StandardCopyOption.ATOMIC_MOVE);
    }

    return Status.SUCCESS;
  }
}

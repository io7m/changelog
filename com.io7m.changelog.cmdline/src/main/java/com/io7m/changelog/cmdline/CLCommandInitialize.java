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
import com.io7m.changelog.core.CTicketSystem;
import com.io7m.changelog.xml.api.CXMLChangelogWriterProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogWriterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.ServiceLoader;

@Parameters(commandDescription = "Initialize the changelog")
final class CLCommandInitialize extends CLCommandRoot
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CLCommandInitialize.class);

  @Parameter(
    names = "-file",
    required = false,
    description = "The changelog file")
  private String file = "README-CHANGES.xml";

  @Parameter(
    names = "-project",
    required = true,
    description = "The project name")
  private String project;

  @Parameter(
    names = "-ticket-system-name",
    required = true,
    description = "The name of the primary ticket system")
  private String ticket_system_name;

  @Parameter(
    names = "-ticket-system-uri",
    required = true,
    description = "The URI of the primary ticket system")
  private URI ticket_system_uri;

  CLCommandInitialize()
  {

  }

  @Override
  public Status execute()
    throws Exception
  {
    if (super.execute() == Status.FAILURE) {
      return Status.FAILURE;
    }

    final Optional<CXMLChangelogWriterProviderType> writer_provider_opt =
      ServiceLoader.load(CXMLChangelogWriterProviderType.class).findFirst();

    if (!writer_provider_opt.isPresent()) {
      LOG.error("No XML writer providers are available");
      return Status.FAILURE;
    }

    final CXMLChangelogWriterProviderType writer_provider =
      writer_provider_opt.get();

    final Path path = Paths.get(this.file);
    if (Files.exists(path)) {
      LOG.error("File {} already exists", this.file);
      return Status.FAILURE;
    }

    final Path path_tmp = Paths.get(this.file + ".tmp");
    try (OutputStream stream = Files.newOutputStream(path_tmp)) {
      final CTicketSystem ticket_system =
        CTicketSystem.of(
          this.ticket_system_name,
          this.ticket_system_uri,
          true);

      final CChangelog changelog =
        CChangelog.builder()
          .setProject(this.project)
          .putTicketSystems(this.ticket_system_name, ticket_system)
          .build();

      final CXMLChangelogWriterType writer =
        writer_provider.create(path.toUri(), stream);
      writer.write(changelog);
    }

    Files.move(path_tmp, path, StandardCopyOption.ATOMIC_MOVE);
    return Status.SUCCESS;
  }
}

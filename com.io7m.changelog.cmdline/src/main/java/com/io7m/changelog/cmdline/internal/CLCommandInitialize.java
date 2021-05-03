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
import com.io7m.changelog.core.CProjectName;
import com.io7m.changelog.core.CTicketSystem;
import com.io7m.changelog.xml.api.CXMLChangelogWriterProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogWriterType;
import com.io7m.claypot.core.CLPCommandContextType;
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

/**
 * The "initialize" command.
 */

@Parameters(commandDescription = "Initialize the changelog.")
public final class CLCommandInitialize extends CLAbstractCommand
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CLCommandInitialize.class);

  @Parameter(
    names = "--file",
    required = false,
    description = "The changelog file")
  private Path path = Paths.get("README-CHANGES.xml");

  @Parameter(
    names = "--project",
    required = true,
    description = "The project name",
    converter = CProjectNameConverter.class)
  private CProjectName project;

  @Parameter(
    names = "--ticket-system-name",
    required = true,
    description = "The name of the primary ticket system")
  private String ticket_system_name;

  @Parameter(
    names = "--ticket-system-uri",
    required = true,
    description = "The URI of the primary ticket system")
  private URI ticket_system_uri;

  /**
   * Construct a command.
   *
   * @param inContext The command context
   */

  public CLCommandInitialize(
    final CLPCommandContextType inContext)
  {
    super(LOG, inContext);
  }

  @Override
  public Status executeActual()
    throws Exception
  {
    final Optional<CXMLChangelogWriterProviderType> writer_provider_opt =
      ServiceLoader.load(CXMLChangelogWriterProviderType.class)
        .findFirst();

    if (!writer_provider_opt.isPresent()) {
      LOG.error("No XML writer providers are available");
      return Status.FAILURE;
    }

    final CXMLChangelogWriterProviderType writer_provider =
      writer_provider_opt.get();

    if (Files.exists(this.path)) {
      LOG.error("File {} already exists", this.path);
      return Status.FAILURE;
    }

    final Path path_tmp = Paths.get(this.path + ".tmp");
    try (OutputStream stream = Files.newOutputStream(path_tmp)) {
      final var ticketSystem =
        CTicketSystem.builder()
          .setId(this.ticket_system_name)
          .setUri(this.ticket_system_uri)
          .setDefault(true)
          .build();

      final CChangelog changelog =
        CChangelog.builder()
          .setProject(this.project)
          .putTicketSystems(this.ticket_system_name, ticketSystem)
          .build();

      final CXMLChangelogWriterType writer =
        writer_provider.create(this.path.toUri(), stream);
      writer.write(changelog);
    }

    Files.move(path_tmp, this.path, StandardCopyOption.ATOMIC_MOVE);
    return Status.SUCCESS;
  }

  @Override
  public String name()
  {
    return "initialize";
  }

  @Override
  public String extendedHelp()
  {
    return this.messages().format("helpInitialize");
  }
}

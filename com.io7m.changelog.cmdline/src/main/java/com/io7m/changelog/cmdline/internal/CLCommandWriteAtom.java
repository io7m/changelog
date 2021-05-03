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
import com.io7m.changelog.parser.api.CParseErrorHandlers;
import com.io7m.changelog.xml.api.CAtomChangelogWriterConfiguration;
import com.io7m.changelog.xml.api.CAtomChangelogWriterProviderType;
import com.io7m.changelog.xml.api.CAtomChangelogWriterType;
import com.io7m.changelog.xml.api.CXMLChangelogParserProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogParserType;
import com.io7m.claypot.core.CLPCommandContextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * The "write-atom" command.
 */

@Parameters(commandDescription = "Generate an atom feed.")
public final class CLCommandWriteAtom extends CLAbstractCommand
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CLCommandWriteAtom.class);

  @Parameter(
    names = "--file",
    required = false,
    description = "The changelog file")
  private Path path = Paths.get("README-CHANGES.xml");

  @Parameter(
    names = "--author-email",
    required = true,
    description = "The author email address")
  private String author_email;

  @Parameter(
    names = "--author-name",
    required = true,
    description = "The author name")
  private String author_name;

  @Parameter(
    names = "--title",
    required = true,
    description = "The feed title")
  private String title;

  @Parameter(
    names = "--uri",
    required = true,
    description = "The feed URI")
  private URI uri;

  /**
   * Construct a command.
   *
   * @param inContext The command context
   */

  public CLCommandWriteAtom(
    final CLPCommandContextType inContext)
  {
    super(LOG, inContext);
  }

  @Override
  public String name()
  {
    return "write-atom";
  }

  @Override
  public String extendedHelp()
  {
    return this.messages().format("helpWriteAtom");
  }

  @Override
  public Status executeActual()
    throws Exception
  {
    final Optional<CXMLChangelogParserProviderType> parser_provider_opt =
      ServiceLoader.load(CXMLChangelogParserProviderType.class).findFirst();

    if (!parser_provider_opt.isPresent()) {
      LOG.error("No XML parser providers are available");
      return Status.FAILURE;
    }

    final Optional<CAtomChangelogWriterProviderType> writer_provider_opt =
      ServiceLoader.load(CAtomChangelogWriterProviderType.class).findFirst();

    if (!writer_provider_opt.isPresent()) {
      LOG.error("No Atom writer providers are available");
      return Status.FAILURE;
    }

    final CXMLChangelogParserProviderType parser_provider =
      parser_provider_opt.get();
    final CAtomChangelogWriterProviderType writer_provider =
      writer_provider_opt.get();

    try (InputStream stream = Files.newInputStream(this.path)) {
      final CXMLChangelogParserType parser = parser_provider.create(
        this.path.toUri(),
        stream,
        CParseErrorHandlers.loggingHandler(LOG));

      final CChangelog changelog = parser.parse();

      final CAtomChangelogWriterType writer =
        writer_provider.createWithConfiguration(
          CAtomChangelogWriterConfiguration.builder()
            .setUpdated(ZonedDateTime.now(ZoneId.of("UTC")))
            .setUri(this.uri)
            .setTitle(this.title)
            .setAuthorName(this.author_name)
            .setAuthorEmail(this.author_email)
            .build(),
          this.uri,
          System.out);

      writer.write(changelog);
    }

    return Status.SUCCESS;
  }
}

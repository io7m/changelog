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
import com.io7m.changelog.core.CChangelogFilters;
import com.io7m.changelog.core.CVersionType;
import com.io7m.changelog.core.CVersions;
import com.io7m.changelog.parser.api.CParseErrorHandlers;
import com.io7m.changelog.xml.api.CXHTMLChangelogWriterProviderType;
import com.io7m.changelog.xml.api.CXHTMLChangelogWriterType;
import com.io7m.changelog.xml.api.CXMLChangelogParserProviderType;
import com.io7m.changelog.xml.api.CXMLChangelogParserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ServiceLoader;

@Parameters(commandDescription = "Generate an XHTML log")
final class CLCommandXHTML extends CLCommandRoot
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CLCommandXHTML.class);

  @Parameter(
    names = "-file",
    required = false,
    description = "The changelog file")
  private Path path = Paths.get("README-CHANGES.xml");

  @Parameter(
    names = "-release",
    description = "The release")
  private String release;

  @Parameter(
    names = "-count",
    required = false,
    description = "The total number of releases to display")
  private int count = Integer.MAX_VALUE;

  CLCommandXHTML()
  {

  }

  @Override
  public Status execute()
    throws Exception
  {
    if (super.execute() == Status.FAILURE) {
      return Status.FAILURE;
    }

    final Optional<CVersionType> version;
    if (this.release != null) {
      version = Optional.of(CVersions.parse(this.release));
    } else {
      version = Optional.empty();
    }

    final Optional<CXMLChangelogParserProviderType> parser_provider_opt =
      ServiceLoader.load(CXMLChangelogParserProviderType.class).findFirst();

    if (!parser_provider_opt.isPresent()) {
      LOG.error("No XML parser providers are available");
      return Status.FAILURE;
    }

    final Optional<CXHTMLChangelogWriterProviderType> writer_provider_opt =
      ServiceLoader.load(CXHTMLChangelogWriterProviderType.class).findFirst();

    if (!writer_provider_opt.isPresent()) {
      LOG.error("No XHTML writer providers are available");
      return Status.FAILURE;
    }

    final CXMLChangelogParserProviderType parser_provider =
      parser_provider_opt.get();
    final CXHTMLChangelogWriterProviderType writer_provider =
      writer_provider_opt.get();

    try (InputStream stream = Files.newInputStream(this.path)) {
      final CXMLChangelogParserType parser = parser_provider.create(
        this.path.toUri(),
        stream,
        CParseErrorHandlers.loggingHandler(LOG));

      final CChangelog changelog = parser.parse();
      final CChangelog changelog_write;
      if (version.isPresent()) {
        final Optional<CChangelog> c_opt =
          CChangelogFilters.upToAndIncluding(
            changelog, version.get(), this.count);
        if (!c_opt.isPresent()) {
          LOG.error("Changelog does not contain release {}", this.release);
          return Status.FAILURE;
        }
        changelog_write = c_opt.get();
      } else {
        changelog_write = CChangelogFilters.limit(changelog, this.count);
      }

      final CXHTMLChangelogWriterType writer =
        writer_provider.create(URI.create("urn:stdout"), System.out);

      writer.write(changelog_write);
    }

    return Status.SUCCESS;
  }
}

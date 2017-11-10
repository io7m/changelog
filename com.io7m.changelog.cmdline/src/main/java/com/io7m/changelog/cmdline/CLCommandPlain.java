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
import com.io7m.changelog.core.CVersionType;
import com.io7m.changelog.core.CVersions;
import com.io7m.changelog.text.CChangelogTextWriter;
import com.io7m.changelog.text.CChangelogTextWriterConfiguration;
import com.io7m.changelog.xom.CChangelogXMLReader;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Parameters(commandDescription = "Generate a plain text log")
final class CLCommandPlain extends CLCommandRoot
{
  @Parameter(
    names = "-file",
    required = false,
    description = "The changelog file")
  private String file = "README-CHANGES.xml";

  @Parameter(
    names = "-release",
    description = "The release")
  private String release;

  @Parameter(
    names = "-show-dates",
    description = "Show dates")
  private boolean date;

  CLCommandPlain()
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

    final Path path = Paths.get(this.file);
    try (InputStream stream = Files.newInputStream(path)) {
      final CChangelog clog =
        CChangelogXMLReader.readFromStream(path.toUri(), stream);
      final CChangelogTextWriterConfiguration.Builder config_b =
        CChangelogTextWriterConfiguration.builder()
          .setRelease(version)
          .setShowDates(this.date);

      try (PrintWriter writer =
             new PrintWriter(
               new OutputStreamWriter(System.out, StandardCharsets.UTF_8))) {
        CChangelogTextWriter.writeChangelog(clog, config_b.build(), writer);
      }
    }

    return Status.SUCCESS;
  }
}

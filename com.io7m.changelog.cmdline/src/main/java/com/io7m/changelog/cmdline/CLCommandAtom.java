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
import com.io7m.changelog.xom.CAtomFeedMeta;
import com.io7m.changelog.xom.CChangelogAtomWriter;
import com.io7m.changelog.xom.CChangelogXMLReader;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Parameters(commandDescription = "Generate an atom feed")
final class CLCommandAtom extends CLCommandRoot
{
  @Parameter(
    names = "-file",
    required = false,
    description = "The changelog file")
  private String file = "README-CHANGES.xml";

  @Parameter(
    names = "-author-email",
    required = true,
    description = "The author email address")
  private String author_email;

  @Parameter(
    names = "-author-name",
    required = true,
    description = "The author name")
  private String author_name;

  @Parameter(
    names = "-title",
    required = true,
    description = "The feed title")
  private String title;

  @Parameter(
    names = "-uri",
    required = true,
    description = "The feed URI")
  private URI uri;

  CLCommandAtom()
  {

  }

  @Override
  public Status execute()
    throws Exception
  {
    if (super.execute() == Status.FAILURE) {
      return Status.FAILURE;
    }

    final Path path = Paths.get(this.file);
    try (InputStream stream = Files.newInputStream(path)) {
      final CChangelog clog =
        CChangelogXMLReader.readFromStream(path.toUri(), stream);

      final CAtomFeedMeta meta =
        CAtomFeedMeta.builder()
          .setAuthorEmail(this.author_email)
          .setAuthorName(this.author_name)
          .setTitle(this.title)
          .setUri(this.uri)
          .build();

      final Element e = CChangelogAtomWriter.writeElement(meta, clog);
      final Serializer s = new Serializer(System.out, "UTF-8");
      s.setIndent(2);
      s.setMaxLength(80);
      s.write(new Document(e));
      s.flush();
    }

    return Status.SUCCESS;
  }
}

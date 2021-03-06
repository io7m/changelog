/*
 * Copyright © 2017 <code@io7m.com> http://io7m.com
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

package com.io7m.changelog.xml.api;

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.parser.api.CChangelogParserProviderType;
import com.io7m.changelog.parser.api.CParseError;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * The type of parser providers.
 */

public interface CXMLChangelogParserProviderType
  extends CChangelogParserProviderType
{
  @Override
  CXMLChangelogParserType create(
    URI uri,
    InputStream stream,
    Consumer<CParseError> receiver)
    throws IOException;

  /**
   * Parse a changelog from the given file.
   *
   * @param file     The file
   * @param receiver An error receiver
   *
   * @return A parsed changelog
   *
   * @throws IOException On I/O errors
   */

  default CChangelog parse(
    final Path file,
    final Consumer<CParseError> receiver)
    throws IOException
  {
    try (var stream = Files.newInputStream(file)) {
      final var parser =
        this.create(file.toUri(), stream, receiver);
      return parser.parse();
    }
  }
}

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

import com.io7m.changelog.writer.api.CChangelogWriterProviderType;
import com.io7m.changelog.writer.api.CChangelogWriterType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * The type of Atom writer providers.
 */

public interface CAtomChangelogWriterProviderType
  extends CChangelogWriterProviderType
{
  @Override
  default CChangelogWriterType create(
    final URI uri,
    final OutputStream stream)
    throws IOException
  {
    return this.createWithConfiguration(
      CAtomChangelogWriterConfiguration.builder()
        .setAuthorEmail("ABUSE@example.com")
        .setAuthorName("Example Nobody")
        .setTitle("Releases")
        .setUri(URI.create("http://www.example.com"))
        .build(),
      uri,
      stream);
  }

  /**
   * Create a writer with the given configuration.
   *
   * @param configuration The atom feed configuration
   * @param uri           The output URI, for diagnostic purposes
   * @param stream        The output stream
   *
   * @return A writer
   *
   * @throws IOException On I/O errors
   */

  CAtomChangelogWriterType createWithConfiguration(
    CAtomChangelogWriterConfiguration configuration,
    URI uri,
    OutputStream stream)
    throws IOException;
}

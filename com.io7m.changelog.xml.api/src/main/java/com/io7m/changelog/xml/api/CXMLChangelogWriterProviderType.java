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
import com.io7m.changelog.writer.api.CChangelogWriterProviderType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * The type of writer providers.
 */

public interface CXMLChangelogWriterProviderType
  extends CChangelogWriterProviderType
{
  @Override
  CXMLChangelogWriterType create(
    URI uri,
    OutputStream stream)
    throws IOException;

  /**
   * Create a new writer, write the contents of {@code changelog} to
   * {@code fileTemp} and then atomically rename {@code fileTemp} to
   * {@code file}.
   *
   * @param file      The output file
   * @param fileTemp  The temporary output file
   * @param changelog The changelog
   *
   * @throws IOException On I/O errors
   */

  default void write(
    final Path file,
    final Path fileTemp,
    final CChangelog changelog)
    throws IOException
  {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(fileTemp, "fileTemp");
    Objects.requireNonNull(changelog, "changelog");

    try (var output = Files.newOutputStream(fileTemp)) {
      final var writer = this.create(file.toUri(), output);
      writer.write(changelog);
    }

    Files.move(fileTemp, file, StandardCopyOption.ATOMIC_MOVE);
  }
}

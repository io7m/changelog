/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
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

package com.io7m.changelog.core;

import java.net.URI;

/**
 * The type of mutable changelog builders.
 */

public interface CChangelogBuilderType
{
  /**
   * Set the project name.
   * 
   * @param project
   *          The project name
   */

  void setProjectName(
    final String project);

  /**
   * Add the ticketing system with the given name and URI.
   * 
   * @param name
   *          The name
   * @param uri
   *          The URI
   */

  void addTicketSystem(
    final String name,
    final URI uri);

  /**
   * Add the given release.
   * 
   * @param release
   *          The release
   */

  void addRelease(
    final CRelease release);

  /**
   * Construct a changelog with the given releases and ticket systems.
   * 
   * @return A new changelog
   */

  CChangelog build();
}

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

package com.io7m.changelog.xom;

import java.net.URI;

/**
 * The type of mutable atom feed builders.
 */

public interface CAtomFeedMetaBuilderType
{
  /**
   * Construct a new feed based on the parameters so far.
   * 
   * @return A feed
   */

  CAtomFeedMeta build();

  /**
   * Set the feed author email address.
   * 
   * @param email
   *          The email
   */

  void setAuthorEmail(
    final String email);

  /**
   * Set the feed author name.
   * 
   * @param name
   *          The name
   */

  void setAuthorName(
    final String name);

  /**
   * Set the feed title.
   * 
   * @param title
   *          The title
   */

  void setTitle(
    final String title);

  /**
   * Set the feed URI.
   * 
   * @param uri
   *          The URI
   */

  void setURI(
    final URI uri);
}

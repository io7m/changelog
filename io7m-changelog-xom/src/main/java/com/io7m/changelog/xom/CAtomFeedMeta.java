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
import java.util.concurrent.atomic.AtomicReference;

/**
 * Information about an Atom feed.
 */

public final class CAtomFeedMeta
{
  /**
   * @return A new atom feed metadata builder
   */

  public static CAtomFeedMetaBuilderType newBuilder()
  {
    final AtomicReference<String> title = new AtomicReference<String>();
    final AtomicReference<String> email = new AtomicReference<String>();
    final AtomicReference<String> name = new AtomicReference<String>();
    final AtomicReference<URI> uri = new AtomicReference<URI>();

    return new CAtomFeedMetaBuilderType() {
      @Override public CAtomFeedMeta build()
      {
        return new CAtomFeedMeta(
          title.get(),
          name.get(),
          email.get(),
          uri.get());
      }

      @Override public void setAuthorEmail(
        final String in_email)
      {
        if (in_email == null) {
          throw new NullPointerException("Author email");
        }
        email.set(in_email);
      }

      @Override public void setAuthorName(
        final String in_name)
      {
        if (in_name == null) {
          throw new NullPointerException("Author name");
        }
        name.set(in_name);
      }

      @Override public void setTitle(
        final String in_title)
      {
        if (in_title == null) {
          throw new NullPointerException("Title");
        }
        title.set(in_title);
      }

      @Override public void setURI(
        final URI in_uri)
      {
        if (in_uri == null) {
          throw new NullPointerException("Feed URI");
        }
        uri.set(in_uri);
      }
    };
  }

  private final String author_email;
  private final String author_name;
  private final String title;
  private final URI    uri;

  CAtomFeedMeta(
    final String in_title,
    final String in_author_name,
    final String in_author_email,
    final URI in_uri)
  {
    if (in_title == null) {
      throw new NullPointerException("Title unset");
    }
    if (in_author_name == null) {
      throw new NullPointerException("Author name unset");
    }
    if (in_author_email == null) {
      throw new NullPointerException("Author email unset");
    }
    if (in_uri == null) {
      throw new NullPointerException("URI unset");
    }

    this.title = in_title;
    this.author_name = in_author_name;
    this.author_email = in_author_email;
    this.uri = in_uri;
  }

  @Override public boolean equals(
    final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final CAtomFeedMeta other = (CAtomFeedMeta) obj;
    return this.author_email.equals(other.author_email)
      && this.author_name.equals(other.author_name)
      && this.title.equals(other.title)
      && this.uri.equals(other.uri);
  }

  /**
   * @return The email of the feed author
   */

  public String getAuthorEmail()
  {
    return this.author_email;
  }

  /**
   * @return The name of the feed author
   */

  public String getAuthorName()
  {
    return this.author_name;
  }

  /**
   * @return The feed title
   */

  public String getTitle()
  {
    return this.title;
  }

  /**
   * @return The URI of the feed
   */

  public URI getURI()
  {
    return this.uri;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.author_email.hashCode();
    result = (prime * result) + this.author_name.hashCode();
    result = (prime * result) + this.title.hashCode();
    result = (prime * result) + this.uri.hashCode();
    return result;
  }
}

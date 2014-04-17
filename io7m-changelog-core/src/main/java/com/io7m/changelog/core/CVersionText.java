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

/**
 * <p>
 * The type of arbitrary lexicographically ordered version strings.
 * </p>
 */

public final class CVersionText implements CVersionType
{
  /**
   * Construct a new version number.
   * 
   * @param version
   *          The version text
   * @return A new version number
   */

  public static CVersionType text(
    final String version)
  {
    return new CVersionText(version);
  }

  private final String version;

  private CVersionText(
    final String in_version)
  {
    if (in_version == null) {
      throw new NullPointerException("Version");
    }
    this.version = in_version;
  }

  @Override public int compareTo(
    final CVersionType o)
  {
    return this.version.compareTo(o.toString());
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
    final CVersionText other = (CVersionText) obj;
    return this.version.equals(other.version);
  }

  /**
   * @return The version text
   */

  public String getVersion()
  {
    return this.version;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.version.hashCode();
    return result;
  }

  @Override public String toString()
  {
    return this.version;
  }

  @Override public <A> A versionAccept(
    final CVersionVisitorType<A> v)
    throws Exception
  {
    return v.text(this);
  }
}

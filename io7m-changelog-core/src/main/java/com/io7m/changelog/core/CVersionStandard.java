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
 * The type of standard version numbers of the form <code>M.N.P-S</code>,
 * where <code>M</code> is the major version, <code>N</code> is the minor
 * version, <code>P</code> is the patch number, and <code>S</code> is an
 * arbitrary qualifier string.
 * </p>
 */

public final class CVersionStandard implements CVersionType
{
  /**
   * Attempt to parse a version number.
   * 
   * @param version
   *          The version string
   * @return A new version number
   */

  public static CVersionType parse(
    final String version)
  {
    if (version == null) {
      throw new NullPointerException("Version");
    }

    String ptr = version;
    int dot = ptr.indexOf('.');
    if (dot == -1) {
      return CVersionText.text(version);
    }

    final Integer mj = Integer.valueOf(ptr.substring(0, dot));
    ptr = version.substring(dot + 1);

    dot = ptr.indexOf('.');
    if (dot == -1) {
      return CVersionText.text(version);
    }

    final Integer mn = Integer.valueOf(ptr.substring(0, dot));
    ptr = ptr.substring(dot + 1);

    if (ptr.matches("[0-9]+")) {
      return CVersionStandard.standard(mj.intValue(), mn.intValue(), Integer
        .valueOf(ptr)
        .intValue(), "");
    }

    dot = ptr.indexOf('-');
    if (dot == -1) {
      return CVersionText.text(version);
    }

    final Integer mp = Integer.valueOf(ptr.substring(0, dot));
    ptr = ptr.substring(dot + 1);

    return CVersionStandard.standard(
      mj.intValue(),
      mn.intValue(),
      mp.intValue(),
      ptr);
  }

  /**
   * Construct a new version number from the given versions.
   * 
   * @param in_major
   *          The major version
   * @param in_minor
   *          The minor version
   * @param in_patch
   *          The patch version
   * @param in_qualifier
   *          An qualifier string
   * @return A new version number
   */

  public static CVersionType standard(
    final int in_major,
    final int in_minor,
    final int in_patch,
    final String in_qualifier)
  {
    return new CVersionStandard(in_major, in_minor, in_patch, in_qualifier);
  }

  private final String qualifier;
  private final int    major;
  private final int    minor;
  private final int    patch;

  private CVersionStandard(
    final int in_major,
    final int in_minor,
    final int in_patch,
    final String in_qualifier)
  {
    if (in_major < 0) {
      throw new IllegalArgumentException("Major < 0");
    }
    if (in_minor < 0) {
      throw new IllegalArgumentException("Minor < 0");
    }
    if (in_patch < 0) {
      throw new IllegalArgumentException("Patch < 0");
    }
    if (in_qualifier == null) {
      throw new NullPointerException("Extra");
    }

    this.major = in_major;
    this.minor = in_minor;
    this.patch = in_patch;
    this.qualifier = in_qualifier;
  }

  @Override public int compareTo(
    final CVersionType o)
  {
    try {
      return o.versionAccept(new CVersionVisitorType<Integer>() {
        @SuppressWarnings({ "synthetic-access", "boxing" }) @Override public
          Integer
          standard(
            final CVersionStandard other)
            throws Exception
        {
          if (CVersionStandard.this.major < other.major) {
            return -1;
          }
          if (CVersionStandard.this.major > other.major) {
            return 1;
          }

          assert CVersionStandard.this.major == other.major;

          if (CVersionStandard.this.minor < other.minor) {
            return -1;
          }
          if (CVersionStandard.this.minor > other.minor) {
            return 1;
          }

          assert CVersionStandard.this.minor == other.minor;

          if (CVersionStandard.this.patch < other.patch) {
            return -1;
          }
          if (CVersionStandard.this.patch > other.patch) {
            return 1;
          }

          assert CVersionStandard.this.patch == other.patch;
          return -CVersionStandard.this.qualifier.compareTo(other.qualifier);
        }

        @SuppressWarnings("boxing") @Override public Integer text(
          final CVersionText s)
          throws Exception
        {
          return this.toString().compareTo(s.getVersion());
        }
      }).intValue();
    } catch (final Exception e) {
      throw new AssertionError(e);
    }
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append(this.major);
    builder.append(".");
    builder.append(this.minor);
    builder.append(".");
    builder.append(this.patch);
    if (this.qualifier.isEmpty() == false) {
      builder.append("-");
      builder.append(this.qualifier);
    }
    return builder.toString();
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
    final CVersionStandard other = (CVersionStandard) obj;
    return this.qualifier.equals(other.qualifier)
      && (this.major == other.major)
      && (this.minor == other.minor)
      && (this.patch == other.patch);
  }

  /**
   * @return The qualifier component of the version number
   */

  public String getQualifier()
  {
    return this.qualifier;
  }

  /**
   * @return The major version number
   */

  public int getMajor()
  {
    return this.major;
  }

  /**
   * @return The minor version number
   */

  public int getMinor()
  {
    return this.minor;
  }

  /**
   * @return The patch number
   */

  public int getPatch()
  {
    return this.patch;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.qualifier.hashCode();
    result = (prime * result) + this.major;
    result = (prime * result) + this.minor;
    result = (prime * result) + this.patch;
    return result;
  }

  @Override public <A> A versionAccept(
    final CVersionVisitorType<A> v)
    throws Exception
  {
    return v.standard(this);
  }
}

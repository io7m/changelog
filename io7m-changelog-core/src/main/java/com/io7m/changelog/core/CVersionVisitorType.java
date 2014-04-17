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
 * A version number visitor.
 * 
 * @param <A>
 *          The type of returned values
 */

public interface CVersionVisitorType<A>
{
  /**
   * Visit a {@link CVersionStandard}.
   * 
   * @param s
   *          The number
   * @return A value of type <code>A</code>
   * @throws Exception
   *           If required
   */

  A standard(
    final CVersionStandard s)
    throws Exception;

  /**
   * Visit a {@link CVersionText}.
   * 
   * @param s
   *          The number
   * @return A value of type <code>A</code>
   * @throws Exception
   *           If required
   */

  A text(
    final CVersionText s)
    throws Exception;
}

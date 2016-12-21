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

package com.io7m.changelog.core;

import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.List;

/**
 * A specific release in a changelog.
 */

@CImmutableStyleType
@Value.Immutable
public interface CReleaseType
{
  /**
   * @return The release date
   */

  @Value.Parameter
  LocalDate date();

  /**
   * @return The list of release changes
   */

  @Value.Parameter
  List<CItem> items();

  /**
   * @return The ticket system ID
   */

  @Value.Parameter
  String ticketSystemID();

  /**
   * @return The version number
   */

  @Value.Parameter
  CVersionType version();
}
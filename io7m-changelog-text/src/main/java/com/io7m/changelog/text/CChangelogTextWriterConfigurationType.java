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

package com.io7m.changelog.text;

import com.io7m.changelog.core.CImmutableStyleType;
import com.io7m.changelog.core.CVersionType;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * The type of text writer configurations.
 */

@CImmutableStyleType
@Value.Immutable
public interface CChangelogTextWriterConfigurationType
{
  /**
   * @return The release, if any, to which the output should be restricted
   */

  @Value.Parameter
  Optional<CVersionType> release();

  /**
   * @return {@code true} iff dates should be shown in the output
   */

  @Value.Parameter
  @Value.Default
  default boolean showDates()
  {
    return true;
  }
}

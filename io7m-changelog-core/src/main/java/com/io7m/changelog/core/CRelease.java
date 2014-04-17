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

import java.util.Date;
import java.util.List;

/**
 * A specific release in a changelog.
 */

public final class CRelease
{
  /**
   * Construct a new release.
   * 
   * @param in_ticket_system_id
   *          The ticket system ID
   * @param in_date
   *          The date
   * @param in_version
   *          The version
   * @param in_items
   *          The items
   * @return A new release
   */

  public static CRelease newRelease(
    final String in_ticket_system_id,
    final Date in_date,
    final CVersionType in_version,
    final List<CItem> in_items)
  {
    return new CRelease(in_ticket_system_id, in_date, in_version, in_items);
  }

  private final Date         date;
  private final List<CItem>  items;
  private final String       ticket_system_id;
  private final CVersionType version;

  private CRelease(
    final String in_ticket_system_id,
    final Date in_date,
    final CVersionType in_version,
    final List<CItem> in_items)
  {
    if (in_ticket_system_id == null) {
      throw new NullPointerException("Ticket system ID");
    }
    if (in_date == null) {
      throw new NullPointerException("Date");
    }
    if (in_version == null) {
      throw new NullPointerException("Version");
    }
    if (in_items == null) {
      throw new NullPointerException("Items");
    }
    if (in_items.size() < 1) {
      throw new IllegalArgumentException("At least one item expected");
    }
    for (int index = 0; index < in_items.size(); ++index) {
      if (in_items.get(index) == null) {
        throw new NullPointerException("Items[" + index + "]");
      }
    }

    this.ticket_system_id = in_ticket_system_id;
    this.date = in_date;
    this.version = in_version;
    this.items = in_items;
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
    final CRelease other = (CRelease) obj;
    return this.date.equals(other.date)
      && this.items.equals(other.items)
      && this.ticket_system_id.equals(other.ticket_system_id)
      && this.version.equals(other.version);
  }

  /**
   * @return The release date
   */

  public Date getDate()
  {
    return this.date;
  }

  /**
   * @return The list of release changes
   */

  public List<CItem> getItems()
  {
    return this.items;
  }

  /**
   * @return The ticket system ID
   */

  public String getTicketSystemID()
  {
    return this.ticket_system_id;
  }

  /**
   * @return The version number
   */

  public CVersionType getVersion()
  {
    return this.version;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.date.hashCode();
    result = (prime * result) + this.items.hashCode();
    result = (prime * result) + this.ticket_system_id.hashCode();
    result = (prime * result) + this.version.hashCode();
    return result;
  }
}

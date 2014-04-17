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
 * A changelog item for a specific release.
 */

public final class CItem
{
  /**
   * Construct a new changelog item.
   * 
   * @param in_tickets
   *          The list of tickets
   * @param in_summary
   *          The summary
   * @param in_date
   *          The date
   * @param in_type
   *          The type
   * @return A new item
   */

  public static CItem newItem(
    final List<String> in_tickets,
    final String in_summary,
    final Date in_date,
    final CChangeType in_type)
  {
    return new CItem(in_tickets, in_summary, in_date, in_type);
  }

  private final Date         date;
  private final String       summary;
  private final List<String> tickets;
  private final CChangeType  type;

  private CItem(
    final List<String> in_tickets,
    final String in_summary,
    final Date in_date,
    final CChangeType in_type)
  {
    if (in_tickets == null) {
      throw new NullPointerException("Tickets");
    }
    if (in_summary == null) {
      throw new NullPointerException("Summary");
    }
    if (in_date == null) {
      throw new NullPointerException("Date");
    }
    if (in_type == null) {
      throw new NullPointerException("Change type");
    }

    this.tickets = in_tickets;
    this.summary = in_summary;
    this.date = in_date;
    this.type = in_type;
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
    final CItem other = (CItem) obj;
    return this.date.equals(other.date)
      && this.summary.equals(other.summary)
      && this.tickets.equals(other.tickets)
      && (this.type == other.type);
  }

  /**
   * @return The change date
   */

  public Date getDate()
  {
    return this.date;
  }

  /**
   * @return The change summary
   */

  public String getSummary()
  {
    return this.summary;
  }

  /**
   * @return The change tickets
   */

  public List<String> getTickets()
  {
    return this.tickets;
  }

  /**
   * @return The change type
   */

  public CChangeType getType()
  {
    return this.type;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.date.hashCode();
    result = (prime * result) + this.summary.hashCode();
    result = (prime * result) + this.tickets.hashCode();
    result = (prime * result) + this.type.hashCode();
    return result;
  }
}

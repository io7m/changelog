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
import java.util.List;

/**
 * The definition of a ticket system.
 */

public final class CTicketSystem
{
  /**
   * Construct a new ticket system.
   * 
   * @param in_id
   *          The unique ID of the ticket system
   * @param in_uris
   *          The list of URIs for the ticket system
   * @return A new ticket system
   */

  public static CTicketSystem ticketSystem(
    final String in_id,
    final List<URI> in_uris)
  {
    return new CTicketSystem(in_id, in_uris);
  }

  private final String    id;
  private final List<URI> uris;

  private CTicketSystem(
    final String in_id,
    final List<URI> in_uris)
  {
    if (in_id == null) {
      throw new NullPointerException("id");
    }
    if (in_uris == null) {
      throw new NullPointerException("URIs");
    }
    for (int index = 0; index < in_uris.size(); ++index) {
      if (in_uris.get(index) == null) {
        throw new NullPointerException("URIs[" + index + "]");
      }
    }

    this.id = in_id;
    this.uris = in_uris;
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
    final CTicketSystem other = (CTicketSystem) obj;
    return this.id.equals(other.id) && this.uris.equals(other.uris);
  }

  /**
   * @return The ID of the ticket system
   */

  public String getID()
  {
    return this.id;
  }

  /**
   * @return The list of URIs for the ticket system
   */

  public List<URI> getURIs()
  {
    return this.uris;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.id.hashCode();
    result = (prime * result) + this.uris.hashCode();
    return result;
  }
}

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The type of software changelogs.
 */

@SuppressWarnings("synthetic-access") public final class CChangelog
{
  /**
   * @return A new changelog builder
   */

  public static CChangelogBuilderType newBuilder()
  {
    final Map<String, URI> ticket_systems_map = new HashMap<String, URI>();
    final List<CRelease> releases = new ArrayList<CRelease>();
    final AtomicReference<String> project_name =
      new AtomicReference<String>();

    return new CChangelogBuilderType() {
      @Override public void addRelease(
        final CRelease release)
      {
        if (release == null) {
          throw new NullPointerException("Release");
        }
        if (ticket_systems_map.containsKey(release.getTicketSystemID()) == false) {
          throw new IllegalArgumentException("Unknown ticket system "
            + release.getTicketSystemID());
        }

        releases.add(release);
      }

      @Override public void addTicketSystem(
        final String name,
        final URI uri)
      {
        if (name == null) {
          throw new NullPointerException("Name");
        }
        if (uri == null) {
          throw new NullPointerException("URI");
        }
        if (ticket_systems_map.containsKey(name)) {
          throw new IllegalArgumentException(
            "Multiple ticket systems with the name " + name);
        }

        ticket_systems_map.put(name, uri);
      }

      @Override public CChangelog build()
      {
        if (project_name.get() == null) {
          throw new IllegalArgumentException("Project name unset");
        }

        return new CChangelog(
          ticket_systems_map,
          project_name.get(),
          releases);
      }

      @Override public void setProjectName(
        final String name)
      {
        if (name == null) {
          throw new NullPointerException(name);
        }

        project_name.set(name);
      }
    };
  }

  private final String           project;
  private final List<CRelease>   releases;
  private final Map<String, URI> ticket_systems_map;

  private CChangelog(
    final Map<String, URI> in_ticket_systems_map,
    final String in_project,
    final List<CRelease> in_releases)
  {
    this.ticket_systems_map = in_ticket_systems_map;
    this.project = in_project;
    this.releases = in_releases;
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
    final CChangelog other = (CChangelog) obj;
    return this.project.equals(other.project)
      && this.releases.equals(other.releases)
      && this.ticket_systems_map.equals(other.ticket_systems_map);
  }

  /**
   * @return The project name
   */

  public String getProject()
  {
    return this.project;
  }

  /**
   * @return The list of releases
   */

  public List<CRelease> getReleases()
  {
    return Collections.unmodifiableList(this.releases);
  }

  /**
   * @return The ticket systems
   */

  public Map<String, URI> getTicketSystemsMap()
  {
    return Collections.unmodifiableMap(this.ticket_systems_map);
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.project.hashCode();
    result = (prime * result) + this.releases.hashCode();
    result = (prime * result) + this.ticket_systems_map.hashCode();
    return result;
  }
}

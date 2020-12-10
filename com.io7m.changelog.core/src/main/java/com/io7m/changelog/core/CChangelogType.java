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

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jaffirm.core.Preconditions;
import org.immutables.value.Value;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.immutables.value.Value.Immutable;

/**
 * The type of changelogs.
 */

@ImmutablesStyleType
@Immutable
public interface CChangelogType
{
  /**
   * @return The project name
   */

  CProjectName project();

  /**
   * @return The list of releases
   */

  Map<CVersionType, CRelease> releases();

  /**
   * @return The ticket systems
   */

  Map<String, CTicketSystem> ticketSystems();

  /**
   * @return The list of available versions in ascending order
   */

  default List<CVersionType> releaseVersions()
  {
    return this.releases()
      .keySet()
      .stream()
      .sorted()
      .collect(Collectors.toList());
  }

  /**
   * @return The latest release, if one is defined
   */

  default Optional<CRelease> latestRelease()
  {
    return this.releaseVersions()
      .stream()
      .sorted(Comparator.reverseOrder())
      .limit(1L)
      .findFirst()
      .flatMap(v -> Optional.ofNullable(this.releases().get(v)));
  }

  /**
   * Find the current ticket system.
   *
   * @param idOpt The ticket system ID, if any
   *
   * @return The ticket system if it exists
   */

  default Optional<String> findTicketSystem(
    final Optional<String> idOpt)
  {
    /*
     * If there's an explicitly defined ticket system, use that.
     */

    if (idOpt.isPresent()) {
      final var id = idOpt.get();
      return Optional.ofNullable(this.ticketSystems().get(id))
        .map(CTicketSystem::id);
    }

    /*
     * Otherwise, if there's only one defined, use that.
     */

    if (this.ticketSystems().size() == 1) {
      return this.ticketSystems()
        .keySet()
        .stream()
        .findFirst();
    }

    /*
     * Otherwise, try to find the default.
     */

    return this.ticketSystems()
      .values()
      .stream()
      .filter(CTicketSystem::isDefault)
      .findFirst()
      .map(CTicketSystem::id);
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    final Map<String, CTicketSystem> systems = this.ticketSystems();

    this.releases().forEach(
      (version, release) ->
      {
        final String system_id = release.ticketSystemID();
        Preconditions.checkPrecondition(
          system_id,
          systems.containsKey(system_id),
          s -> "Release must refer to a defined ticket system");
      });

    Preconditions.checkPrecondition(
      systems,
      systems.values()
        .stream()
        .filter(CTicketSystem::isDefault)
        .count() <= 1L,
      x -> "At most one ticket system may be declared as being the default");
  }
}

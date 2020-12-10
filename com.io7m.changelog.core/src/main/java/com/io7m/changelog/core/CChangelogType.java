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

import java.math.BigInteger;
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

  Map<CVersion, CRelease> releases();

  /**
   * @return The ticket systems
   */

  Map<String, CTicketSystem> ticketSystems();

  /**
   * @return The list of available versions in ascending order
   */

  default List<CVersion> releaseVersions()
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
   * Suggest a new version number for the next release.
   *
   * @return A new version greater than any existing version
   *
   * @throws IllegalArgumentException If a version cannot be determined
   */

  default CVersion suggestNextRelease()
    throws IllegalArgumentException
  {
    final var latestOpt = this.latestRelease();
    if (latestOpt.isEmpty()) {
      return CVersion.of(BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO);
    }

    final var latest = latestOpt.get().version();
    return CVersion.of(
      latest.major(),
      latest.minor().add(BigInteger.ONE),
      BigInteger.ZERO
    );
  }

  /**
   * Find the target release if one is specified, or return the current release
   * otherwise, but only if either of those releases are open.
   *
   * @param version The release version
   *
   * @return The target release
   */

  default Optional<CRelease> findTargetReleaseOrLatestOpen(
    final Optional<CVersion> version)
  {
    return this.findTargetReleaseOrLatest(version)
      .flatMap(release -> {
        if (release.isOpen()) {
          return Optional.of(release);
        }
        return Optional.empty();
      });
  }

  /**
   * Find the target release if one is specified, or return the current release otherwise.
   *
   * @param version The release version
   *
   * @return The target release
   */

  default Optional<CRelease> findTargetReleaseOrLatest(
    final Optional<CVersion> version)
  {
    return version.flatMap(this::findTargetRelease)
      .or(this::latestRelease);
  }

  /**
   * Find the target release.
   *
   * @param version The release version
   *
   * @return The target release
   */

  default Optional<CRelease> findTargetRelease(
    final CVersion version)
  {
    return Optional.ofNullable(this.releases().get(version));
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

    final var openReleases =
      this.releases()
        .values()
        .stream()
        .filter(CReleaseType::isOpen)
        .count();

    Preconditions.checkPreconditionL(
      openReleases,
      openReleases <= 1L,
      c -> "At most one release may be open at any given time"
    );

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

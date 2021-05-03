/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> http://io7m.com
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

package com.io7m.changelog.cmdline.internal;

import com.io7m.claypot.core.CLPAbstractCommand;
import com.io7m.claypot.core.CLPCommandContextType;
import org.slf4j.Logger;

import java.util.Objects;

/**
 * The base type of commands.
 */

public abstract class CLAbstractCommand extends CLPAbstractCommand
{
  private final CLMessages messages;
  private final Logger logger;

  /**
   * Construct a command.
   *
   * @param inLogger  The command logger
   * @param inContext The command context
   */

  public CLAbstractCommand(
    final Logger inLogger,
    final CLPCommandContextType inContext)
  {
    super(inContext);
    this.logger = Objects.requireNonNull(inLogger, "logger");
    this.messages = CLMessages.create();
  }

  protected final CLMessages messages()
  {
    return this.messages;
  }

  protected final void error(
    final String id,
    final Object... arguments)
  {
    this.logger.error("{}", this.messages.format(id, arguments));
  }

  protected final void info(
    final String id,
    final Object... arguments)
  {
    this.logger.info("{}", this.messages.format(id, arguments));
  }
}

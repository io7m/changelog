/*
 * Copyright © 2017 <code@io7m.com> http://io7m.com
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

package com.io7m.changelog.parser.api;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.junreachable.UnreachableCodeException;
import org.slf4j.Logger;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Functions to handle parse errors.
 */

public final class CParseErrorHandlers
{
  private CParseErrorHandlers()
  {
    throw new UnreachableCodeException();
  }

  /**
   * A parse error handler that formats and logs all errors to the given
   * logger.
   *
   * @param logger The logger
   *
   * @return An error handler
   */

  public static Consumer<CParseError> loggingHandler(
    final Logger logger)
  {
    Objects.requireNonNull(logger, "Logger");

    return error -> {
      final Optional<URI> file_opt = error.lexical().file();
      final Optional<Exception> ex_opt = error.exception();
      switch (error.severity()) {
        case WARNING: {
          onWarn(logger, error, file_opt, ex_opt);
          break;
        }
        case ERROR: {
          onError(logger, error, file_opt, ex_opt);
          break;
        }
        case CRITICAL: {
          onError(logger, error, file_opt, ex_opt);
          break;
        }
      }
    };
  }

  private static void onWarn(
    final Logger logger,
    final CParseError error,
    final Optional<URI> file_opt,
    final Optional<Exception> ex_opt)
  {
    final LexicalPosition<URI> lexical = error.lexical();
    if (file_opt.isPresent()) {
      if (ex_opt.isPresent()) {
        final Exception exception = ex_opt.get();
        logger.warn(
          "{}:{}:{}: {}: ",
          file_opt.get(),
          Integer.valueOf(lexical.line()),
          Integer.valueOf(lexical.column()),
          error.message(),
          exception);
      } else {
        logger.warn(
          "{}:{}:{}: {}",
          file_opt.get(),
          Integer.valueOf(lexical.line()),
          Integer.valueOf(lexical.column()),
          error.message());
      }
    } else {
      if (ex_opt.isPresent()) {
        final Exception exception = ex_opt.get();
        logger.warn(
          "{}:{}: {}: ",
          Integer.valueOf(lexical.line()),
          Integer.valueOf(lexical.column()),
          error.message(),
          exception);
      } else {
        logger.warn(
          "{}:{}: {}",
          Integer.valueOf(lexical.line()),
          Integer.valueOf(lexical.column()),
          error.message());
      }
    }
  }

  private static void onError(
    final Logger logger,
    final CParseError error,
    final Optional<URI> file_opt,
    final Optional<Exception> ex_opt)
  {
    final LexicalPosition<URI> lexical = error.lexical();
    if (file_opt.isPresent()) {
      if (ex_opt.isPresent()) {
        final Exception exception = ex_opt.get();
        logger.error(
          "{}:{}:{}: {}: ",
          file_opt.get(),
          Integer.valueOf(lexical.line()),
          Integer.valueOf(lexical.column()),
          error.message(),
          exception);
      } else {
        logger.error(
          "{}:{}:{}: {}",
          file_opt.get(),
          Integer.valueOf(lexical.line()),
          Integer.valueOf(lexical.column()),
          error.message());
      }
    } else {
      if (ex_opt.isPresent()) {
        final Exception exception = ex_opt.get();
        logger.error(
          "{}:{}: {}: ",
          Integer.valueOf(lexical.line()),
          Integer.valueOf(lexical.column()),
          error.message(),
          exception);
      } else {
        logger.error(
          "{}:{}: {}",
          Integer.valueOf(lexical.line()),
          Integer.valueOf(lexical.column()),
          error.message());
      }
    }
  }
}

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

package com.io7m.changelog.tests.cmdline;

import com.io7m.changelog.cmdline.MainExitless;
import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CRelease;
import com.io7m.changelog.core.CVersion;
import com.io7m.changelog.parser.api.CParseErrorHandlers;
import com.io7m.changelog.tests.CLTestDirectories;
import com.io7m.changelog.xml.CXMLChangelogParsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.math.BigInteger.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CLCommandLineTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CLCommandLineTest.class);

  private ByteArrayOutputStream output;
  private PrintStream outputPrint;
  private Path directory;
  private Path directoryOutput;
  private Path outputPath;

  private static CChangelog parse(
    final Path path)
    throws IOException
  {
    try (var stream = Files.newInputStream(path)) {
      final var handler =
        CParseErrorHandlers.loggingHandler(LOG);
      final var parser =
        new CXMLChangelogParsers()
          .create(path.toUri(), stream, handler);

      return parser.parse();
    }
  }

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.directory =
      CLTestDirectories.createTempDirectory();
    this.directoryOutput =
      CLTestDirectories.createTempDirectory();

    this.outputPath = this.directoryOutput.resolve("README-TEST.xml");
    this.output = new ByteArrayOutputStream();
    this.outputPrint = new PrintStream(this.output);
    System.setOut(null);
    System.setErr(null);
  }

  void flush()
  {
    try {
      this.outputPrint.flush();
      this.output.flush();
    } catch (final IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }

  @Test
  public void testNoArguments()
    throws IOException
  {
    assertThrows(IOException.class, () -> {
      MainExitless.main(new String[]{

      });
    });
  }

  @Test
  public void testHelp()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "help"
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.contains("Commands:"));
    LOG.debug("{}", text);
  }

  @Test
  public void testHelpHelp()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "help", "version"
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.contains("Usage: version"));
    LOG.debug("{}", text);
  }

  @Test
  public void testHelpAddChange()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "help",
      "change-add"
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.contains("Usage: change-add"));
    assertTrue(text.contains("The change-add command"));
    LOG.debug("{}", text);
  }

  @Test
  public void testHelpInitialize()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "help",
      "initialize"
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.contains("Usage: initialize"));
    assertTrue(text.contains("The initialize command"));
    LOG.debug("{}", text);
  }

  @Test
  public void testHelpWritePlain()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "help", "write-plain"
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.contains("Usage: write-plain"));
    LOG.debug("{}", text);
  }

  @Test
  public void testHelpWriteXHTML()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "help", "write-xhtml"
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.contains("Usage: write-xhtml"));
    LOG.debug("{}", text);
  }

  @Test
  public void testHelpWriteAtom()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "help", "write-atom"
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.contains("Usage: write-atom"));
    LOG.debug("{}", text);
  }

  @Test
  public void testHelpReleaseBegin()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "help", "release-begin"
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.contains("Usage: release-begin"));
    LOG.debug("{}", text);
  }

  @Test
  public void testHelpReleaseFinish()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "help", "release-finish"
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.contains("Usage: release-finish"));
    LOG.debug("{}", text);
  }

  @Test
  public void testHelpReleaseSetVersion()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "help", "release-set-version"
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.contains("Usage: release-set-version"));
    LOG.debug("{}", text);
  }

  @Test
  public void testVersion()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "version"
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.trim().startsWith("changelog"));
    LOG.debug("{}", text);
  }

  @Test
  public void testInitialize()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.isEmpty());
    LOG.debug("{}", text);

    assertTrue(Files.isRegularFile(this.outputPath));
  }

  @Test
  public void testInitializeAlreadyExists()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    assertThrows(IOException.class, () -> {
      MainExitless.main(new String[]{
        "initialize",
        "--file",
        this.outputPath.toString(),
        "--ticket-system-name",
        "com.github.io7m.changelog.test",
        "--ticket-system-uri",
        "https://www.github.com/io7m/changelog/issues/",
        "--project",
        "com.io7m.changelog.test"
      });
    });
  }

  @Test
  public void testInitializeTooFewArgs()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    assertThrows(IOException.class, () -> {
      MainExitless.main(new String[]{
        "initialize",
        "--verbose",
        "trace",
        "--file",
        this.outputPath.toString(),
      });
    });

    this.flush();
    final var text = this.output.toString();
    LOG.debug("{}", text);
  }

  @Test
  public void testInitializeBrokenProjectName()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    assertThrows(IOException.class, () -> {
      MainExitless.main(new String[]{
        "initialize",
        "--file",
        this.outputPath.toString(),
        "--ticket-system-name",
        "com.github.io7m.changelog.test",
        "--ticket-system-uri",
        "https://www.github.com/io7m/changelog/issues/",
        "--project",
        ""
      });
    });
  }

  @Test
  public void testAddChangeNoRelease()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    assertThrows(IOException.class, () -> {
      MainExitless.main(new String[]{
        "change-add",
        "--file",
        this.outputPath.toString(),
        "--summary",
        "Some text"
      });
    });
  }

  @Test
  public void testAddChangeWithTicketsAndModule()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString(),
      "--version",
      "1.0.0"
    });

    MainExitless.main(new String[]{
      "change-add",
      "--file",
      this.outputPath.toString(),
      "--summary",
      "Some text",
      "--ticket",
      "1",
      "--ticket",
      "2",
      "--ticket",
      "3",
      "--module",
      "com.io7m.test"
    });

    final var changelog = parse(this.outputPath);
    final var release = changelog.latestRelease().get();
    assertEquals("1.0.0", String.format("%s", release.version()));
    final var change = release.changes().get(0);
    assertEquals("Some text", change.summary());
    assertEquals("1", change.tickets().get(0).value());
    assertEquals("2", change.tickets().get(1).value());
    assertEquals("3", change.tickets().get(2).value());
    assertEquals("com.io7m.test", change.module().get().value());
  }

  @Test
  public void testAddChangeWithBrokenTicket()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString(),
      "--version",
      "1.0.0"
    });

    assertThrows(IOException.class, () -> {
      MainExitless.main(new String[]{
        "change-add",
        "--file",
        this.outputPath.toString(),
        "--summary",
        "Some text",
        "--ticket",
        ""
      });
    });

    final var changelog = parse(this.outputPath);
    final var release = changelog.latestRelease().get();
    assertEquals("1.0.0", String.format("%s", release.version()));
    assertEquals(0, release.changes().size());
  }

  @Test
  public void testAddChangeWithBrokenModule()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString(),
      "--version",
      "1.0.0"
    });

    assertThrows(IOException.class, () -> {
      MainExitless.main(new String[]{
        "change-add",
        "--file",
        this.outputPath.toString(),
        "--summary",
        "Some text",
        "--module",
        ""
      });
    });

    final var changelog = parse(this.outputPath);
    final var release = changelog.latestRelease().get();
    assertEquals("1.0.0", String.format("%s", release.version()));
    assertEquals(0, release.changes().size());
  }

  @Test
  public void testAddReleaseAlreadyExists()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString(),
      "--version",
      "1.0.0"
    });

    assertThrows(IOException.class, () -> {
      MainExitless.main(new String[]{
        "release-begin",
        "--file",
        this.outputPath.toString(),
        "--version",
        "1.0.0"
      });
    });
  }

  @Test
  public void testAtom()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString(),
      "--version",
      "1.0.0"
    });

    MainExitless.main(new String[]{
      "change-add",
      "--file",
      this.outputPath.toString(),
      "--summary",
      "Some text",
      "--ticket",
      "1",
      "--ticket",
      "2",
      "--ticket",
      "3",
      "--module",
      "com.io7m.test"
    });

    MainExitless.main(new String[]{
      "write-atom",
      "--file",
      this.outputPath.toString(),
      "--title",
      "Atom Feed",
      "--author-name",
      "Test Author",
      "--author-email",
      "someone@example.com",
      "--uri",
      "http://www.example.com/"
    });
  }

  @Test
  public void testPlain()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString(),
      "--version",
      "1.0.0"
    });

    MainExitless.main(new String[]{
      "change-add",
      "--file",
      this.outputPath.toString(),
      "--summary",
      "Some text",
      "--ticket",
      "1",
      "--ticket",
      "2",
      "--ticket",
      "3",
      "--module",
      "com.io7m.test"
    });

    MainExitless.main(new String[]{
      "write-plain",
      "--file",
      this.outputPath.toString(),
      "--version",
      "1.0.0"
    });

    MainExitless.main(new String[]{
      "write-plain",
      "--file",
      this.outputPath.toString()
    });

    MainExitless.main(new String[]{
      "write-plain",
      "--file",
      this.outputPath.toString(),
      "--show-dates",
      "true"
    });
  }

  @Test
  public void testXHTML()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString(),
      "--version",
      "1.0.0"
    });

    MainExitless.main(new String[]{
      "change-add",
      "--file",
      this.outputPath.toString(),
      "--summary",
      "Some text",
      "--ticket",
      "1",
      "--ticket",
      "2",
      "--ticket",
      "3",
      "--module",
      "com.io7m.test"
    });

    MainExitless.main(new String[]{
      "write-xhtml",
      "--file",
      this.outputPath.toString()
    });

    MainExitless.main(new String[]{
      "write-xhtml",
      "--file",
      this.outputPath.toString(),
      "--version",
      "1.0.0"
    });

    MainExitless.main(new String[]{
      "write-xhtml",
      "--file",
      this.outputPath.toString()
    });
  }

  @Test
  public void testXHTMLNonexistentRelease0()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString(),
      "--version",
      "1.0.0"
    });

    MainExitless.main(new String[]{
      "change-add",
      "--file",
      this.outputPath.toString(),
      "--summary",
      "Some text",
      "--ticket",
      "1",
      "--ticket",
      "2",
      "--ticket",
      "3",
      "--module",
      "com.io7m.test"
    });

    assertThrows(IOException.class, () -> {
      MainExitless.main(new String[]{
        "write-xhtml",
        "--file",
        this.outputPath.toString(),
        "--release",
        "2.0.0"
      });
    });
  }

  @Test
  public void testPlainNonexistentRelease0()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString(),
      "--version",
      "1.0.0"
    });

    MainExitless.main(new String[]{
      "change-add",
      "--file",
      this.outputPath.toString(),
      "--summary",
      "Some text",
      "--ticket",
      "1",
      "--ticket",
      "2",
      "--ticket",
      "3",
      "--module",
      "com.io7m.test"
    });

    assertThrows(IOException.class, () -> {
      MainExitless.main(new String[]{
        "write-plain",
        "--file",
        this.outputPath.toString(),
        "--release",
        "2.0.0"
      });
    });
  }

  @Test
  public void testReleaseBegin()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString()
    });

    final var changelog = parse(this.outputPath);
    final var release = changelog.latestRelease().get();
    assertEquals("1.0.0", String.format("%s", release.version()));
    assertEquals(0, release.changes().size());
    assertTrue(release.isOpen());
  }

  @Test
  public void testReleaseBeginNext()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString(),
      "--version",
      "1.0.0"
    });

    MainExitless.main(new String[]{
      "release-finish",
      "--file",
      this.outputPath.toString()
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString()
    });

    final var changelog = parse(this.outputPath);
    final var release = changelog.latestRelease().get();
    assertEquals("1.1.0", String.format("%s", release.version()));
    assertEquals(0, release.changes().size());
    assertTrue(release.isOpen());
  }

  @Test
  public void testReleaseFinish()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString()
    });

    MainExitless.main(new String[]{
      "release-finish",
      "--file",
      this.outputPath.toString()
    });

    final var changelog = parse(this.outputPath);
    final var release = changelog.latestRelease().get();
    assertEquals("1.0.0", String.format("%s", release.version()));
    assertEquals(0, release.changes().size());
    assertFalse(release.isOpen());
  }

  @Test
  public void testReleaseSetVersion()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString()
    });

    MainExitless.main(new String[]{
      "release-finish",
      "--file",
      this.outputPath.toString()
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString()
    });

    MainExitless.main(new String[]{
      "release-set-version",
      "--file",
      this.outputPath.toString(),
      "--version",
      "2.0.0"
    });

    final var changelog = parse(this.outputPath);

    final var release0 =
      changelog.releases()
        .get(CVersion.of(ONE, ZERO, ZERO));

    assertEquals(2, changelog.releases().size());
    assertEquals("1.0.0", String.format("%s", release0.version()));
    assertFalse(release0.isOpen());

    final var release1 = changelog.latestRelease().get();
    assertEquals("2.0.0", String.format("%s", release1.version()));
    assertEquals(0, release1.changes().size());
    assertTrue(release1.isOpen());
  }

  @Test
  public void testReleaseSetVersionClosed()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString()
    });

    MainExitless.main(new String[]{
      "release-finish",
      "--file",
      this.outputPath.toString()
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString()
    });

    MainExitless.main(new String[]{
      "release-finish",
      "--file",
      this.outputPath.toString()
    });

    assertThrows(IOException.class, () -> {
      MainExitless.main(new String[]{
        "release-set-version",
        "--file",
        this.outputPath.toString(),
        "--version",
        "2.0.1"
      });
    });
  }

  @Test
  public void testReleaseSetVersionEmpty()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    assertThrows(IOException.class, () -> {
      MainExitless.main(new String[]{
        "release-set-version",
        "--file",
        this.outputPath.toString(),
        "--version",
        "2.0.1"
      });
    });
  }

  @Test
  public void testReleaseCurrent()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    Files.deleteIfExists(this.outputPath);

    MainExitless.main(new String[]{
      "initialize",
      "--file",
      this.outputPath.toString(),
      "--ticket-system-name",
      "com.github.io7m.changelog.test",
      "--ticket-system-uri",
      "https://www.github.com/io7m/changelog/issues/",
      "--project",
      "com.io7m.changelog.test"
    });

    assertThrows(IOException.class, () -> {
      MainExitless.main(new String[]{
        "release-current",
        "--file",
        this.outputPath.toString()
      });
    });

    MainExitless.main(new String[]{
      "release-begin",
      "--file",
      this.outputPath.toString()
    });

    MainExitless.main(new String[]{
      "release-current",
      "--file",
      this.outputPath.toString()
    });

    MainExitless.main(new String[]{
      "release-finish",
      "--file",
      this.outputPath.toString()
    });

    MainExitless.main(new String[]{
      "release-current",
      "--file",
      this.outputPath.toString()
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.contains("No current release exists"));
    assertTrue(text.contains("1.0.0 (open)"));
    assertTrue(text.contains("1.0.0 (closed)"));
    LOG.debug("{}", text);
  }
}

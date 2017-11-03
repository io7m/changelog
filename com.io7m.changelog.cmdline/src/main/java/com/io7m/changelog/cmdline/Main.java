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

package com.io7m.changelog.cmdline;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CVersionType;
import com.io7m.changelog.core.CVersions;
import com.io7m.changelog.text.CChangelogTextWriter;
import com.io7m.changelog.text.CChangelogTextWriterConfiguration;
import com.io7m.changelog.xom.CAtomFeedMeta;
import com.io7m.changelog.xom.CChangelogAtomWriter;
import com.io7m.changelog.xom.CChangelogXMLReader;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Main command line entry point.
 */

public final class Main implements Runnable
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(Main.class);
  }

  private final Map<String, CommandType> commands;
  private final JCommander commander;
  private final String[] args;
  private int exit_code;

  private Main(
    final String[] in_args)
  {
    this.args =
      Objects.requireNonNull(in_args, "Command line arguments");

    final CommandRoot r = new CommandRoot();
    final CommandVersion version = new CommandVersion();
    final CommandAtom atom = new CommandAtom();
    final CommandPlain plain = new CommandPlain();

    this.commands = new HashMap<>(8);
    this.commands.put("atom", atom);
    this.commands.put("plain", plain);
    this.commands.put("version", version);

    this.commander = new JCommander(r);
    this.commander.setProgramName("changelog");
    this.commander.addCommand("atom", atom);
    this.commander.addCommand("plain", plain);
    this.commander.addCommand("version", version);
  }

  /**
   * The main entry point.
   *
   * @param args Command line arguments
   */

  public static void main(final String[] args)
  {
    final Main cm = new Main(args);
    cm.run();
    System.exit(cm.exitCode());
  }

  /**
   * @return The program exit code
   */

  public int exitCode()
  {
    return this.exit_code;
  }

  @Override
  public void run()
  {
    try {
      this.commander.parse(this.args);

      final String cmd = this.commander.getParsedCommand();
      if (cmd == null) {
        final StringBuilder sb = new StringBuilder(128);
        this.commander.usage(sb);
        LOG.info("Arguments required.\n{}", sb.toString());
        this.exit_code = 1;
        return;
      }

      final CommandType command = this.commands.get(cmd);
      command.execute();
    } catch (final ParameterException e) {
      final StringBuilder sb = new StringBuilder(128);
      this.commander.usage(sb);
      LOG.error("{}\n{}", e.getMessage(), sb.toString());
      this.exit_code = 1;
    } catch (final Exception e) {
      LOG.error("{}", e.getMessage(), e);
      this.exit_code = 1;
    }
  }

  private interface CommandType
  {
    void execute()
      throws Exception;
  }

  private class CommandRoot implements CommandType
  {
    @Parameter(
      names = "-verbose",
      converter = CLLogLevelConverter.class,
      description = "Set the minimum logging verbosity level")
    private CLLogLevel verbose = CLLogLevel.LOG_INFO;

    CommandRoot()
    {

    }

    @Override
    public void execute()
      throws Exception
    {
      final ch.qos.logback.classic.Logger root =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
          Logger.ROOT_LOGGER_NAME);
      root.setLevel(this.verbose.toLevel());
      LOG.trace("start");
    }
  }

  @Parameters(commandDescription = "Retrieve the program version")
  private final class CommandVersion extends CommandRoot
  {
    CommandVersion()
    {

    }

    @Override
    public void execute()
      throws Exception
    {
      super.execute();

      final Package p = this.getClass().getPackage();
      System.out.printf(
        "%s %s %s\n",
        p.getImplementationVendor(),
        p.getImplementationTitle(),
        p.getImplementationVersion());
    }
  }

  @Parameters(commandDescription = "Generate an atom feed")
  private final class CommandAtom extends CommandRoot
  {
    @Parameter(
      names = "-file",
      required = true,
      description = "The changelog file")
    private String file;

    @Parameter(
      names = "-author-email",
      required = true,
      description = "The author email address")
    private String author_email;

    @Parameter(
      names = "-author-name",
      required = true,
      description = "The author name")
    private String author_name;

    @Parameter(
      names = "-title",
      required = true,
      description = "The feed title")
    private String title;

    @Parameter(
      names = "-uri",
      required = true,
      description = "The feed URI")
    private URI uri;

    CommandAtom()
    {

    }

    @Override
    public void execute()
      throws Exception
    {
      super.execute();

      final Path path = Paths.get(this.file);
      try (InputStream stream = Files.newInputStream(path)) {
        final CChangelog clog =
          CChangelogXMLReader.readFromStream(path.toUri(), stream);

        final CAtomFeedMeta meta =
          CAtomFeedMeta.builder()
            .setAuthorEmail(this.author_email)
            .setAuthorName(this.author_name)
            .setTitle(this.title)
            .setUri(this.uri)
            .build();

        final Element e = CChangelogAtomWriter.writeElement(meta, clog);
        final Serializer s = new Serializer(System.out, "UTF-8");
        s.setIndent(2);
        s.setMaxLength(80);
        s.write(new Document(e));
        s.flush();
      }
    }
  }

  @Parameters(commandDescription = "Generate a plain text log")
  private final class CommandPlain extends CommandRoot
  {
    @Parameter(
      names = "-file",
      required = true,
      description = "The changelog file")
    private String file;

    @Parameter(
      names = "-release",
      description = "The release")
    private String release;

    @Parameter(
      names = "-show-dates",
      description = "Show dates")
    private boolean date;

    CommandPlain()
    {

    }

    @Override
    public void execute()
      throws Exception
    {
      super.execute();

      final Optional<CVersionType> version;
      if (this.release != null) {
        version = Optional.of(CVersions.parse(this.release));
      } else {
        version = Optional.empty();
      }

      final Path path = Paths.get(this.file);
      try (InputStream stream = Files.newInputStream(path)) {
        final CChangelog clog =
          CChangelogXMLReader.readFromStream(path.toUri(), stream);
        final CChangelogTextWriterConfiguration.Builder config_b =
          CChangelogTextWriterConfiguration.builder()
            .setRelease(version)
            .setShowDates(this.date);

        try (PrintWriter writer =
               new PrintWriter(
                 new OutputStreamWriter(System.out, StandardCharsets.UTF_8))) {
          CChangelogTextWriter.writeChangelog(clog, config_b.build(), writer);
        }
      }
    }
  }
}

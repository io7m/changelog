package com.io7m.changelog.tests;

import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.parser.api.CParseErrorHandlers;
import com.io7m.changelog.text.api.CPlainChangelogWriterType;
import com.io7m.changelog.text.vanilla.CPlainChangelogWriters;
import com.io7m.changelog.xml.CXMLChangelogParsers;
import com.io7m.changelog.xml.CXMLChangelogWriters;
import com.io7m.changelog.xml.api.CXMLChangelogParserType;
import com.io7m.changelog.xml.api.CXMLChangelogWriterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class XMLTest
{
  private static final Logger LOG = LoggerFactory.getLogger(XMLTest.class);

  private XMLTest()
  {

  }

  public static void main(
    final String[] args)
    throws IOException
  {
    final CXMLChangelogParsers parsers = new CXMLChangelogParsers();

    final Path path = Paths.get(args[0]);
    try (InputStream stream = Files.newInputStream(path)) {
      final CXMLChangelogParserType parser =
        parsers.create(
          path.toUri(),
          stream,
          CParseErrorHandlers.loggingHandler(LOG));
      final CChangelog c = parser.parse();
      LOG.debug("changelog: {}", c);

      final CXMLChangelogWriterType writer =
        new CXMLChangelogWriters().create(URI.create("urn:x"), System.out);

      writer.write(c);

      final CPlainChangelogWriterType text =
        new CPlainChangelogWriters().create(URI.create("urn:x"), System.out);

      text.write(c);
    }
  }
}

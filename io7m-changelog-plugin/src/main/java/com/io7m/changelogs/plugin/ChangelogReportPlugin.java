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

package com.io7m.changelogs.plugin;

import java.io.File;
import java.net.URI;
import java.util.Locale;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import com.io7m.changelog.xom.CAtomFeedMeta;
import com.io7m.changelog.xom.CAtomFeedMetaBuilderType;

/**
 * The changelog page report plugin.
 * 
 * @goal changelog
 * 
 * @phase site
 */

public final class ChangelogReportPlugin extends AbstractMavenReport
{
  /**
   * The file containing an XML changelog.
   * 
   * @parameter
   */

  private File         file;

  /**
   * The current maven project.
   * 
   * @component
   */

  private MavenProject project;

  /**
   * The name of the Atom feed author.
   * 
   * @parameter
   */

  private String       feedAuthorName;

  /**
   * The email of the Atom feed author.
   * 
   * @parameter
   */

  private String       feedAuthorEmail;

  @Override public String getOutputName()
  {
    return "changes";
  }

  @Override public String getName(
    final Locale locale)
  {
    return "Changelog generation";
  }

  @Override public String getDescription(
    final Locale locale)
  {
    return "Generate a changelog page.";
  }

  @Override protected Renderer getSiteRenderer()
  {
    // TODO Auto-generated method stub
    throw new AssertionError("Unimplemented");
  }

  @Override protected String getOutputDirectory()
  {
    // TODO Auto-generated method stub
    throw new AssertionError("Unimplemented");
  }

  @Override protected MavenProject getProject()
  {
    return this.project;
  }

  @Override protected void executeReport(
    final Locale locale)
    throws MavenReportException
  {
    try {
      final Log logger = this.getLog();
      logger.info("Generating changelog report for " + this.file);

      ChangelogReportPlugin.checkParameter(this.file, "Feed file", "file");
      ChangelogReportPlugin.checkParameter(
        this.feedAuthorName,
        "Feed author name",
        "feedAuthorName");
      ChangelogReportPlugin.checkParameter(
        this.feedAuthorEmail,
        "Feed author email",
        "feedAuthorEmail");

      final File atom =
        new File(this.getReportOutputDirectory(), "releases.atom");

      final CAtomFeedMetaBuilderType builder = CAtomFeedMeta.newBuilder();
      builder.setAuthorEmail(this.feedAuthorEmail);
      builder.setAuthorName(this.feedAuthorName);
      builder.setTitle(this.project.getName() + " Releases");
      builder.setURI(new URI(this.project.getUrl() + "/releases.atom"));
      final CAtomFeedMeta meta = builder.build();

      final ChangelogReport r =
        new ChangelogReport(meta, atom, this.file.toURI(), this.getSink());

      r.run();
    } catch (final Exception e) {
      this.handleException(e);
    }
  }

  private static void checkParameter(
    final Object p,
    final String description,
    final String name)
  {
    if (p == null) {
      throw new IllegalArgumentException(String.format(
        "%s (%s) parameter not set",
        description,
        name));
    }
  }

  private void handleException(
    final Exception e)
    throws MavenReportException
  {
    final Log logger = this.getLog();
    logger.error(e.getMessage());
    throw new MavenReportException(e.getMessage(), e);
  }
}

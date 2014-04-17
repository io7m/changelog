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

package com.io7m.changelog.text;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.io7m.changelog.core.CChangeType;
import com.io7m.changelog.core.CChangelog;
import com.io7m.changelog.core.CChangelogBuilderType;
import com.io7m.changelog.core.CItem;
import com.io7m.changelog.core.CRelease;
import com.io7m.changelog.core.CVersionStandard;

public class CChangelogTextWriterTest
{
  @SuppressWarnings("static-method") @Test public void testPlain()
    throws URISyntaxException,
      ParseException
  {
    final CChangelogBuilderType b = CChangelog.newBuilder();
    b.setProjectName("example");
    b.addTicketSystem("t", new URI("http://example.com/tickets/"));
    final Date date = new SimpleDateFormat("yyyy-MM-dd").parse("2014-01-01");
    final List<CItem> items = new ArrayList<CItem>();

    items.add(CItem.newItem(
      new ArrayList<String>(),
      "Summary 0",
      date,
      CChangeType.CHANGE_TYPE_CODE_FIX));

    final ArrayList<String> tickets = new ArrayList<String>();
    tickets.add("23");
    tickets.add("48");
    items.add(CItem.newItem(
      tickets,
      "Summary 1",
      date,
      CChangeType.CHANGE_TYPE_CODE_CHANGE));

    items.add(CItem.newItem(
      new ArrayList<String>(),
      "Summary 2",
      date,
      CChangeType.CHANGE_TYPE_CODE_NEW));

    b.addRelease(CRelease.newRelease(
      "t",
      date,
      CVersionStandard.parse("1.0.0"),
      items));

    final PrintWriter writer = new PrintWriter(System.out);
    CChangelogTextWriter.writeChangelog(b.build(), writer);
  }
}

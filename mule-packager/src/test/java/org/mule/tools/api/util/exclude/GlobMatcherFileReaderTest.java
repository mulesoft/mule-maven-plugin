/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.util.exclude;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;

public class GlobMatcherFileReaderTest {

  private GlobMatcherFileReader reader;

  @Before
  public void setUp() throws IOException {
    reader = new GlobMatcherFileReader();
  }

  @Test
  public void blankLineTest() throws IOException {
    processLines("   ");
    assertThat("Result should be empty", reader.getResult().isEmpty(), equalTo(true));
  }

  @Test
  public void emptyLineTest() throws IOException {
    processLines(StringUtils.EMPTY);
    assertThat("Result should be empty", reader.getResult().isEmpty(), equalTo(true));
  }

  @Test
  public void commentLineTest() throws IOException {
    processLines("# this is a comment");
    assertThat("Result should be empty", reader.getResult().isEmpty(), equalTo(true));
  }

  @Test
  public void commentLineTrailingSpacesTest() throws IOException {
    processLines(" # this is a comment");
    assertThat("Result should be empty", reader.getResult().isEmpty(), equalTo(true));
  }

  @Test
  public void oneLineTest() throws IOException {
    processLines("*");
    assertThat("Result should contain one matcher", reader.getResult().size(), equalTo(1));
  }

  @Test
  public void twoLinesTest() throws IOException {
    processLines("*", "*.java");
    assertThat("Result should contain two matchers", reader.getResult().size(), equalTo(2));
  }

  @Test
  public void manyLinesWithCommentsAndBlankLinesTest() throws IOException {
    processLines("# This is a comment", "*.java", "*.cpp", StringUtils.EMPTY, "*.class");
    assertThat("Result should contain three matchers", reader.getResult().size(), equalTo(3));
  }

  private void processLines(String... lines) throws IOException {
    for (String line : lines) {
      reader.processLine(line);
    }
  }
}

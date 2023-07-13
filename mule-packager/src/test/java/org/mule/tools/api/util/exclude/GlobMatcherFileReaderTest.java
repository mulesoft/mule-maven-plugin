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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobMatcherFileReaderTest {

  private GlobMatcherFileReader reader;

  @BeforeEach
  public void setUp() throws IOException {
    reader = new GlobMatcherFileReader();
  }

  @Test
  public void blankLineTest() throws IOException {
    processLines("   ");
    assertThat(reader.getResult().isEmpty()).describedAs("Result should be empty").isTrue();
  }

  @Test
  public void emptyLineTest() throws IOException {
    processLines(StringUtils.EMPTY);
    assertThat(reader.getResult().isEmpty()).describedAs("Result should be empty").isTrue();
  }

  @Test
  public void commentLineTest() throws IOException {
    processLines("# this is a comment");
    assertThat(reader.getResult().isEmpty()).describedAs("Result should be empty").isTrue();
  }

  @Test
  public void commentLineTrailingSpacesTest() throws IOException {
    processLines(" # this is a comment");
    assertThat(reader.getResult().isEmpty()).describedAs("Result should be empty").isTrue();
  }

  @Test
  public void oneLineTest() throws IOException {
    processLines("*");
    assertThat(reader.getResult().size()).describedAs("Result should contain one matcher").isEqualTo(1);
  }

  @Test
  public void twoLinesTest() throws IOException {
    processLines("*", "*.java");
    assertThat(reader.getResult().size()).describedAs("Result should contain two matchers").isEqualTo(2);
  }

  @Test
  public void manyLinesWithCommentsAndBlankLinesTest() throws IOException {
    processLines("# This is a comment", "*.java", "*.cpp", StringUtils.EMPTY, "*.class");
    assertThat(reader.getResult().size()).describedAs("Result should contain three matchers").isEqualTo(3);
  }

  private void processLines(String... lines) throws IOException {
    for (String line : lines) {
      reader.processLine(line);
    }
  }
}

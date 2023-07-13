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


import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
//import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
//import java.util.ArrayList;
import java.util.Arrays;

import static org.apache.commons.io.FileUtils.writeLines;
//import static org.hamcrest.core.Is.is;
//import static org.hamcrest.core.IsEqual.equalTo;
//import static org.junit.Assert.assertThat;
//import static org.junit.Assert.fail;
//import static org.junit.Assume.assumeThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mule.tools.api.util.exclude.MuleExclusionMatcher.MULE_EXCLUDE_FILENAME;

public class MuleExclusionMatcherTest {

  @TempDir
  public Path temporaryFolder;
  //  @Rule
  //  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private File muleExcludeFile;
  private Path projectBaseFolder;
  private MuleExclusionMatcher matcher;

  @BeforeEach
  public void setUp() throws IOException {
    //    temporaryFolder.create();
    muleExcludeFile = temporaryFolder.resolve(MULE_EXCLUDE_FILENAME).toFile();
    projectBaseFolder = Paths.get(temporaryFolder.toAbsolutePath().toUri());
  }

  @Test
  public void emptyFileTest() throws IOException {
    buildMuleExclusionMatcher();
  }

  @Test
  public void nonExistingFileTest() {
    FileUtils.deleteQuietly(muleExcludeFile);
    assertThat(muleExcludeFile.exists()).isFalse();
    buildMuleExclusionMatcher();
  }

  @Test
  public void customMuleExcludeFileTest() throws IOException {
    File shouldMatchConfigFile1 = temporaryFolder.resolve("a.xml").toFile();
    //    temporaryFolder.newFolder("src", "main", "mule");
    File shouldMatchConfigFile2 = temporaryFolder.resolve("src/main/mule/lala.xml").toFile();//temporaryFolder.newFile("src/main/mule/lala.xml");

    File shouldNotMatchConfigFile = temporaryFolder.resolve("b.xml").toFile();//temporaryFolder.newFile("b.xml");
    writeLinesToMuleExcludeFile("a.xml", "**/*/lala.xml", "# comment");
    buildMuleExclusionMatcher();

    assertThat(matcher.matches(shouldMatchConfigFile1.toPath())).describedAs("This file should be matched").isTrue();
    assertThat(matcher.matches(shouldMatchConfigFile2.toPath())).describedAs("This file should be matched").isTrue();
    assertThat(matcher.matches(shouldNotMatchConfigFile.toPath())).describedAs("This file should not be matched").isFalse();
  }

  private void writeLinesToMuleExcludeFile(String... lines) throws IOException {
    writeLines(muleExcludeFile, Arrays.asList(lines));
  }

  private void buildMuleExclusionMatcher() {
    try {
      matcher = new MuleExclusionMatcher(projectBaseFolder);
    } catch (IOException e) {
      fail("Mule exclusion matcher should have been created");
    }
  }
}

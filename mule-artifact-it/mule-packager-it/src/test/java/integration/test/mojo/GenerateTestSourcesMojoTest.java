/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo;

import static org.hamcrest.MatcherAssert.assertThat;
import static integration.FileTreeMatcher.hasSameTreeStructure;

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.junit.Before;
import org.junit.Test;


public class GenerateTestSourcesMojoTest extends MojoTest implements SettingsConfigurator {

  private static final String GENERATE_TEST_SOURCES = "generate-test-sources";

  public GenerateTestSourcesMojoTest() {
    this.goal = GENERATE_TEST_SOURCES;
  }

  @Before
  public void before() throws IOException {
    clearResources();
  }

  @Test
  public void testGenerateTestSources() throws IOException, VerificationException {
    verifier.executeGoal(GENERATE_TEST_SOURCES);
    File expectedStructure = getExpectedStructure();
    assertThat("The directory structure is different from the expected", targetFolder,
               hasSameTreeStructure(expectedStructure, excludes));

    verifier.verifyErrorFreeLog();
  }
}

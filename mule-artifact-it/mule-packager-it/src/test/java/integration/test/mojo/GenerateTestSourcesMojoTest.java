/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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

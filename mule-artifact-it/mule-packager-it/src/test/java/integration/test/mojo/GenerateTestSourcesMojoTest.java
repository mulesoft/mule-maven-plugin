/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo;

import static org.hamcrest.MatcherAssert.assertThat;
import static integration.FileTreeMatcher.hasSameTreeStructure;

import java.io.File;
import java.io.IOException;

import org.apache.maven.shared.verifier.VerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GenerateTestSourcesMojoTest extends MojoTest implements SettingsConfigurator {

  private static final String GENERATE_TEST_SOURCES = "generate-test-sources";

  public GenerateTestSourcesMojoTest() {
    this.goal = GENERATE_TEST_SOURCES;
  }

  @BeforeEach
  void before() throws IOException {
    clearResources();
  }

  @Test
  void testGenerateTestSources() throws IOException, VerificationException {
    verifier.addCliArgument(GENERATE_TEST_SOURCES);
    verifier.execute();
    File expectedStructure = getExpectedStructure();
    assertThat("The directory structure is different from the expected", targetFolder,
               hasSameTreeStructure(expectedStructure, excludes));
    verifier.verifyErrorFreeLog();
  }
}

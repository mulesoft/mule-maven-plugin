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

import org.apache.maven.it.VerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InitializeMojoTest extends MojoTest implements SettingsConfigurator {

  private static final String INITIALIZE = "initialize";

  public InitializeMojoTest() {
    this.goal = INITIALIZE;
  }

  @BeforeEach
  void before() throws IOException, VerificationException {
    clearResources();
    verifier.setSystemProperty(PROJECT_BASE_DIR_PROPERTY, projectBaseDirectory.getAbsolutePath());
    verifier.setSystemProperty(PROJECT_BUILD_DIRECTORY_PROPERTY, targetFolder.getAbsolutePath());
    setMuleMavenPluginVersion(verifier);
  }

  @Test
  void testInitializeOnEmptyProject() throws Exception {
    verifier.executeGoal(INITIALIZE);

    File expectedStructure = getExpectedStructure();
    assertThat("The directory structure is different from the expected", targetFolder,
               hasSameTreeStructure(expectedStructure, excludes));
    verifier.verifyErrorFreeLog();
  }
}

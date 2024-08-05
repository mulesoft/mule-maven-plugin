/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo;

import static integration.FileTreeMatcher.hasSameTreeStructure;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.maven.shared.verifier.VerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompileMojoTest extends MojoTest implements SettingsConfigurator {

  private static final String GOAL = "compile";

  public CompileMojoTest() {
    this.goal = GOAL;
  }

  @BeforeEach
  void before() throws IOException {
    clearResources();
  }

  @Test
  void testCompile() throws IOException, VerificationException {
    verifier.addCliArguments(GOAL);
    verifier.execute();
    File expectedStructure = getExpectedStructure();
    assertThat("The directory structure is different from the expected", targetFolder,
               hasSameTreeStructure(expectedStructure, excludesCompile));
    verifier.verifyErrorFreeLog();
  }
}

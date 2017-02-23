/**
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo;

import org.apache.maven.it.VerificationException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.mule.tools.maven.FileTreeMatcher.hasSameTreeStructure;
import static org.hamcrest.MatcherAssert.assertThat;

public class InitializeMojoTest extends MojoTest {

  private static final String INITIALIZE = "initialize";

  public InitializeMojoTest() {
    this.goal = INITIALIZE;
  }

  @Before
  public void before() throws IOException, VerificationException {
    clearResources();
    verifier.setSystemProperty(PROJECT_BASE_DIR_PROPERTY, projectBaseDirectory.getAbsolutePath());
    verifier.setSystemProperty(PROJECT_BUILD_DIRECTORY_PROPERTY, targetFolder.getAbsolutePath());
  }

  @Test
  public void testInitializeOnEmptyProject()
      throws Exception {
    verifier.executeGoal(INITIALIZE);

    File expectedStructure = getExpectedStructure();

    assertThat("The directory structure is different from the expected", targetFolder, hasSameTreeStructure(expectedStructure));

    verifier.verifyErrorFreeLog();
  }
}

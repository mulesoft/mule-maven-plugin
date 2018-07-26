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

import static integration.FileTreeMatcher.hasSameTreeStructure;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.junit.Before;
import org.junit.Test;

public class CompileMojoTest extends MojoTest implements SettingsConfigurator {

  private static final String GOAL = "compile";

  public CompileMojoTest() {
    this.goal = GOAL;
  }

  @Before
  public void before() throws IOException, VerificationException {
    clearResources();
  }

  @Test
  public void testCompile() throws IOException, VerificationException {
    verifier.executeGoal(GOAL);
    File expectedStructure = getExpectedStructure();
    assertThat("The directory structure is different from the expected", targetFolder,
               hasSameTreeStructure(expectedStructure, excludesCompile));
    verifier.verifyErrorFreeLog();
  }
}

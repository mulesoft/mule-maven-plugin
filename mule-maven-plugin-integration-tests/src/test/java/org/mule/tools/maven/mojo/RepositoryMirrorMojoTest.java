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
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mule.tools.maven.FileTreeMatcher.hasSameTreeStructure;

public class RepositoryMirrorMojoTest extends MojoTest {

  private static final String REPOSITORY_MIRROR = "repository-mirror";
  private static final String INSTALL = "install";
  private static final String DEPENDENCY_ORG_ID = "org.apache.maven.plugin.my.dependency";
  private static final String DEPENDENCY_NAME = "dependency-repository-mirror-project";
  private static final String DEPENDENCY_VERSION = "1.0-SNAPSHOT";
  private static final String DEPENDENCY_TYPE = "jar";
  private static final String DEPENDENCY_PROJECT_NAME = "dependency-repository-mirror-test";

  public RepositoryMirrorMojoTest() {
    this.goal = REPOSITORY_MIRROR;
  }

  @Before
  public void before() throws IOException, VerificationException {
    clearResources();
  }

  @Test
  public void testRepositoryMirror() throws IOException, VerificationException {
    installThirdPartyArtifact();

    verifier.executeGoal("org.mule.tools.maven:mule-maven-plugin:" + REPOSITORY_MIRROR);

    File expectedStructure = getExpectedStructure();

    assertThat("The directory structure is different from the expected", targetFolder, hasSameTreeStructure(expectedStructure));

    verifier.verifyErrorFreeLog();
  }

  private void installThirdPartyArtifact() throws IOException, VerificationException {
    File dependencyProjectRootFolder = builder.createProjectBaseDir(DEPENDENCY_PROJECT_NAME, this.getClass());
    Verifier auxVerifier = new Verifier(dependencyProjectRootFolder.getAbsolutePath());
    auxVerifier.deleteArtifact(DEPENDENCY_ORG_ID, DEPENDENCY_NAME, DEPENDENCY_VERSION, DEPENDENCY_TYPE);
    auxVerifier.assertArtifactNotPresent(DEPENDENCY_ORG_ID, DEPENDENCY_NAME, DEPENDENCY_VERSION, DEPENDENCY_TYPE);
    auxVerifier.executeGoal(INSTALL);
    auxVerifier.assertArtifactPresent(DEPENDENCY_ORG_ID, DEPENDENCY_NAME, DEPENDENCY_VERSION, DEPENDENCY_TYPE);
    auxVerifier.verifyErrorFreeLog();
  }
}

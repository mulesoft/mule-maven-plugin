/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integrationTests.mojo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mule.tools.maven.FileTreeMatcher.hasSameTreeStructure;

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Test;

public class ProcessSourcesMojoTest extends MojoTest {

  private static final String PROCESS_SOURCES = "process-sources";
  private static final String DEPENDENCY_A_GROUP_ID = "group.id.a";
  private static final String DEPENDENCY_A_ARTIFACT_ID = "artifact-id-a";
  private static final String DEPENDENCY_A_VERSION = "1.0.0-SNAPSHOT";
  private static final String DEPENDENCY_A_TYPE = "jar";
  private static final String DEPENDENCY_B_GROUP_ID = "group.id.b";
  private static final String DEPENDENCY_B_ARTIFACT_ID = "artifact-id-b";
  private static final String DEPENDENCY_B_VERSION = "1.0.0";
  private static final String DEPENDENCY_B_TYPE = "jar";
  private static final String DEPENDENCY_A_PROJECT_NAME = "dependency-a";
  private static final String DEPENDENCY_B_PROJECT_NAME = "dependency-b";
  private static final String DEPENDENCY_C_GROUP_ID = "group.id.c";
  private static final String DEPENDENCY_C_ARTIFACT_ID = "artifact-id-c";
  private static final String DEPENDENCY_C_VERSION = "1.0.0-SNAPSHOT";
  private static final String DEPENDENCY_C_TYPE = "jar";
  private static final String DEPENDENCY_D_GROUP_ID = "group.id.d";
  private static final String DEPENDENCY_D_ARTIFACT_ID = "artifact-id-d";
  private static final String DEPENDENCY_D_VERSION = "1.0.0";
  private static final String DEPENDENCY_D_TYPE = "jar";
  private static final String DEPENDENCY_C_PROJECT_NAME = "dependency-c";
  private static final String DEPENDENCY_D_PROJECT_NAME = "dependency-d";

  public ProcessSourcesMojoTest() {
    this.goal = PROCESS_SOURCES;
  }

  @Before
  public void before() throws IOException, VerificationException {
    clearResources();
  }

  @Test
  public void testProcessSources() throws IOException, VerificationException {
    installThirdPartyArtifact(DEPENDENCY_ORG_ID, DEPENDENCY_NAME, DEPENDENCY_VERSION, DEPENDENCY_TYPE, DEPENDENCY_PROJECT_NAME);

    verifier.executeGoal(PROCESS_SOURCES);

    File expectedStructure = getExpectedStructure();

    assertThat("The directory structure is different from the expected", targetFolder,
               hasSameTreeStructure(expectedStructure, excludes));

    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testProcessSourcesExcludedDependency() throws IOException, VerificationException {
    installThirdPartyArtifact(DEPENDENCY_A_GROUP_ID, DEPENDENCY_A_ARTIFACT_ID, DEPENDENCY_A_VERSION, DEPENDENCY_A_TYPE,
                              DEPENDENCY_A_PROJECT_NAME);
    installThirdPartyArtifact(DEPENDENCY_B_GROUP_ID, DEPENDENCY_B_ARTIFACT_ID, DEPENDENCY_B_VERSION, DEPENDENCY_B_TYPE,
                              DEPENDENCY_B_PROJECT_NAME);
    installThirdPartyArtifact(DEPENDENCY_D_GROUP_ID, DEPENDENCY_D_ARTIFACT_ID, DEPENDENCY_D_VERSION, DEPENDENCY_D_TYPE,
                              DEPENDENCY_D_PROJECT_NAME);
    installThirdPartyArtifact(DEPENDENCY_C_GROUP_ID, DEPENDENCY_C_ARTIFACT_ID, DEPENDENCY_C_VERSION, DEPENDENCY_C_TYPE,
                              DEPENDENCY_C_PROJECT_NAME);

    projectBaseDirectory = builder.createProjectBaseDir("empty-excluded-dependencies-project", this.getClass());
    verifier = new Verifier(projectBaseDirectory.getAbsolutePath());
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.setMavenDebug(true);
    verifier.executeGoal(PROCESS_SOURCES);

    File expectedStructure = getExpectedStructure("/integrationTests/expected-excluded-dependencies-structure");
    targetFolder = new File(expectedStructure.getParent(), "empty-excluded-dependencies-project/target");

    assertThat("The directory structure is different from the expected", targetFolder, hasSameTreeStructure(expectedStructure,
                                                                                                            excludes));
    verifier.verifyErrorFreeLog();
  }
}

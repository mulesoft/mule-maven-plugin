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


public class PackageMojoTest extends MojoTest {

  private static final String PACKAGE = "package";
  private static final String TARGET_FOLDER_NAME = "target";

  public PackageMojoTest() {
    this.goal = PACKAGE;
  }

  @Before
  public void before() throws IOException, VerificationException {
    clearResources();
  }

  @Test
  public void testPackageApp() throws IOException, VerificationException {
    installThirdPartyArtifact(DEPENDENCY_ORG_ID, DEPENDENCY_NAME, DEPENDENCY_VERSION, DEPENDENCY_TYPE, DEPENDENCY_PROJECT_NAME);
    verifier.executeGoal(PACKAGE);

    File expectedStructure = getExpectedStructure();

    assertThat("The directory structure is different from the expected", targetFolder,
               hasSameTreeStructure(expectedStructure, excludes));

    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testPackageAppWithSharedLibraries() throws IOException, VerificationException {
    installThirdPartyArtifact(DEPENDENCY_A_GROUP_ID, DEPENDENCY_A_ARTIFACT_ID, DEPENDENCY_A_VERSION, DEPENDENCY_A_TYPE,
                              DEPENDENCY_A_PROJECT_NAME);
    installThirdPartyArtifact(DEPENDENCY_B_GROUP_ID, DEPENDENCY_B_ARTIFACT_ID, DEPENDENCY_B_VERSION, DEPENDENCY_B_TYPE,
                              DEPENDENCY_B_PROJECT_NAME);
    String artifactId = "validate-shared-libraries-project";
    projectBaseDirectory = builder.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.setMavenDebug(true);
    verifier.executeGoal(PACKAGE);

    File expectedStructure = getExpectedStructure("/integrationTests/expected-package-app-shared-libraries");
    File targetStructure = new File(verifier.getBasedir() + File.separator + TARGET_FOLDER_NAME);

    assertThat("The directory structure is different from the expected", targetStructure,
               hasSameTreeStructure(expectedStructure, excludes));
    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testPackagePolicy() throws IOException, VerificationException {
    String artifactId = "empty-package-policy-project";
    projectBaseDirectory = builder.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.setMavenDebug(true);
    verifier.executeGoal(PACKAGE);

    File expectedStructure = getExpectedStructure("/integrationTests/expected-package-policy-structure");
    File targetStructure = new File(verifier.getBasedir() + File.separator + TARGET_FOLDER_NAME);

    assertThat("The directory structure is different from the expected", targetStructure,
               hasSameTreeStructure(expectedStructure, excludes));
    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testPackageMultiModuleAppModuleCorrectStructure() throws IOException, VerificationException {
    String artifactId = "multi-module-application";
    projectBaseDirectory = builder.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.setMavenDebug(true);
    verifier.executeGoal(PACKAGE);

    String moduleName = "empty-app";
    File expectedStructure = getExpectedStructure("/integrationTests/multi-module-application" + File.separator + moduleName
        + File.separator + TARGET_FOLDER_NAME);
    File targetStructure = new File(verifier.getBasedir() + File.separator + moduleName + File.separator + TARGET_FOLDER_NAME);

    assertThat("The directory structure is different from the expected", targetStructure,
               hasSameTreeStructure(expectedStructure, excludes));
    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testPackageMultiModulePolicyModuleCorrectStructure() throws IOException, VerificationException {
    String artifactId = "multi-module-application";
    projectBaseDirectory = builder.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.setMavenDebug(true);
    verifier.executeGoal(PACKAGE);

    String moduleName = "empty-policy";
    File expectedStructure = getExpectedStructure("/integrationTests/multi-module-application" + File.separator + moduleName
        + File.separator + TARGET_FOLDER_NAME);
    File targetStructure = new File(verifier.getBasedir() + File.separator + moduleName + File.separator + TARGET_FOLDER_NAME);

    assertThat("The directory structure is different from the expected", targetStructure,
               hasSameTreeStructure(expectedStructure, excludes));
    verifier.verifyErrorFreeLog();
  }

}

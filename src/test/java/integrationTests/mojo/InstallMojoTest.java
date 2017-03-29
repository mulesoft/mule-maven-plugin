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

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Test;


public class InstallMojoTest extends MojoTest {

  private static final String INSTALL = "install";
  private static final String GROUP_ID = "org.apache.maven.plugin.my.unit";
  private static final String ARTIFACT_ID = "empty-install-project";
  private static final String VERSION = "1.0-SNAPSHOT";
  private static final String EXT = "zip";
  private static final String MULE_POLICY_CLASSIFIER = "mule-policy";
  private static final String MULE_APPLICATION_CLASSIFIER = "mule-application";
  private static final String MULE_APPLICATION_CLASSIFIER_LIGHT_PACKAGE = "mule-application-light-package";

  public InstallMojoTest() {
    this.goal = INSTALL;
  }

  @Before
  public void before() throws IOException, VerificationException {
    clearResources();
  }

  @Test
  public void testInstall() throws IOException, VerificationException {
    verifier.addCliOption("-DattachMuleSources=true");
    verifier.deleteArtifacts(GROUP_ID, ARTIFACT_ID, VERSION);
    String artifactPath = verifier.getArtifactPath(GROUP_ID, ARTIFACT_ID, VERSION, EXT, MULE_APPLICATION_CLASSIFIER);
    File artifactFile = new File(artifactPath);
    assertThat("Artifact already exists", !artifactFile.exists());

    verifier.executeGoal(INSTALL);

    verifier.verifyErrorFreeLog();
    assertThat("Artifact was not installed in the .m2 repository", artifactFile.exists());
  }

  @Test
  public void testInstallPolicy() throws IOException, VerificationException {
    String artifactId = "empty-install-policy-project";
    projectBaseDirectory = builder.createProjectBaseDir(artifactId, this.getClass());
    verifier = new Verifier(projectBaseDirectory.getAbsolutePath());
    verifier.addCliOption("-DattachMuleSources=true");
    verifier.deleteArtifacts(GROUP_ID, artifactId, VERSION);
    String artifactPath = verifier.getArtifactPath(GROUP_ID, artifactId, VERSION, EXT, MULE_POLICY_CLASSIFIER);
    File artifactFile = new File(artifactPath);
    assertThat("Artifact already exists", !artifactFile.exists());
    verifier.executeGoal(INSTALL);

    verifier.verifyErrorFreeLog();

    assertThat("Artifact was not installed in the .m2 repository", artifactFile.exists());
  }

  @Test
  public void testInstallAppTemplate() throws IOException, VerificationException {

  }

  @Test
  public void testInstallAppExample() throws IOException, VerificationException {

  }

  @Test
  public void testInstallDomain() throws IOException, VerificationException {

  }

  @Test
  public void testInstallLightPackage() throws IOException, VerificationException {
    verifier.addCliOption("-DattachMuleSources=true");
    verifier.addCliOption("-DlightwayPackage=true");
    verifier.deleteArtifacts(GROUP_ID, ARTIFACT_ID, VERSION);
    String artifactPath =
        verifier.getArtifactPath(GROUP_ID, ARTIFACT_ID, VERSION, EXT, MULE_APPLICATION_CLASSIFIER_LIGHT_PACKAGE);
    File artifactFile = new File(artifactPath);
    assertThat("Artifact already exists", !artifactFile.exists());

    verifier.executeGoal(INSTALL);

    verifier.verifyErrorFreeLog();
    assertThat("Artifact was not installed in the .m2 repository", artifactFile.exists());
  }
}

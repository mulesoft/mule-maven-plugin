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

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.junit.Before;
import org.junit.Test;


public class InstallMojoTest extends MojoTest {

  private static final String INSTALL = "install";
  private static final String GROUP_ID = "org.apache.maven.plugin.my.unit";
  private static final String ARTIFACT_ID = "empty-install-project";
  private static final String VERSION = "1.0.0-SNAPSHOT";
  private static final String EXT = "jar";
  private static final String LIGHT_PACKAGE_EXT = "-light-package";
  private static final String MULE_POLICY_CLASSIFIER = "mule-policy";
  private static final String MULE_DOMAIN_CLASSIFIER = "mule-domain";
  private static final String MULE_APPLICATION_CLASSIFIER = "mule-application";
  private static final String MULE_APPLICATION_EXAMPLE_CLASSIFIER = "mule-application-example";
  private static final String MULE_APPLICATION_TEMPLATE_CLASSIFIER = "mule-application-template";
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
    verifier = buildVerifier(projectBaseDirectory);
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
  public void testInstallMustacheXmlPolicy() throws IOException, VerificationException {
    String artifactId = "custom-policy-test";
    projectBaseDirectory = builder.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
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
    String artifactId = "empty-install-app-template-project";
    projectBaseDirectory = builder.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-DattachMuleSources=true");
    verifier.deleteArtifacts(GROUP_ID, artifactId, VERSION);
    String artifactPath = verifier.getArtifactPath(GROUP_ID, artifactId, VERSION, EXT, MULE_APPLICATION_TEMPLATE_CLASSIFIER);
    File artifactFile = new File(artifactPath);
    assertThat("Artifact already exists", !artifactFile.exists());
    verifier.executeGoal(INSTALL);

    verifier.verifyErrorFreeLog();

    assertThat("Artifact was not installed in the .m2 repository", artifactFile.exists());
  }

  @Test
  public void testInstallAppExample() throws IOException, VerificationException {
    String artifactId = "empty-install-app-example-project";
    projectBaseDirectory = builder.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-DattachMuleSources=true");
    verifier.deleteArtifacts(GROUP_ID, artifactId, VERSION);
    String artifactPath = verifier.getArtifactPath(GROUP_ID, artifactId, VERSION, EXT, MULE_APPLICATION_EXAMPLE_CLASSIFIER);
    File artifactFile = new File(artifactPath);
    assertThat("Artifact already exists", !artifactFile.exists());
    verifier.executeGoal(INSTALL);

    verifier.verifyErrorFreeLog();

    assertThat("Artifact was not installed in the .m2 repository", artifactFile.exists());
  }

  @Test
  public void testInstallDomain() throws IOException, VerificationException {
    String artifactId = "empty-install-domain-project";
    projectBaseDirectory = builder.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-DattachMuleSources=true");
    verifier.deleteArtifacts(GROUP_ID, artifactId, VERSION);
    String artifactPath = verifier.getArtifactPath(GROUP_ID, artifactId, VERSION, EXT, MULE_DOMAIN_CLASSIFIER);
    File artifactFile = new File(artifactPath);
    assertThat("Artifact already exists", !artifactFile.exists());
    verifier.executeGoal(INSTALL);

    verifier.verifyErrorFreeLog();

    assertThat("Artifact was not installed in the .m2 repository", artifactFile.exists());
  }

  @Test
  public void testInstallMultiModuleApplication() throws IOException, VerificationException {

    String artifactId = "multi-module-application";
    String appSubModule = "empty-app";
    String policySubModule = "empty-policy";
    projectBaseDirectory = builder.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-DattachMuleSources=true");

    verifier.deleteArtifacts(GROUP_ID, appSubModule, VERSION);
    String artifactPath = verifier.getArtifactPath(GROUP_ID, appSubModule, VERSION, EXT, MULE_APPLICATION_CLASSIFIER);
    File artifactAppFile = new File(artifactPath);
    assertThat("Artifact already exists", !artifactAppFile.exists());

    verifier.deleteArtifacts(GROUP_ID, policySubModule, VERSION);
    artifactPath = verifier.getArtifactPath(GROUP_ID, policySubModule, VERSION, EXT, MULE_POLICY_CLASSIFIER);
    File artifactPolicyFile = new File(artifactPath);
    assertThat("Artifact already exists", !artifactPolicyFile.exists());

    verifier.executeGoal(INSTALL);

    verifier.verifyErrorFreeLog();

    assertThat("Artifact was not installed in the .m2 repository", artifactAppFile.exists());
    assertThat("Artifact was not installed in the .m2 repository", artifactPolicyFile.exists());
  }

  @Test
  public void testInstallApplicationLightPackage() throws IOException, VerificationException {
    verifier.addCliOption("-DattachMuleSources=true");
    verifier.addCliOption("-DlightweightPackage=true");
    verifier.deleteArtifacts(GROUP_ID, ARTIFACT_ID, VERSION);
    String artifactPath =
        verifier.getArtifactPath(GROUP_ID, ARTIFACT_ID, VERSION, EXT, MULE_APPLICATION_CLASSIFIER_LIGHT_PACKAGE);
    File artifactFile = new File(artifactPath);
    assertThat("Artifact already exists", !artifactFile.exists());

    verifier.executeGoal(INSTALL);

    verifier.verifyErrorFreeLog();
    assertThat("Artifact was not installed in the .m2 repository", artifactFile.exists());
  }

  @Test
  public void testInstallTemplateLightPackage() throws IOException, VerificationException {
    String artifactId = "empty-install-app-template-project";
    projectBaseDirectory = builder.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-DattachMuleSources=true");
    verifier.addCliOption("-DlightweightPackage=true");
    verifier.deleteArtifacts(GROUP_ID, artifactId, VERSION);
    String artifactPath =
        verifier.getArtifactPath(GROUP_ID, artifactId, VERSION, EXT, MULE_APPLICATION_TEMPLATE_CLASSIFIER + LIGHT_PACKAGE_EXT);
    File artifactFile = new File(artifactPath);
    assertThat("Artifact already exists", !artifactFile.exists());

    verifier.executeGoal(INSTALL);

    verifier.verifyErrorFreeLog();
    assertThat("Artifact was not installed in the .m2 repository", artifactFile.exists());
  }

  @Test
  public void testInstallExampleLightPackage() throws IOException, VerificationException {
    String artifactId = "empty-install-app-example-project";
    projectBaseDirectory = builder.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-DattachMuleSources=true");
    verifier.addCliOption("-DlightweightPackage=true");
    verifier.deleteArtifacts(GROUP_ID, artifactId, VERSION);
    String artifactPath =
        verifier.getArtifactPath(GROUP_ID, artifactId, VERSION, EXT, MULE_APPLICATION_EXAMPLE_CLASSIFIER + LIGHT_PACKAGE_EXT);
    File artifactFile = new File(artifactPath);
    assertThat("Artifact already exists", !artifactFile.exists());

    verifier.executeGoal(INSTALL);

    verifier.verifyErrorFreeLog();
    assertThat("Artifact was not installed in the .m2 repository", artifactFile.exists());
  }

  @Test
  public void testInstallDomainLightPackage() throws IOException, VerificationException {
    String artifactId = "empty-install-domain-project";
    projectBaseDirectory = builder.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-DattachMuleSources=true");
    verifier.addCliOption("-DlightweightPackage=true");
    verifier.deleteArtifacts(GROUP_ID, artifactId, VERSION);
    String artifactPath =
        verifier.getArtifactPath(GROUP_ID, artifactId, VERSION, EXT, MULE_DOMAIN_CLASSIFIER + LIGHT_PACKAGE_EXT);
    File artifactFile = new File(artifactPath);
    assertThat("Artifact already exists", !artifactFile.exists());

    verifier.executeGoal(INSTALL);

    verifier.verifyErrorFreeLog();
    assertThat("Artifact was not installed in the .m2 repository", artifactFile.exists());
  }
}

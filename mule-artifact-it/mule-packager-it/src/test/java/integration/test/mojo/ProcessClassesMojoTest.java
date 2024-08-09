/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mule.tools.api.packager.sources.DefaultValuesMuleArtifactJsonGenerator.DEFAULT_PACKAGE_EXPORT;
import static org.mule.tools.api.packager.sources.DefaultValuesMuleArtifactJsonGenerator.EXPORTED_PACKAGES;
import static org.mule.tools.api.packager.sources.DefaultValuesMuleArtifactJsonGenerator.EXPORTED_RESOURCES;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.FolderNames.TARGET;
import static org.mule.tools.api.packager.structure.PackagerFiles.ARTIFACT_AST;
import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_ARTIFACT_JSON;

import static java.nio.file.Files.readAllBytes;

import static integration.FileTreeMatcher.hasSameTreeStructure;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import integration.ProjectFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.persistence.MuleApplicationModelJsonSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.apache.maven.it.VerificationException;

@SuppressWarnings("unchecked")
public class ProcessClassesMojoTest extends MojoTest implements SettingsConfigurator {

  private static final String GOAL = "process-classes";

  public ProcessClassesMojoTest() {
    this.goal = GOAL;
  }

  @BeforeEach
  public void before() throws IOException {
    clearResources();
  }

  @Test
  public void testProcessClasses() throws IOException, VerificationException {
    verifier.executeGoal(GOAL);
    File expectedStructure = getExpectedStructure();
    assertThat("The directory structure is different from the expected", targetFolder,
               hasSameTreeStructure(expectedStructure, excludesCompile));
    verifier.verifyErrorFreeLog();

    final String json =
        new String(Files.readAllBytes(targetFolder.toPath().resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
            .resolve(MULE_ARTIFACT_JSON)));
    MuleApplicationModel muleApplicationModel = new MuleApplicationModelJsonSerializer().deserialize(json);
    assertThat((List<String>) muleApplicationModel.getClassLoaderModelLoaderDescriptor().getAttributes().get(EXPORTED_PACKAGES),
               containsInAnyOrder(DEFAULT_PACKAGE_EXPORT, "org.mule.apackagehere"));

    assertThat((List<String>) muleApplicationModel.getClassLoaderModelLoaderDescriptor().getAttributes().get(EXPORTED_RESOURCES),
               containsInAnyOrder(".placeholder", "some/path/file.txt"));
  }

  @Test
  public void testFailOnEmptyPolicyProject() throws Exception {
    projectBaseDirectory = ProjectFactory.createProjectBaseDir(EMPTY_POLICY_NAME, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    assertThatThrownBy(() -> verifier.executeGoal(GOAL)).isExactlyInstanceOf(VerificationException.class);
  }

  @Test
  public void testFailOnEmptyDomainProject() throws Exception {
    projectBaseDirectory = ProjectFactory.createProjectBaseDir(EMPTY_DOMAIN_NAME, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    assertThatThrownBy(() -> verifier.executeGoal(GOAL)).isExactlyInstanceOf(VerificationException.class);
  }

  @Test
  public void testFailOnEmptyProject() throws Exception {
    projectBaseDirectory = ProjectFactory.createProjectBaseDir(EMPTY_PROJECT_NAME, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    assertThatThrownBy(() -> verifier.executeGoal(GOAL)).isExactlyInstanceOf(VerificationException.class);
  }

  @Test
  public void testDoNotCheckSemverProject() throws Exception {
    projectBaseDirectory = ProjectFactory.createProjectBaseDir(SEMVER_CHECK, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);

    verifier.executeGoal(GOAL);
  }

  @Test
  // W-12021994
  public void noAstGenerationOnDynamicStructure() throws Exception {
    projectBaseDirectory =
        ProjectFactory.createProjectBaseDir("mule-application-structure-dependant-on-properties", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);

    verifier.executeGoal(GOAL);

    verifier
        .verifyTextInLog("Could not resolve imported resource '${env.dependant}': Couldn't find configuration property value for key ${env.dependant}");
    verifier
        .verifyTextInLog("The application has a dynamic structure based on properties available only at design time, so an artifact AST for it cannot be generated at this time. See previous WARN messages for where that dynamic structure is being detected.");

    File artifactAstTargetFile =
        projectBaseDirectory.toPath().resolve("target").resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
            .resolve(ARTIFACT_AST).toFile();
    assertThat(artifactAstTargetFile.exists(), is(false));
  }

  @Test
  // W-11831692, W-11802232
  public void astGenerationWithPropertiesOnValidableParameters() throws Exception {
    projectBaseDirectory = ProjectFactory.createProjectBaseDir("mule-application-with-unresolved-properties", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);

    verifier.executeGoal(GOAL);

    verifier
        .verifyTextInLog("'http:listener' has 'config-ref' '${config.property}' which is resolved with a property and may cause the artifact to have different behavior on different environments.");
    verifier
        .verifyTextInLog("'raise-error' has 'type' '${errorType.property}' which is resolved with a property and may cause the artifact to have different behavior on different environments.");

    File artifactAstTargetFile =
        projectBaseDirectory.toPath().resolve("target").resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
            .resolve(ARTIFACT_AST).toFile();
    assertThat(artifactAstTargetFile.exists(), is(true));

    final String serializedAst = new String(readAllBytes(artifactAstTargetFile.toPath()));
    assertThat(serializedAst, containsString("config.property"));
    assertThat(serializedAst, containsString("errorType.property"));
  }

  @Test
  public void testAstValidationWithImportTag() throws Exception {
    projectBaseDirectory = ProjectFactory.createProjectBaseDir("mule-application-with-import-file", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);

    verifier.executeGoal(GOAL);

    File artifactAstTargetFile =
        projectBaseDirectory.toPath().resolve(TARGET.value()).resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
            .resolve(ARTIFACT_AST).toFile();
    assertThat(artifactAstTargetFile.exists(), is(true));

    final String serializedAst = new String(readAllBytes(artifactAstTargetFile.toPath()));

    assertThat(serializedAst, containsString("\"resourceLocation\":\"configurations/local-config.xml\","));
    assertThat(serializedAst, containsString("\"resourceLocation\":\"connectors/global-connectors.xml\","));
  }

  @Test
  public void testAstValidationWithImportTagInvalidPath() throws Exception {
    projectBaseDirectory = ProjectFactory.createProjectBaseDir("mule-application-with-import-file-invalid-path", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);

    assertThatThrownBy(() -> verifier.executeGoal(GOAL)).isExactlyInstanceOf(VerificationException.class)
        .hasMessageContaining("Caused by: org.mule.tooling.api.ConfigurationException: Could not find imported resource 'configurations/local-config.xml'");
  }

  @Test
  public void testAstValidationWithImportTagInvalidPathSkipAST() throws Exception {
    projectBaseDirectory = ProjectFactory.createProjectBaseDir("mule-application-with-import-file-invalid-path", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-DskipAST");
    verifier.executeGoal(GOAL);

    File artifactAstTargetFile =
        projectBaseDirectory.toPath().resolve(TARGET.value()).resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
            .resolve(ARTIFACT_AST).toFile();

    assertThat(artifactAstTargetFile.exists(), is(false));
  }

  @Test
  public void testAstValidationWithImportTagInvalidPathSkipASTValidation() throws Exception {
    projectBaseDirectory = ProjectFactory.createProjectBaseDir("mule-application-with-import-file-invalid-path", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-DskipASTValidation");
    verifier.executeGoal(GOAL);

    File artifactAstTargetFile =
        projectBaseDirectory.toPath().resolve(TARGET.value()).resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
            .resolve(ARTIFACT_AST).toFile();

    assertThat(artifactAstTargetFile.exists(), is(true));
  }

  @Test
  public void astGenerationApplicationPluginWithUnresolvedPropertiesShouldNotFail() throws Exception {
    projectBaseDirectory =
        ProjectFactory.createProjectBaseDir("mule-application-plugin-with-unresolved-properties", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.executeGoal(GOAL);
    verifier.verifyErrorFreeLog();
  }
}

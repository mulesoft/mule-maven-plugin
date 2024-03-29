/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.verifier.policy;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.file.Paths.get;
import static org.mockito.Mockito.mock;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_POLICY;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.DefaultProjectInformation;
import org.mule.tools.api.packager.Pom;
import org.mule.tools.api.util.Project;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Path;

public class PolicyMuleArtifactJsonVerifierTest {

  private static final String GROUP_ID = "com.mule.anypoint.api.manager";
  private static final String ARTIFACT_ID = "custom.policy.test";
  private static final String VERSION = "1.0.0-SNAPSHOT";

  @Test
  public void goodYamlSuccessfulValidation() throws ValidationException {
    validateMuleArtifactJson("mule-artifact.json");
  }

  @Test
  public void onlyMinMuleVersionIsSuccessful() throws ValidationException {
    validateMuleArtifactJson("only-min-mule-version.json");
  }

  @Test
  public void wrongGroupIdFailsValidation() {
    assertThatThrownBy(() -> validateMuleArtifactJson(GROUP_ID + 2, ARTIFACT_ID, VERSION, "mule-artifact.json"))
        .isExactlyInstanceOf(ValidationException.class)
        .hasMessageContaining(format("Error in file 'mule-artifact.json'. The groupId does not match the one defined in the pom.xml. Expected '%s'.",
                                     GROUP_ID + 2));
  }

  @Test
  public void wrongArtifactIdFailsValidation() {
    assertThatThrownBy(() -> validateMuleArtifactJson(GROUP_ID, ARTIFACT_ID + 2, VERSION, "mule-artifact.json"))
        .isExactlyInstanceOf(ValidationException.class)
        .hasMessageContaining(format("Error in file 'mule-artifact.json'. The artifactId does not match the one defined in the pom.xml. Expected '%s'.",
                                     ARTIFACT_ID + 2));
  }

  @Test
  public void wrongVersionFailsValidation() {
    assertThatThrownBy(() -> validateMuleArtifactJson(GROUP_ID, ARTIFACT_ID, VERSION + 2, "mule-artifact.json"))
        .isExactlyInstanceOf(ValidationException.class)
        .hasMessageContaining(format("Error in file 'mule-artifact.json'. The version does not match the one defined in the pom.xml. Expected '%s'.",
                                     VERSION + 2));
  }

  @Test
  public void missingBundleDescriptorLoader() throws ValidationException {
    validateMuleArtifactJson("missing-bundle-descriptor-loader.json");
  }

  @Test
  public void missingAttributes() throws ValidationException {
    validateMuleArtifactJson("missing-attributes.json");
  }

  @Test
  public void missingClassifierFailsValidation() throws ValidationException {
    validateMuleArtifactJson("missing-classifier.json");
  }

  @Test
  public void wrongClassifierFailsValidation() {
    assertThatThrownBy(() -> validateMuleArtifactJson("wrong-classifier.json"))
        .isExactlyInstanceOf(ValidationException.class)
        .hasMessageContaining("Error in file 'wrong-classifier.json'. The field 'classifier' had an unexpected value. Expected 'mule-policy'.");
  }

  @Test
  public void missingTypeSuccessfulValidation() throws ValidationException {
    validateMuleArtifactJson("missing-type.json");
  }

  @Test
  public void wrongTypeFailsValidation() {
    assertThatThrownBy(() -> validateMuleArtifactJson("wrong-type.json"))
        .isExactlyInstanceOf(ValidationException.class)
        .hasMessageContaining("Error in file 'wrong-type.json'. The field 'type' had an unexpected value. Expected 'jar'.");
  }

  @Test
  public void configsFailsValidation() {
    assertThatThrownBy(() -> validateMuleArtifactJson("configs.json"))
        .isExactlyInstanceOf(ValidationException.class)
        .hasMessageContaining("Error in file 'configs.json'. The field configs must not be defined or be empty.");
  }

  @Test
  public void exportedPackagesFailsValidation() {
    assertThatThrownBy(() -> validateMuleArtifactJson("exported-packages.json"))
        .isExactlyInstanceOf(ValidationException.class)
        .hasMessageContaining("Error in file 'exported-packages.json'. The field exportedPackages must not be defined or be empty.");
  }

  @Test
  public void exportedResourcesFailsValidation() {
    assertThatThrownBy(() -> validateMuleArtifactJson("exported-resources.json"))
        .isExactlyInstanceOf(ValidationException.class)
        .hasMessageContaining("Error in file 'exported-resources.json'. The field exportedResources must not be defined or be empty.");
  }

  private void validateMuleArtifactJson(String fileName) throws ValidationException {
    validateMuleArtifactJson(GROUP_ID, ARTIFACT_ID, VERSION, fileName);
  }

  private void validateMuleArtifactJson(String groupId, String artifactId, String version, String fileName)
      throws ValidationException {
    new PolicyMuleArtifactJsonVerifier(
                                       getProjectInformation(groupId, artifactId, version, getTestResourceFolder()),
                                       new File(getTestResourceFolder().toAbsolutePath().toString(),
                                                fileName))
                                                    .validate();
  }

  private Path getTestResourceFolder() {
    return get(join(File.separator, "target", "test-classes", "policy-validation", "json-validation-examples"));
  }

  private DefaultProjectInformation getProjectInformation(String groupId, String artifactId, String version, Path path) {
    return new DefaultProjectInformation.Builder()
        .withGroupId(groupId)
        .withArtifactId(artifactId)
        .withVersion(version)
        .withPackaging(MULE_POLICY.toString())
        .withProjectBaseFolder(path)
        .withBuildDirectory(path)
        .setTestProject(false)
        .withResolvedPom(mock(Pom.class))
        .withDependencyProject(mock(Project.class))
        .build();
  }

}

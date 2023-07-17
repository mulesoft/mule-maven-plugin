/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.verifier.policy;

import static java.lang.String.join;
import static java.nio.file.Paths.get;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_POLICY;

import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.DefaultProjectInformation;
import org.mule.tools.api.packager.Pom;
import org.mule.tools.api.util.Project;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Path;

public class MulePolicyVerifierTest {

  private static final String RESOURCES_FOLDER = "policy-validation";
  private static final String HAPPY_PATH = concatPath(RESOURCES_FOLDER, "happy-path");
  private static final String MISSING_MULE_ARTIFACT = concatPath(RESOURCES_FOLDER, "missing-mule-artifact");
  private static final String MISSING_TEMPLATE_XML = concatPath(RESOURCES_FOLDER, "missing-template-xml");
  private static final String MISSING_YAML = concatPath(RESOURCES_FOLDER, "missing-yaml");
  private static final String MISSING_YAML2 = concatPath(RESOURCES_FOLDER, "missing-yaml2");
  private static final String INVALID_YAML = concatPath(RESOURCES_FOLDER, "invalid-yaml");

  private static final String GROUP_ID = "com.mule.anypoint.api.manager";
  private static final String ARTIFACT_ID = "custom.policy.test";
  private static final String ARTIFACT_ID_2 = "custom.policy.test2";
  private static final String VERSION = "1.0.0-SNAPSHOT";

  @Test
  public void isPolicyProjectStructureIsValid() throws ValidationException {
    getVerifier(getTestResourceFolder(HAPPY_PATH)).verify();
  }

  @Test
  public void isPolicyProjectStructureIsInvalidWithoutMuleArtifact() {
    assertThatThrownBy(() -> getVerifier(getTestResourceFolder(MISSING_MULE_ARTIFACT)).verify())
            .isExactlyInstanceOf(ValidationException.class)
            .hasMessageContaining("The file mule-artifact.json should be present.");
  }

  @Test
  public void isPolicyProjectStructureIsInvalidWithoutTemplateXML() {
    assertThatThrownBy(() -> getVerifier(getTestResourceFolder(MISSING_TEMPLATE_XML)).verify())
            .isExactlyInstanceOf(ValidationException.class)
            .hasMessageContaining("The file " + concatPath("src", "main", "mule", "template.xml") + " should be present.");

  }

  @Test
  public void isPolicyProjectStructureIsInvalidWithMissingYaml() {
    assertThatThrownBy(() -> getVerifier(getTestResourceFolder(MISSING_YAML)).verify())
            .isExactlyInstanceOf(ValidationException.class)
            .hasMessageContaining("The file " + ARTIFACT_ID + ".yaml should be present.");
  }

  @Test
  public void isPolicyProjectStructureIsInvalidWithMissingYaml2() {
    assertThatThrownBy(() -> getVerifier(ARTIFACT_ID_2, getTestResourceFolder(MISSING_YAML2)).verify())
            .isExactlyInstanceOf(ValidationException.class)
            .hasMessageContaining("The file " + ARTIFACT_ID_2 + ".yaml should be present.");
  }

  @Test
  public void isPolicyProjectStructureIsInvalidWithInvalidYaml() {
    assertThatThrownBy(() -> getVerifier(getTestResourceFolder(INVALID_YAML)).verify())
            .isExactlyInstanceOf(ValidationException.class)
            .hasMessageContaining("Error validating '" + ARTIFACT_ID + ".yaml'. Missing required creator property 'id'");
  }

  private Path getTestResourceFolder(String folderName) {
    return get(concatPath("target", "test-classes", folderName));
  }

  private static String concatPath(String... parts) {
    return join(File.separator, parts);
  }

  private MulePolicyVerifier getVerifier(Path basePath) {
    return getVerifier(ARTIFACT_ID, basePath);
  }

  private MulePolicyVerifier getVerifier(String artifactId, Path basePath) {
    DefaultProjectInformation.Builder builder = new DefaultProjectInformation.Builder()
        .withGroupId(GROUP_ID)
        .withArtifactId(artifactId)
        .withVersion(VERSION)
        .withPackaging(MULE_POLICY.toString())
        .withProjectBaseFolder(basePath)
        .withBuildDirectory(get(concatPath(basePath.toAbsolutePath().toString(), "target")))
        .setTestProject(false)
        .withResolvedPom(mock(Pom.class))
        .withDependencyProject(mock(Project.class));

    return new MulePolicyVerifier(builder.build());

  }
}

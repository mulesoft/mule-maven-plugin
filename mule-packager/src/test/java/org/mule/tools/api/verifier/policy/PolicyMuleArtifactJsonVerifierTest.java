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

import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.file.Paths.get;
import static org.mockito.Mockito.mock;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_POLICY;
import static org.mule.tools.api.verifier.policy.PolicyMuleArtifactJsonVerifier.validate;

import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.DefaultProjectInformation;
import org.mule.tools.api.packager.Pom;
import org.mule.tools.api.util.Project;

import java.io.File;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PolicyMuleArtifactJsonVerifierTest {

  private static final String GROUP_ID = "com.mule.anypoint.api.manager";
  private static final String ARTIFACT_ID = "custom.policy.test";
  private static final String VERSION = "1.0.0-SNAPSHOT";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void goodYamlSuccessfulValidation() throws ValidationException {
    validateMuleArtifactJson("mule-artifact.json");
  }

  @Test
  public void wrongGroupIdFailsValidation() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage(format("Error validating attributes from 'mule-artifact.json'. The groupId does not match the one defined in the pom.xml. Expected '%s'.",
                              GROUP_ID + 2));
    validateMuleArtifactJson(GROUP_ID + 2, ARTIFACT_ID, VERSION, "mule-artifact.json");
  }

  @Test
  public void wrongArtifactIdFailsValidation() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage(format("Error validating attributes from 'mule-artifact.json'. The artifactId does not match the one defined in the pom.xml. Expected '%s'.",
                              ARTIFACT_ID + 2));
    validateMuleArtifactJson(GROUP_ID, ARTIFACT_ID + 2, VERSION, "mule-artifact.json");
  }

  @Test
  public void wrongVersionFailsValidation() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage(format("Error validating attributes from 'mule-artifact.json'. The version does not match the one defined in the pom.xml. Expected '%s'.",
                              VERSION + 2));
    validateMuleArtifactJson(GROUP_ID, ARTIFACT_ID, VERSION + 2, "mule-artifact.json");
  }

  @Test
  public void missingClassifierFailsValidation() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage("Error validating attributes from 'missing-classifier.json'. The field 'classifier' had an unexpected value. Expected 'mule-policy'.");
    validateMuleArtifactJson("missing-classifier.json");
  }

  @Test
  public void wrongClassifierFailsValidation() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage("Error validating attributes from 'wrong-classifier.json'. The field 'classifier' had an unexpected value. Expected 'mule-policy'.");
    validateMuleArtifactJson("wrong-classifier.json");
  }

  @Test
  public void missingTypeSuccessfulValidation() throws ValidationException {
    validateMuleArtifactJson("missing-type.json");
  }

  @Test
  public void wrongTypeFailsValidation() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage("Error validating attributes from 'wrong-type.json'. The field 'type' had an unexpected value. Expected 'jar'.");
    validateMuleArtifactJson("wrong-type.json");
  }

  @Test
  public void configsFailsValidation() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("Error in file 'configs.json'. The field configs must not be defined or be empty.");
    validateMuleArtifactJson("configs.json");
  }

  @Test
  public void exportedPackagesFailsValidation() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage("Error in file 'exportedPackages.json'. The field exportedPackages must not be defined or be empty.");
    validateMuleArtifactJson("exportedPackages.json");
  }

  @Test
  public void exportedResourcesFailsValidation() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage("Error in file 'exportedResources.json'. The field exportedResources must not be defined or be empty.");
    validateMuleArtifactJson("exportedResources.json");
  }

  private void validateMuleArtifactJson(String fileName) throws ValidationException {
    validateMuleArtifactJson(GROUP_ID, ARTIFACT_ID, VERSION, fileName);
  }

  public void validateMuleArtifactJson(String groupId, String artifactId, String version, String fileName)
      throws ValidationException {
    validate(
             getProjectInformation(groupId, artifactId, version, getTestResourceFolder()),
             new File(getTestResourceFolder().toAbsolutePath().toString(),
                      fileName));
  }

  private Path getTestResourceFolder() {
    return get(concatPath("target", "test-classes", "policy-validation", "json-validation-examples"));
  }

  private static String concatPath(String... parts) {
    return join(File.separator, parts);
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

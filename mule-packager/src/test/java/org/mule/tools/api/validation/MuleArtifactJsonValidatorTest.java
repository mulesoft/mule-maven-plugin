/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.validation;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.persistence.MuleApplicationModelJsonSerializer;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.model.Deployment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_ARTIFACT_JSON;
import static org.mule.tools.api.validation.MuleArtifactJsonValidator.*;

public class MuleArtifactJsonValidatorTest {

  @Rule
  public TemporaryFolder projectBaseFolder = new TemporaryFolder();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private File muleArtifactJsonFile;
  private List<String> missingFields;
  private Deployment deploymentConfigurationMock;

  @Before
  public void setUp() throws IOException {
    projectBaseFolder.create();
    muleArtifactJsonFile = projectBaseFolder.newFile(MULE_ARTIFACT_JSON);
    missingFields = new ArrayList<>();
    deploymentConfigurationMock = mock(Deployment.class);
    when(deploymentConfigurationMock.getMuleVersion()).thenReturn(Optional.of("4.0.0"));
  }

  @Test
  public void isMuleArtifactJsonPresentFailTest() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage("Invalid Mule project. Missing mule-artifact.json file, it must be present in the root of application");
    muleArtifactJsonFile.delete();
    isMuleArtifactJsonPresent(projectBaseFolder.getRoot().toPath());
  }

  @Test
  public void isMuleArtifactJsonPresentTest() throws ValidationException, IOException {
    isMuleArtifactJsonPresent(projectBaseFolder.getRoot().toPath());
  }

  @Test
  public void isMuleArtifactJsonValidEmptyFileTest() throws IOException, ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("The mule-artifact.json file is empty");
    isMuleArtifactJsonValid(projectBaseFolder.getRoot().toPath(), deploymentConfigurationMock);
  }

  @Test
  public void isMuleArtifactJsonValidInvalidJsonSyntaxTest() throws IOException, ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("JsonSyntaxException");
    FileUtils.writeStringToFile(muleArtifactJsonFile, "{}}}", (String) null);
    isMuleArtifactJsonValid(projectBaseFolder.getRoot().toPath(), deploymentConfigurationMock);
  }

  @Test
  public void isMuleArtifactJsonValidArbitraryIOExceptionTest() throws IOException, ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("cannot be read");
    muleArtifactJsonFile.setReadable(false);
    isMuleArtifactJsonValid(projectBaseFolder.getRoot().toPath(), deploymentConfigurationMock);
  }

  @Test
  public void checkNameMissingTest() throws IOException {
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{}");

    checkName(muleArtifact, missingFields);

    assertThat("Missing fields should contain the name field name", missingFields, containsInAnyOrder("name"));
  }

  @Test
  public void checkNameTest() throws IOException {
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ name:aaa }");

    checkName(muleArtifact, missingFields);

    assertThat("Missing fields should be empty", missingFields.isEmpty(), is(true));
  }

  @Test
  public void checkMinMuleVersionValueMissingTest() throws IOException, ValidationException {
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ }");

    checkMinMuleVersionValue(muleArtifact, missingFields, deploymentConfigurationMock);

    assertThat("Missing fields should contain the minMuleVersion field name", missingFields,
               containsInAnyOrder("minMuleVersion"));
  }

  @Test
  public void checkMinMuleVersionValueTest() throws IOException, ValidationException {
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ minMuleVersion:4.0.0 }");

    checkMinMuleVersionValue(muleArtifact, missingFields, deploymentConfigurationMock);

    assertThat("Missing fields should be empty", missingFields.isEmpty(), is(true));
  }

  @Test
  public void checkClassLoaderModelDescriptorMissingTest() throws IOException {
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ }");

    checkClassLoaderModelDescriptor(muleArtifact, missingFields);

    assertThat("Missing fields should contain the classLoaderModelLoaderDescriptor and classLoaderModelLoaderDescriptor.id fields names",
               missingFields,
               containsInAnyOrder("classLoaderModelLoaderDescriptor", "classLoaderModelLoaderDescriptor.id"));
  }

  @Test
  public void checkClassLoaderModelDescriptorMissingIdTest() throws IOException {
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ classLoaderModelLoaderDescriptor: {} }");

    checkClassLoaderModelDescriptor(muleArtifact, missingFields);

    assertThat("There should be just one missing field", missingFields.size(), equalTo(1));
    assertThat("Missing fields should contain the classLoaderModelLoaderDescriptor.id fields names", missingFields,
               containsInAnyOrder("classLoaderModelLoaderDescriptor.id"));
  }

  @Test
  public void checkClassLoaderModelDescriptorTest() throws IOException {
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ classLoaderModelLoaderDescriptor: { id:mule } }");

    checkClassLoaderModelDescriptor(muleArtifact, missingFields);

    assertThat("Missing fields should be empty", missingFields.isEmpty(), is(true));
  }

  @Test
  public void validateMuleArtifactMandatoryFieldsMissingTest() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage("The following mandatory fields in the mule-artifact.json are missing or invalid: [name, minMuleVersion, classLoaderModelLoaderDescriptor, classLoaderModelLoaderDescriptor.id, requiredProduct]");

    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer()
            .deserialize("{ }");

    validateMuleArtifactMandatoryFields(muleArtifact, deploymentConfigurationMock);
  }

  @Test
  public void validateMuleArtifactMandatoryFieldsMissingNameTest() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("The following mandatory fields in the mule-artifact.json are missing or invalid: [name]");

    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer()
            .deserialize("{ minMuleVersion:4.0.0, classLoaderModelLoaderDescriptor: { id:mule }, requiredProduct: MULE }");

    validateMuleArtifactMandatoryFields(muleArtifact, deploymentConfigurationMock);
  }



  @Test
  public void validateMuleArtifactMandatoryFieldsMissingMinMuleVersionTest() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage("The following mandatory fields in the mule-artifact.json are missing or invalid: [minMuleVersion]");

    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer()
            .deserialize("{ name:lala, classLoaderModelLoaderDescriptor: { id:mule }, requiredProduct: MULE }");

    validateMuleArtifactMandatoryFields(muleArtifact, deploymentConfigurationMock);
  }

  @Test
  public void validateMuleArtifactMandatoryFieldsMissingclassLoaderModelLoaderDescriptorTest() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage("The following mandatory fields in the mule-artifact.json are missing or invalid: [classLoaderModelLoaderDescriptor, classLoaderModelLoaderDescriptor.id, requiredProduct]. requiredProduct valid values are: MULE, MULE_EE");

    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer()
            .deserialize("{ name:lala, minMuleVersion:4.0.0, requiredProduct: MULE_EEa }");

    validateMuleArtifactMandatoryFields(muleArtifact, deploymentConfigurationMock);
  }

  @Test
  public void validateMuleArtifactMandatoryFieldsMissingclassLoaderModelLoaderDescriptorIdTest() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage("The following mandatory fields in the mule-artifact.json are missing or invalid: [classLoaderModelLoaderDescriptor.id]");

    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer()
            .deserialize("{ name:lala, minMuleVersion:4.0.0, classLoaderModelLoaderDescriptor: {  }, requiredProduct: MULE_EE }");

    validateMuleArtifactMandatoryFields(muleArtifact, deploymentConfigurationMock);
  }

  @Test
  public void validateMuleArtifactMandatoryFieldsMissingNameAndClassLoaderModelLoaderDescriptorIdTest()
      throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage("The following mandatory fields in the mule-artifact.json are missing or invalid: [name, classLoaderModelLoaderDescriptor.id]");

    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer()
            .deserialize("{ minMuleVersion:4.0.0, classLoaderModelLoaderDescriptor: {  }, requiredProduct: MULE }");

    validateMuleArtifactMandatoryFields(muleArtifact, deploymentConfigurationMock);
  }

  @Test
  public void validateMuleArtifactMandatoryFieldsMissingRequiredProductTest()
      throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage("The following mandatory fields in the mule-artifact.json are missing or invalid: [requiredProduct]");

    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer()
            .deserialize("{ name:lala, minMuleVersion:4.0.0, classLoaderModelLoaderDescriptor: { id:mule } }");

    validateMuleArtifactMandatoryFields(muleArtifact, deploymentConfigurationMock);
  }

  @Test
  public void checkMinMuleVersionInvalidValueTest() throws IOException, ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("Version 4.0 does not comply with semantic versioning specification");
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ minMuleVersion:4.0 }");

    checkMinMuleVersionValue(muleArtifact, missingFields, deploymentConfigurationMock);
  }

  @Test
  public void checkMinMuleVersionAgainstDeploymentInvalidValueTest() throws IOException, ValidationException {
    expectedException.expect(ValidationException.class);
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ minMuleVersion:4.0.0 }");

    when(deploymentConfigurationMock.getMuleVersion()).thenReturn(Optional.of("3.8.0"));
    checkMinMuleVersionValue(muleArtifact, missingFields, deploymentConfigurationMock);
  }

  @Test
  public void checkMinMuleVersionAgainstDeploymentInvalidValue2Test() throws IOException, ValidationException {
    expectedException.expect(ValidationException.class);
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ minMuleVersion:4.1.0 }");

    when(deploymentConfigurationMock.getMuleVersion()).thenReturn(Optional.of("4.0.0"));
    checkMinMuleVersionValue(muleArtifact, missingFields, deploymentConfigurationMock);
  }

  @Test
  public void checkMinMuleVersionAgainstDeploymentInvalidValue3Test() throws IOException, ValidationException {
    expectedException.expect(ValidationException.class);
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ minMuleVersion:4.0.1 }");

    when(deploymentConfigurationMock.getMuleVersion()).thenReturn(Optional.of("4.0.0"));
    checkMinMuleVersionValue(muleArtifact, missingFields, deploymentConfigurationMock);
  }

  @Test
  public void checkMinMuleVersionAgainstDeploymentValidValueTest() throws IOException, ValidationException {
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ minMuleVersion:4.0.1 }");

    when(deploymentConfigurationMock.getMuleVersion()).thenReturn(Optional.of("4.1.0"));
    checkMinMuleVersionValue(muleArtifact, missingFields, deploymentConfigurationMock);
  }

  @Test
  public void checkMinMuleVersionAgainstDeploymentValidValue2Test() throws IOException, ValidationException {
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ minMuleVersion:4.10.10 }");

    when(deploymentConfigurationMock.getMuleVersion()).thenReturn(Optional.of("5.0.0"));
    checkMinMuleVersionValue(muleArtifact, missingFields, deploymentConfigurationMock);
  }

}

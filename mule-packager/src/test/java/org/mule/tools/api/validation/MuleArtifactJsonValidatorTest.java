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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_ARTIFACT_JSON;
import static org.mule.tools.api.validation.MuleArtifactJsonValidator.checkClassLoaderModelDescriptor;
import static org.mule.tools.api.validation.MuleArtifactJsonValidator.checkMinMuleVersionValue;
import static org.mule.tools.api.validation.MuleArtifactJsonValidator.checkName;
import static org.mule.tools.api.validation.MuleArtifactJsonValidator.isMuleArtifactJsonPresent;
import static org.mule.tools.api.validation.MuleArtifactJsonValidator.isMuleArtifactJsonValid;
import static org.mule.tools.api.validation.MuleArtifactJsonValidator.validateMuleArtifactMandatoryFields;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.persistence.MuleApplicationModelJsonSerializer;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.model.Deployment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

public class MuleArtifactJsonValidatorTest {

  @TempDir
  public Path projectBaseFolder;

  private File muleArtifactJsonFile;
  private List<String> missingFields;
  private Deployment deploymentConfigurationMock;

  @BeforeEach
  public void setUp() throws IOException {
    projectBaseFolder.toFile();
    Files.createFile(projectBaseFolder.resolve(MULE_ARTIFACT_JSON));
    muleArtifactJsonFile = projectBaseFolder.resolve(MULE_ARTIFACT_JSON).toFile();
    projectBaseFolder.resolve(muleArtifactJsonFile.toString());
    missingFields = new ArrayList<>();
    deploymentConfigurationMock = mock(Deployment.class);
    when(deploymentConfigurationMock.getMuleVersion()).thenReturn(Optional.of("4.0.0"));
  }

  @Test
  public void isMuleArtifactJsonPresentFailTest() {
    Exception exception = assertThrows(ValidationException.class, () -> {
      muleArtifactJsonFile.delete();

      isMuleArtifactJsonPresent(projectBaseFolder.toAbsolutePath());
    });
    String expectedMessage =
        "Invalid Mule project. Missing mule-artifact.json file, it must be present in the root of application";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void isMuleArtifactJsonPresentTest() throws ValidationException {
    isMuleArtifactJsonPresent(projectBaseFolder.toAbsolutePath());
  }

  @Test
  public void isMuleArtifactJsonValidEmptyFileTest() {
    Exception exception = assertThrows(ValidationException.class,
                                       () -> isMuleArtifactJsonValid(projectBaseFolder.toAbsolutePath(), Optional.empty()));
    String expectedMessage = "The mule-artifact.json file is empty";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void isMuleArtifactJsonValidInvalidJsonSyntaxTest() {
    Exception exception = assertThrows(ValidationException.class, () -> {
      FileUtils.writeStringToFile(muleArtifactJsonFile, "{}}}", (String) null);
      isMuleArtifactJsonValid(projectBaseFolder.toAbsolutePath(), Optional.empty());
    });
    String expectedMessage = "JsonSyntaxException";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void isMuleArtifactJsonValidArbitraryIOExceptionTest() {
    Exception exception = assertThrows(ValidationException.class, () -> {
      muleArtifactJsonFile.delete();
      muleArtifactJsonFile = projectBaseFolder.resolve(MULE_ARTIFACT_JSON).toFile();
      isMuleArtifactJsonValid(projectBaseFolder.toAbsolutePath(), Optional.empty());
    });
    String expectedMessage = "java.io.FileNotFoundException";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void checkNameMissingTest() {
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{}");

    checkName(muleArtifact, missingFields);

    assertThat(missingFields).describedAs("Missing fields should contain the name field name").contains("name");
  }

  @Test
  public void checkNameTest() {
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ name:aaa }");

    checkName(muleArtifact, missingFields);

    assertThat(missingFields.isEmpty()).describedAs("Missing fields should be empty").isTrue();
  }

  @Test
  public void checkMinMuleVersionValueMissingTest() throws ValidationException {
    MuleApplicationModel muleArtifact = new MuleApplicationModelJsonSerializer().deserialize("{ }");

    checkMinMuleVersionValue(muleArtifact, missingFields, Optional.empty());

    assertThat(missingFields).describedAs("Missing fields should contain the minMuleVersion field name")
        .contains("minMuleVersion");
  }

  @Test
  public void checkMinMuleVersionValueTest() throws ValidationException {
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ minMuleVersion:4.0.0 }");

    checkMinMuleVersionValue(muleArtifact, missingFields, Optional.empty());

    assertThat(missingFields.isEmpty()).describedAs("Missing fields should be empty").isTrue();
  }

  @Test
  public void checkClassLoaderModelDescriptorMissingTest() {
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ }");

    checkClassLoaderModelDescriptor(muleArtifact, missingFields);

    assertThat(missingFields)
        .describedAs("Missing fields should contain the classLoaderModelLoaderDescriptor and classLoaderModelLoaderDescriptor.id fields names")
        .contains("classLoaderModelLoaderDescriptor", "classLoaderModelLoaderDescriptor.id");
  }

  @Test
  public void checkClassLoaderModelDescriptorMissingIdTest() {
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ classLoaderModelLoaderDescriptor: {} }");

    checkClassLoaderModelDescriptor(muleArtifact, missingFields);

    assertThat(missingFields.size()).describedAs("There should be just one missing field").isEqualTo(1);
    assertThat(missingFields).describedAs("Missing fields should contain the classLoaderModelLoaderDescriptor.id fields names")
        .contains("classLoaderModelLoaderDescriptor.id");
  }

  @Test
  public void checkClassLoaderModelDescriptorTest() {
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ classLoaderModelLoaderDescriptor: { id:mule } }");

    checkClassLoaderModelDescriptor(muleArtifact, missingFields);

    assertThat(missingFields.isEmpty()).describedAs("Missing fields should be empty").isTrue();
  }

  @Test
  public void validateMuleArtifactMandatoryFieldsMissingTest() {
    Exception exception = assertThrows(ValidationException.class, () -> {
      MuleApplicationModel muleArtifact =
          new MuleApplicationModelJsonSerializer()
              .deserialize("{ }");

      validateMuleArtifactMandatoryFields(muleArtifact, Optional.empty());
    });
    String expectedMessage =
        "The following mandatory fields in the mule-artifact.json are missing or invalid: [name, minMuleVersion, requiredProduct]";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void validateMuleArtifactMandatoryFieldsMissingNameTest() {
    Exception exception = assertThrows(ValidationException.class, () -> {
      MuleApplicationModel muleArtifact =
          new MuleApplicationModelJsonSerializer()
              .deserialize("{ minMuleVersion:4.0.0, classLoaderModelLoaderDescriptor: { id:mule }, requiredProduct: MULE }");

      validateMuleArtifactMandatoryFields(muleArtifact, Optional.empty());
    });
    String expectedMessage = "The following mandatory fields in the mule-artifact.json are missing or invalid: [name]";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void validateMuleArtifactMandatoryFieldsMissingMinMuleVersionTest() {
    Exception exception = assertThrows(ValidationException.class, () -> {
      MuleApplicationModel muleArtifact =
          new MuleApplicationModelJsonSerializer()
              .deserialize("{ name:lala, classLoaderModelLoaderDescriptor: { id:mule }, requiredProduct: MULE }");

      validateMuleArtifactMandatoryFields(muleArtifact, Optional.empty());
    });
    String expectedMessage = "The following mandatory fields in the mule-artifact.json are missing or invalid: [minMuleVersion]";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void validateMuleArtifactMandatoryFieldsMissingclassLoaderModelLoaderDescriptorTest() {
    Exception exception = assertThrows(ValidationException.class, () -> {
      MuleApplicationModel muleArtifact =
          new MuleApplicationModelJsonSerializer()
              .deserialize("{ name:lala, minMuleVersion:4.0.0, requiredProduct: MULE_EEa }");

      validateMuleArtifactMandatoryFields(muleArtifact, Optional.empty());
    });
    String expectedMessage =
        "The following mandatory fields in the mule-artifact.json are missing or invalid: [requiredProduct]. requiredProduct valid values are: MULE, MULE_EE";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void validateMuleArtifactMandatoryFieldsMissingNameAndClassLoaderModelLoaderDescriptorIdTest() {
    Exception exception = assertThrows(ValidationException.class, () -> {
      MuleApplicationModel muleArtifact =
          new MuleApplicationModelJsonSerializer()
              .deserialize("{ minMuleVersion:4.0.0, classLoaderModelLoaderDescriptor: {  }, requiredProduct: MULE }");

      validateMuleArtifactMandatoryFields(muleArtifact, Optional.empty());
    });
    String expectedMessage = "The following mandatory fields in the mule-artifact.json are missing or invalid: [name]";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void validateMuleArtifactMandatoryFieldsMissingRequiredProductTest() {
    Exception exception = assertThrows(ValidationException.class, () -> {
      MuleApplicationModel muleArtifact =
          new MuleApplicationModelJsonSerializer()
              .deserialize("{ name:lala, minMuleVersion:4.0.0, classLoaderModelLoaderDescriptor: { id:mule } }");

      validateMuleArtifactMandatoryFields(muleArtifact, Optional.empty());
    });
    String expectedMessage = "The following mandatory fields in the mule-artifact.json are missing or invalid: [requiredProduct]";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void checkMinMuleVersionInvalidValueTest() {
    Exception exception = assertThrows(ValidationException.class, () -> {
      MuleApplicationModel muleArtifact =
          new MuleApplicationModelJsonSerializer().deserialize("{ minMuleVersion:4.0 }");

      checkMinMuleVersionValue(muleArtifact, missingFields, Optional.empty());
    });
    String expectedMessage = "Version 4.0 does not comply with semantic versioning specification";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void checkMinMuleVersionAgainstDeploymentInvalidValueTest() {
    assertThrows(ValidationException.class, () -> {
      MuleApplicationModel muleArtifact =
          new MuleApplicationModelJsonSerializer().deserialize("{ minMuleVersion:4.0.0 }");

      checkMinMuleVersionValue(muleArtifact, missingFields, Optional.of("3.8.0"));
    });
  }

  @Test
  public void checkMinMuleVersionAgainstDeploymentInvalidValue2Test() {
    assertThrows(ValidationException.class, () -> {
      MuleApplicationModel muleArtifact =
          new MuleApplicationModelJsonSerializer().deserialize("{ minMuleVersion:4.1.0 }");

      checkMinMuleVersionValue(muleArtifact, missingFields, Optional.of("4.0.0"));
    });
  }

  @Test
  public void checkMinMuleVersionAgainstDeploymentInvalidValue3Test() {
    assertThrows(ValidationException.class, () -> {
      MuleApplicationModel muleArtifact =
          new MuleApplicationModelJsonSerializer().deserialize("{ minMuleVersion:4.0.1 }");

      checkMinMuleVersionValue(muleArtifact, missingFields, Optional.of("4.0.0"));
    });
  }

  @Test
  public void checkMinMuleVersionAgainstDeploymentValidValueTest() throws IOException, ValidationException {
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ minMuleVersion:4.0.1 }");

    when(deploymentConfigurationMock.getMuleVersion()).thenReturn(Optional.of("4.1.0"));
    checkMinMuleVersionValue(muleArtifact, missingFields, Optional.empty());
  }

  @Test
  public void checkMinMuleVersionAgainstDeploymentValidValue2Test() throws IOException, ValidationException {
    MuleApplicationModel muleArtifact =
        new MuleApplicationModelJsonSerializer().deserialize("{ minMuleVersion:4.10.10 }");

    when(deploymentConfigurationMock.getMuleVersion()).thenReturn(Optional.of("5.0.0"));
    checkMinMuleVersionValue(muleArtifact, missingFields, Optional.empty());
  }

}

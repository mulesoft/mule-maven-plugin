/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mule.tools.artifact.archiver.api.PackagerFiles.MULE_APPLICATION_JSON;
import static org.mule.tools.artifact.archiver.api.PackagerFiles.MULE_POLICY_JSON;
import static org.mule.tools.api.packager.FolderNames.MAIN;
import static org.mule.tools.api.packager.FolderNames.MULE;
import static org.mule.tools.api.packager.FolderNames.POLICY;
import static org.mule.tools.api.packager.FolderNames.SRC;

import org.mule.tools.api.packager.exception.ValidationException;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

public class ValidatorTest {

  public static final String MULE_DOMAIN = "mule-domain";
  public static final String MULE_APPLICATION = "mule-application";
  public static final String MULE_POLICY = "mule-policy";
  @Rule
  public TemporaryFolder projectBaseFolder = new TemporaryFolder();

  private Validator validator;

  @Before
  public void before() throws IOException, MojoExecutionException {
    validator = new Validator(projectBaseFolder.getRoot().toPath());
  }

  @Test
  public void isMuleApplicationPackagingTypeValid() throws ValidationException {
    Boolean valid = validator.isPackagingTypeValid(MULE_APPLICATION);
    assertThat("Packaging type should be valid", valid, is(true));
  }

  @Test
  public void isMuleDomainPackagingTypeValid() throws ValidationException {
    Boolean valid = validator.isPackagingTypeValid(MULE_DOMAIN);
    assertThat("Packaging type should be valid", valid, is(true));
  }

  @Test
  public void isMulePolicyPackagingTypeValid() throws ValidationException {
    Boolean valid = validator.isPackagingTypeValid(MULE_POLICY);
    assertThat("Packaging type should be valid", valid, is(true));
  }

  @Test(expected = ValidationException.class)
  public void isPackagingTypeValid() throws ValidationException {
    Boolean valid = validator.isPackagingTypeValid("no-valid-packagin");
    assertThat("Packaging type should be valid", valid, is(true));
  }

  @Test(expected = ValidationException.class)
  public void projectStructureValidMuleApplicationInvalid() throws ValidationException {
    Path mainSrcFolder = projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value());
    mainSrcFolder.toFile().mkdirs();

    validator.isProjectStructureValid(MULE_APPLICATION);
  }

  @Test
  public void isProjectStructureValidMuleApplication() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(MULE.value());
    muleMainSrcFolder.toFile().mkdirs();

    Boolean valid = validator.isProjectStructureValid(MULE_APPLICATION);
    assertThat("Project structure should be valid", valid, is(true));
  }


  @Test(expected = ValidationException.class)
  public void isProjectStructureValidMulePolicyInvalid() throws ValidationException {
    Path mainSrcFolder = projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value());
    mainSrcFolder.toFile().mkdirs();

    validator.isProjectStructureValid(MULE_POLICY);
  }

  @Test
  public void isProjectStructureValidMulePolicy() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(POLICY.value());
    muleMainSrcFolder.toFile().mkdirs();

    Boolean valid = validator.isProjectStructureValid(MULE_POLICY);
    assertThat("Project structure should be valid", valid, is(true));
  }

  @Test(expected = ValidationException.class)
  public void isProjectStructureValidMuleDomainInvalid() throws ValidationException {
    Path mainSrcFolder = projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value());
    mainSrcFolder.toFile().mkdirs();

    validator.isProjectStructureValid(MULE_DOMAIN);
  }

  @Test
  public void isProjectStructureValidMuleDomain() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(MULE.value());
    muleMainSrcFolder.toFile().mkdirs();

    Boolean valid = validator.isProjectStructureValid(MULE_DOMAIN);
    assertThat("Project structure should be valid", valid, is(true));
  }

  @Test(expected = ValidationException.class)
  public void isDescriptorFilePresentMuleApplicationInvalid() throws IOException, ValidationException {
    validator.isDescriptorFilePresent(MULE_APPLICATION);
  }

  @Test
  public void isDescriptorFilePresentMuleApplication() throws IOException, ValidationException {
    Path descriptorFilePath = projectBaseFolder.getRoot().toPath().resolve(MULE_APPLICATION_JSON);
    descriptorFilePath.toFile().createNewFile();

    Boolean valid = validator.isDescriptorFilePresent(MULE_APPLICATION);
    assertThat("Project's descriptor file should be present", valid, is(true));
  }

  @Test(expected = ValidationException.class)
  public void isDescriptorFilePresentMulePolicyInvalid() throws IOException, ValidationException {
    validator.isDescriptorFilePresent(MULE_POLICY);
  }

  @Test
  public void isDescriptorFilePresentMulePolicy() throws IOException, ValidationException {
    Path descriptorFilePath = projectBaseFolder.getRoot().toPath().resolve(MULE_POLICY_JSON);
    descriptorFilePath.toFile().createNewFile();

    Boolean valid = validator.isDescriptorFilePresent(MULE_POLICY);
    assertThat("Project's descriptor file should be present", valid, is(true));
  }

  @Test
  public void isProjectValid() throws ValidationException {
    validator = Mockito.mock(Validator.class);
    String packagingType = MULE_APPLICATION;

    doReturn(true).when(validator).isPackagingTypeValid(packagingType);
    doReturn(true).when(validator).isProjectStructureValid(packagingType);
    doReturn(true).when(validator).isDescriptorFilePresent(packagingType);

    doCallRealMethod().when(validator).isProjectValid(packagingType);
    validator.isProjectValid(packagingType);

    verify(validator, times(1)).isPackagingTypeValid(packagingType);
    verify(validator, times(1)).isProjectStructureValid(packagingType);
    verify(validator, times(1)).isDescriptorFilePresent(packagingType);
  }
}

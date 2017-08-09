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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.mule.tools.api.packager.structure.FolderNames.MAIN;
import static org.mule.tools.api.packager.structure.FolderNames.MULE;
import static org.mule.tools.api.packager.structure.FolderNames.POLICY;
import static org.mule.tools.api.packager.structure.FolderNames.SRC;

import org.junit.rules.ExpectedException;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.SharedLibraryDependency;
import org.mule.tools.api.exception.ValidationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

public class ValidatorTest {

  private static final String VALIDATE_SHARED_LIBRARIES_MESSAGE =
      "The mule application does not contain the following shared libraries: ";
  public static final String MULE_POLICY = "mule-policy";
  protected static final String MULE_ARTIFACT_JSON = "mule-artifact.json";
  protected static final String GROUP_ID = "group-id";
  protected static final String ARTIFACT_ID = "artifact-id";
  protected static final String MULE_APPLICATION = "mule-application";
  protected static final String MULE_DOMAIN = "mule-domain";
  protected final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
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

  @Test(expected = ValidationException.class)
  public void isProjectStructureInvalidValidMuleApplication() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(POLICY.value());
    muleMainSrcFolder.toFile().mkdirs();

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

  @Test(expected = ValidationException.class)
  public void isProjectStructureInvalidValidMulePolicy() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(MULE.value());
    muleMainSrcFolder.toFile().mkdirs();

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

  @Test(expected = ValidationException.class)
  public void isProjectStructureInValidMuleDomain() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(POLICY.value());
    muleMainSrcFolder.toFile().mkdirs();

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
    validator.isDescriptorFilePresent();
  }

  @Test
  public void isDescriptorFilePresentMuleApplication() throws IOException, ValidationException {
    Path descriptorFilePath = projectBaseFolder.getRoot().toPath().resolve(MULE_ARTIFACT_JSON);
    descriptorFilePath.toFile().createNewFile();

    Boolean valid = validator.isDescriptorFilePresent();
    assertThat("Project's descriptor file should be present", valid, is(true));
  }

  @Test
  public void isProjectValid() throws ValidationException {
    validator = Mockito.mock(Validator.class);
    String packagingType = MULE_APPLICATION;

    doReturn(true).when(validator).isPackagingTypeValid(packagingType);
    doReturn(true).when(validator).isProjectStructureValid(packagingType);
    doReturn(true).when(validator).isDescriptorFilePresent();

    doCallRealMethod().when(validator).isProjectValid(packagingType);
    validator.isProjectValid(packagingType);

    verify(validator, times(1)).isPackagingTypeValid(packagingType);
    verify(validator, times(1)).isProjectStructureValid(packagingType);
    verify(validator, times(1)).isDescriptorFilePresent();
  }

  @Test
  public void validateNoSharedLibrariesInDependenciesTest() throws MojoExecutionException, ValidationException {
    expectedException.expect(ValidationException.class);

    List<SharedLibraryDependency> sharedLibraries = new ArrayList<>();
    sharedLibraries.add(buildSharedLibraryDependency(GROUP_ID, ARTIFACT_ID));

    validator.validateSharedLibraries(sharedLibraries, new ArrayList<>());

    assertThat("Validate goal message was not the expected", VALIDATE_SHARED_LIBRARIES_MESSAGE + sharedLibraries.toString(),
               equalTo(outContent.toString()));
  }

  @Test
  public void validateSharedLibrariesInDependenciesTest() throws MojoExecutionException, ValidationException {

    SharedLibraryDependency sharedLibraryDependencyB = new SharedLibraryDependency();
    sharedLibraryDependencyB.setArtifactId(ARTIFACT_ID + "-b");
    sharedLibraryDependencyB.setGroupId(GROUP_ID + "-b");

    List<SharedLibraryDependency> sharedLibraries = new ArrayList<>();
    sharedLibraries.add(buildSharedLibraryDependency(GROUP_ID + "-a", ARTIFACT_ID + "-a"));
    sharedLibraries.add(buildSharedLibraryDependency(GROUP_ID + "-b", ARTIFACT_ID + "-b"));

    List<ArtifactCoordinates> projectDependencies = new ArrayList<>();
    projectDependencies.add(buildDependency(GROUP_ID + "-a", ARTIFACT_ID + "-a"));
    projectDependencies.add(buildDependency(GROUP_ID + "-b", ARTIFACT_ID + "-b"));
    projectDependencies.add(buildDependency(GROUP_ID + "-c", ARTIFACT_ID + "-c"));
    validator.validateSharedLibraries(sharedLibraries, projectDependencies);
  }

  private SharedLibraryDependency buildSharedLibraryDependency(String groupId, String artifactId) {
    SharedLibraryDependency sharedLibraryDependency = new SharedLibraryDependency();
    sharedLibraryDependency.setArtifactId(artifactId);
    sharedLibraryDependency.setGroupId(groupId);
    return sharedLibraryDependency;
  }

  private ArtifactCoordinates buildDependency(String groupId, String artifactId) {
    return new ArtifactCoordinates(groupId, artifactId, "1.0.0");
  }
}

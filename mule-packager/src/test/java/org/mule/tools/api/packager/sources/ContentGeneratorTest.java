/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.sources;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.mule.tools.api.packager.structure.FolderNames.MAPPINGS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.mule.tools.api.packager.PackagerTestUtils;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.packaging.PackagingType;

public class ContentGeneratorTest {

  protected static final String GROUP_ID = "org.mule.munit";
  protected static final String ARTIFACT_ID = "fake-id";
  protected static final String VERSION = "1.0.0-SNAPSHOT";

  private static final String POM_FILE_NAME = "pom.xml";
  private static final String FAKE_FILE_NAME = "fakeFile.xml";

  @Rule
  public TemporaryFolder projectBaseFolder = new TemporaryFolder();

  private PackagingType packagingType = PackagingType.MULE;

  private MuleContentGenerator contentGenerator;
  private File projectTargetFolder;

  @Before
  public void setUp() throws IOException {
    projectTargetFolder = projectBaseFolder.newFolder("target");
    ProjectInformation info = new ProjectInformation.Builder()
        .withGroupId(GROUP_ID)
        .withArtifactId(ARTIFACT_ID)
        .withVersion(VERSION)
        .withPackaging(packagingType.toString())
        .withProjectBaseFolder(projectBaseFolder.getRoot().toPath())
        .withBuildDirectory(projectTargetFolder.toPath())
        .build();
    contentGenerator = new MuleContentGenerator(info);
  }

  @Test(expected = IllegalArgumentException.class)
  public void failCreationProjectBaseFolderNonExistent() {
    ProjectInformation info = new ProjectInformation.Builder()
        .withGroupId(GROUP_ID)
        .withArtifactId(ARTIFACT_ID)
        .withVersion(VERSION)
        .withPackaging(packagingType.toString())
        .withProjectBaseFolder(Paths.get("/fake/project/base/folder"))
        .withBuildDirectory(projectTargetFolder.toPath()).build();
    new MuleContentGenerator(info);
  }

  @Test(expected = IllegalArgumentException.class)
  public void failCreationProjectTargetFolderNonExistent() {
    ProjectInformation info = new ProjectInformation.Builder()
        .withGroupId(GROUP_ID)
        .withArtifactId(ARTIFACT_ID)
        .withVersion(VERSION)
        .withPackaging(packagingType.toString())
        .withProjectBaseFolder(projectBaseFolder.getRoot().toPath())
        .withBuildDirectory(Paths.get("/fake/project/base/folder")).build();
    new MuleContentGenerator(info);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createSrcFolderContentNonExistingSourceFolder() throws IOException {
    String destinationFolderName = packagingType.getSourceFolderName();

    Path destinationFolderPath = projectTargetFolder.toPath().resolve(destinationFolderName);
    PackagerTestUtils.createEmptyFolder(destinationFolderPath);

    contentGenerator.createMuleSrcFolderContent();
  }

  @Test
  public void createTestFolderContentNonExistingSourceFolder() throws IOException {
    String destinationFolderName = packagingType.getTestFolderName();

    Path destinationFolderPath = projectTargetFolder.toPath().resolve(PackagerTestUtils.TEST_MULE)
        .resolve(destinationFolderName);
    PackagerTestUtils.createEmptyFolder(destinationFolderPath);

    contentGenerator.createTestFolderContent();
    PackagerTestUtils.assertFileDoesNotExists(destinationFolderPath.resolve(FAKE_FILE_NAME));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createTestFolderContentNonExistingDestinationFolder() throws IOException {
    String sourceFolderName = packagingType.getTestFolderName();

    Path sourceFolderPath = projectBaseFolder.getRoot().toPath().resolve(PackagerTestUtils.SRC).resolve(
                                                                                                        PackagerTestUtils.TEST)
        .resolve(sourceFolderName);
    PackagerTestUtils.createFolder(sourceFolderPath, FAKE_FILE_NAME, true);

    contentGenerator.createTestFolderContent();
  }

  @Test
  public void createTestFolderContent() throws IOException {
    String sourceFolderName = packagingType.getTestFolderName();
    String destinationFolderName = packagingType.getTestFolderName();

    Path sourceFolderPath = projectBaseFolder.getRoot().toPath().resolve(PackagerTestUtils.SRC).resolve(
                                                                                                        PackagerTestUtils.TEST)
        .resolve(sourceFolderName);
    PackagerTestUtils.createFolder(sourceFolderPath, FAKE_FILE_NAME, true);

    Path destinationFolderPath = projectTargetFolder.toPath().resolve(
                                                                      PackagerTestUtils.TEST_MULE)
        .resolve(destinationFolderName);
    PackagerTestUtils.createEmptyFolder(destinationFolderPath);

    contentGenerator.createTestFolderContent();

    PackagerTestUtils.assertFileExists(destinationFolderPath.resolve(FAKE_FILE_NAME));
  }

  @Test
  public void createMappingsContentNonExistingSourceFolder() throws IOException {
    Path destinationFolderPath = projectTargetFolder.toPath().resolve(MAPPINGS.value());
    PackagerTestUtils.createEmptyFolder(destinationFolderPath);

    contentGenerator.createMappingsContent();
    PackagerTestUtils.assertFolderIsEmpty(destinationFolderPath);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createMappingsFolderContentNonExistingDestinationFolder() throws IOException {
    Path sourceFolderPath = projectBaseFolder.getRoot().toPath().resolve(MAPPINGS.value());
    PackagerTestUtils.createFolder(sourceFolderPath, FAKE_FILE_NAME, true);

    contentGenerator.createMappingsContent();
  }

  @Test
  public void createMappingsFolderContent() throws IOException {
    Path sourceFolderPath = projectBaseFolder.getRoot().toPath().resolve(MAPPINGS.value());
    PackagerTestUtils.createFolder(sourceFolderPath, FAKE_FILE_NAME, true);

    Path destinationFolderPath = projectTargetFolder.toPath().resolve(MAPPINGS.value());
    PackagerTestUtils.createEmptyFolder(destinationFolderPath);

    contentGenerator.createMappingsContent();

    PackagerTestUtils.assertFileExists(destinationFolderPath.resolve(FAKE_FILE_NAME));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createDescriptorsNoPomDestinationFolder() throws IOException {

    Path sourceFolderPath = projectBaseFolder.getRoot().toPath();
    PackagerTestUtils.createFolder(sourceFolderPath, POM_FILE_NAME, true);

    contentGenerator.createDescriptors();
  }

  @Test
  public void createContent() throws IOException {
    MuleContentGenerator contentGeneratorMock = mock(MuleContentGenerator.class);

    doNothing().when(contentGeneratorMock).createMuleSrcFolderContent();
    doNothing().when(contentGeneratorMock).createDescriptors();

    doCallRealMethod().when(contentGeneratorMock).createContent();
    contentGeneratorMock.createContent();

    verify(contentGeneratorMock, times(1)).createContent();
    verify(contentGeneratorMock, times(1)).createDescriptors();
  }
}

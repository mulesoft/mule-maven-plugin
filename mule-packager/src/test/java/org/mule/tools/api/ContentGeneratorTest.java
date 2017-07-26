/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.mule.tools.api.packager.ContentGenerator;
import org.mule.tools.api.packager.PackagerTestUtils;
import org.mule.tools.api.packager.packaging.PackagingType;

public class ContentGeneratorTest {

  protected static final String GROUP_ID = "org.mule.munit";
  protected static final String ARTIFACT_ID = "fake-id";
  protected static final String VERSION = "1.0.0-SNAPSHOT";

  private static final String POM_FILE_NAME = "pom.xml";
  private static final String FAKE_FILE_NAME = "fakeFile.xml";
  private static final String MULE_ARTIFACT_DESCRIPTOR_FILE_NAME = "mule-artifact.json";

  @Rule
  public TemporaryFolder projectBaseFolder = new TemporaryFolder();

  @Rule
  public TemporaryFolder projectTargetFolder = new TemporaryFolder();

  private PackagingType packagingType = PackagingType.MULE_APPLICATION;

  private ContentGenerator contentGenerator;


  @Before
  public void setUp() {
    contentGenerator = new ContentGenerator(GROUP_ID, ARTIFACT_ID, VERSION, packagingType,
                                            projectBaseFolder.getRoot().toPath(), projectTargetFolder.getRoot().toPath());
  }

  @Test(expected = IllegalArgumentException.class)
  public void failCreationProjectBaseFolderNonExistent() {
    new ContentGenerator(GROUP_ID, ARTIFACT_ID, VERSION, packagingType,
                         Paths.get("/fake/project/base/folder"), projectTargetFolder.getRoot().toPath());
  }

  @Test(expected = IllegalArgumentException.class)
  public void failCreationProjectTargetFolderNonExistent() {
    new ContentGenerator(GROUP_ID, ARTIFACT_ID, VERSION, packagingType,
                         projectBaseFolder.getRoot().toPath(), Paths.get("/fake/project/base/folder"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createSrcFolderContentNonExistingSourceFolder() throws IOException {
    String destinationFolderName = packagingType.getSourceFolderName();

    Path destinationFolderPath = projectTargetFolder.getRoot().toPath().resolve(destinationFolderName);
    PackagerTestUtils.createEmptyFolder(destinationFolderPath);

    contentGenerator.createSrcFolderContent();
  }

  @Test(expected = IllegalArgumentException.class)
  public void createSrcFolderContentNonExistingDestinationFolder() throws IOException {
    String sourceFolderName = packagingType.getSourceFolderName();

    Path sourceFolderPath = projectBaseFolder.getRoot().toPath().resolve(PackagerTestUtils.SRC).resolve(
                                                                                                        PackagerTestUtils.MAIN)
        .resolve(sourceFolderName);
    PackagerTestUtils.createFolder(sourceFolderPath, FAKE_FILE_NAME, true);

    contentGenerator.createSrcFolderContent();
  }

  @Test
  public void createSrcFolderContent() throws IOException {
    String sourceFolderName = packagingType.getSourceFolderName();
    String destinationFolderName = packagingType.getSourceFolderName();


    Path sourceFolderPath = projectBaseFolder.getRoot().toPath().resolve(PackagerTestUtils.SRC).resolve(
                                                                                                        PackagerTestUtils.MAIN)
        .resolve(sourceFolderName);
    PackagerTestUtils.createFolder(sourceFolderPath, FAKE_FILE_NAME, true);

    Path destinationFolderPath = projectTargetFolder.getRoot().toPath().resolve(destinationFolderName);
    PackagerTestUtils.createEmptyFolder(destinationFolderPath);

    contentGenerator.createSrcFolderContent();

    PackagerTestUtils.assertFileExists(destinationFolderPath.resolve(FAKE_FILE_NAME));
  }

  @Test
  public void createTestFolderContentNonExistingSourceFolder() throws IOException {
    String destinationFolderName = packagingType.getTestFolderName();

    Path destinationFolderPath = projectTargetFolder.getRoot().toPath().resolve(
                                                                                PackagerTestUtils.TEST_MULE)
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

    Path destinationFolderPath = projectTargetFolder.getRoot().toPath().resolve(
                                                                                PackagerTestUtils.TEST_MULE)
        .resolve(destinationFolderName);
    PackagerTestUtils.createEmptyFolder(destinationFolderPath);

    contentGenerator.createTestFolderContent();

    PackagerTestUtils.assertFileExists(destinationFolderPath.resolve(FAKE_FILE_NAME));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createMetaInfMuleSourceFolderContentNonExistingDestinationFolder() throws IOException {
    Path sourceFolderPath = projectBaseFolder.getRoot().toPath();
    PackagerTestUtils.createFolder(sourceFolderPath, FAKE_FILE_NAME, true);

    contentGenerator.createMetaInfMuleSourceFolderContent();
  }

  @Test
  public void createMetaInfMuleSourceFolderContent() throws IOException {
    Path sourceFolderPath = projectBaseFolder.getRoot().toPath();
    PackagerTestUtils.createFolder(sourceFolderPath, FAKE_FILE_NAME, true);

    Path destinationFolderPath = projectTargetFolder.getRoot().toPath().resolve(PackagerTestUtils.META_INF).resolve(
                                                                                                                    PackagerTestUtils.MULE_SRC)
        .resolve(ARTIFACT_ID);
    PackagerTestUtils.createEmptyFolder(destinationFolderPath);

    contentGenerator.createMetaInfMuleSourceFolderContent();
    PackagerTestUtils.assertFileExists(destinationFolderPath.resolve(FAKE_FILE_NAME));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createDescriptorsNoOriginalPom() throws IOException {
    String descriptorFileName = MULE_ARTIFACT_DESCRIPTOR_FILE_NAME;

    Path sourceFolderPath = projectBaseFolder.getRoot().toPath();
    PackagerTestUtils.createFolder(sourceFolderPath, descriptorFileName, true);

    contentGenerator.createDescriptors();
  }

  @Test(expected = IllegalArgumentException.class)
  public void createDescriptorsNoPomDestinationFolder() throws IOException {

    Path sourceFolderPath = projectBaseFolder.getRoot().toPath();
    PackagerTestUtils.createFolder(sourceFolderPath, POM_FILE_NAME, true);

    contentGenerator.createDescriptors();
  }

  @Test(expected = IllegalArgumentException.class)
  public void createDescriptorsNoOriginalDescriptor() throws IOException {
    Path sourceFolderPath = projectBaseFolder.getRoot().toPath();
    PackagerTestUtils.createFolder(sourceFolderPath, POM_FILE_NAME, true);

    Path pomPropertiesDestinationPath =
        projectTargetFolder.getRoot().toPath().resolve(PackagerTestUtils.META_INF).resolve(
                                                                                           PackagerTestUtils.MAVEN)
            .resolve(GROUP_ID).resolve(ARTIFACT_ID);
    PackagerTestUtils.createEmptyFolder(pomPropertiesDestinationPath);

    contentGenerator.createDescriptors();
  }

  @Test(expected = IllegalArgumentException.class)
  public void createDescriptorsNoDescriptorDestinationFolder() throws IOException {
    String descriptorFileName = MULE_ARTIFACT_DESCRIPTOR_FILE_NAME;

    Path sourceFolderPath = projectBaseFolder.getRoot().toPath();
    PackagerTestUtils.createFolder(sourceFolderPath, POM_FILE_NAME, true);
    PackagerTestUtils.createFolder(sourceFolderPath, descriptorFileName, true);

    Path pomPropertiesDestinationPath =
        projectTargetFolder.getRoot().toPath().resolve(PackagerTestUtils.META_INF).resolve(
                                                                                           PackagerTestUtils.MAVEN)
            .resolve(GROUP_ID).resolve(ARTIFACT_ID);
    PackagerTestUtils.createEmptyFolder(pomPropertiesDestinationPath);

    contentGenerator.createDescriptors();
  }

  @Test
  public void createDescriptors() throws IOException {
    String descriptorFileName = MULE_ARTIFACT_DESCRIPTOR_FILE_NAME;

    Path sourceFolderPath = projectBaseFolder.getRoot().toPath();
    PackagerTestUtils.createFolder(sourceFolderPath, POM_FILE_NAME, true);
    PackagerTestUtils.createFolder(sourceFolderPath, descriptorFileName, true);

    Path pomPropertiesDestinationPath =
        projectTargetFolder.getRoot().toPath().resolve(PackagerTestUtils.META_INF).resolve(
                                                                                           PackagerTestUtils.MAVEN)
            .resolve(GROUP_ID).resolve(ARTIFACT_ID);
    PackagerTestUtils.createEmptyFolder(pomPropertiesDestinationPath);

    Path descriptorDestinationPath = projectTargetFolder.getRoot().toPath().resolve(PackagerTestUtils.META_INF).resolve(
                                                                                                                        PackagerTestUtils.MULE_ARTIFACT);
    PackagerTestUtils.createEmptyFolder(descriptorDestinationPath);

    contentGenerator.createDescriptors();

    PackagerTestUtils.assertFileExists(pomPropertiesDestinationPath.resolve(POM_FILE_NAME));
    PackagerTestUtils.assertFileExists(pomPropertiesDestinationPath.resolve(PackagerTestUtils.POM_PROPERTIES));
    PackagerTestUtils.assertFileExists(descriptorDestinationPath.resolve(descriptorFileName));
  }

  @Test
  public void createDescriptorsPolicy() throws IOException {
    contentGenerator = new ContentGenerator(GROUP_ID, ARTIFACT_ID, VERSION, PackagingType.MULE_POLICY,
                                            projectBaseFolder.getRoot().toPath(), projectTargetFolder.getRoot().toPath());

    String descriptorFileName = MULE_ARTIFACT_DESCRIPTOR_FILE_NAME;

    Path sourceFolderPath = projectBaseFolder.getRoot().toPath();
    PackagerTestUtils.createFolder(sourceFolderPath, POM_FILE_NAME, true);
    PackagerTestUtils.createFolder(sourceFolderPath, descriptorFileName, true);

    Path pomPropertiesDestinationPath =
        projectTargetFolder.getRoot().toPath().resolve(PackagerTestUtils.META_INF).resolve(
                                                                                           PackagerTestUtils.MAVEN)
            .resolve(GROUP_ID).resolve(ARTIFACT_ID);
    PackagerTestUtils.createEmptyFolder(pomPropertiesDestinationPath);

    Path descriptorDestinationPath = projectTargetFolder.getRoot().toPath().resolve(PackagerTestUtils.META_INF).resolve(
                                                                                                                        PackagerTestUtils.MULE_ARTIFACT);
    PackagerTestUtils.createEmptyFolder(descriptorDestinationPath);

    contentGenerator.createDescriptors();

    PackagerTestUtils.assertFileExists(pomPropertiesDestinationPath.resolve(POM_FILE_NAME));
    PackagerTestUtils.assertFileExists(pomPropertiesDestinationPath.resolve(PackagerTestUtils.POM_PROPERTIES));
    PackagerTestUtils.assertFileExists(descriptorDestinationPath.resolve(descriptorFileName));
  }

  @Test
  public void createDescriptorsMuleDomain() throws IOException {
    contentGenerator = new ContentGenerator(GROUP_ID, ARTIFACT_ID, VERSION, PackagingType.MULE_DOMAIN,
                                            projectBaseFolder.getRoot().toPath(), projectTargetFolder.getRoot().toPath());

    String descriptorFileName = MULE_ARTIFACT_DESCRIPTOR_FILE_NAME;

    Path sourceFolderPath = projectBaseFolder.getRoot().toPath();
    PackagerTestUtils.createFolder(sourceFolderPath, POM_FILE_NAME, true);
    PackagerTestUtils.createFolder(sourceFolderPath, descriptorFileName, true);

    Path pomPropertiesDestinationPath =
        projectTargetFolder.getRoot().toPath().resolve(PackagerTestUtils.META_INF).resolve(
                                                                                           PackagerTestUtils.MAVEN)
            .resolve(GROUP_ID).resolve(ARTIFACT_ID);
    PackagerTestUtils.createEmptyFolder(pomPropertiesDestinationPath);

    Path descriptorDestinationPath = projectTargetFolder.getRoot().toPath().resolve(PackagerTestUtils.META_INF).resolve(
                                                                                                                        PackagerTestUtils.MULE_ARTIFACT);
    PackagerTestUtils.createEmptyFolder(descriptorDestinationPath);

    contentGenerator.createDescriptors();

    PackagerTestUtils.assertFileExists(pomPropertiesDestinationPath.resolve(POM_FILE_NAME));
    PackagerTestUtils.assertFileExists(pomPropertiesDestinationPath.resolve(PackagerTestUtils.POM_PROPERTIES));
    PackagerTestUtils.assertFileExists(descriptorDestinationPath.resolve(descriptorFileName));
  }

  @Test
  public void createContent() throws IOException {
    ContentGenerator contentGeneratorMock = mock(ContentGenerator.class);

    doNothing().when(contentGeneratorMock).createSrcFolderContent();
    doNothing().when(contentGeneratorMock).createMetaInfMuleSourceFolderContent();
    doNothing().when(contentGeneratorMock).createDescriptors();

    doCallRealMethod().when(contentGeneratorMock).createContent();
    contentGeneratorMock.createContent();

    verify(contentGeneratorMock, times(1)).createContent();
    verify(contentGeneratorMock, times(1)).createMetaInfMuleSourceFolderContent();
    verify(contentGeneratorMock, times(1)).createDescriptors();
  }
}

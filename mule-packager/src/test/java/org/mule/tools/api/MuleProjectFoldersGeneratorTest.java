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

import static org.mule.tools.api.packager.PackagerTestUtils.CLASSES;
import static org.mule.tools.api.packager.PackagerTestUtils.MAVEN;
import static org.mule.tools.api.packager.PackagerTestUtils.META_INF;
import static org.mule.tools.api.packager.PackagerTestUtils.MULE;
import static org.mule.tools.api.packager.PackagerTestUtils.MULE_ARTIFACT;
import static org.mule.tools.api.packager.PackagerTestUtils.MULE_SRC;
import static org.mule.tools.api.packager.PackagerTestUtils.MUNIT;
import static org.mule.tools.api.packager.PackagerTestUtils.POLICY;
import static org.mule.tools.api.packager.PackagerTestUtils.REPOSITORY;
import static org.mule.tools.api.packager.PackagerTestUtils.TEST_CLASSES;
import static org.mule.tools.api.packager.PackagerTestUtils.TEST_MULE;
import static org.mule.tools.api.packager.PackagerTestUtils.assertFileExists;
import static org.mule.tools.api.packager.PackagerTestUtils.assertFolderExist;
import static org.mule.tools.api.packager.PackagerTestUtils.assertFolderIsEmpty;
import static org.mule.tools.api.packager.PackagerTestUtils.createFolder;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.mule.tools.api.packager.MuleProjectFoldersGenerator;
import org.mule.tools.api.packager.packaging.PackagingType;

public class MuleProjectFoldersGeneratorTest {

  protected static final String GROUP_ID = "org.mule.munit";
  protected static final String ARTIFACT_ID = "fake-id";
  private static final String FAKE_FILE_NAME = "fakeFile.xml";

  @Rule
  public TemporaryFolder projectBaseFolder = new TemporaryFolder();

  private Path basePath;

  private MuleProjectFoldersGenerator generator;

  @Before
  public void setUp() {
    basePath = projectBaseFolder.getRoot().toPath();
  }

  @Test
  public void generateMuleApplication() {
    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_APPLICATION);
    generator.generate(projectBaseFolder.getRoot().toPath());

    checkNoPackageDependentFolders();
  }

  @Test
  public void generateMulePolicy() {
    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_POLICY);
    generator.generate(projectBaseFolder.getRoot().toPath());

    checkNoPackageDependentFolders();
  }

  @Test
  public void generateMuleDomain() {
    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_DOMAIN);
    generator.generate(projectBaseFolder.getRoot().toPath());

    checkNoPackageDependentFolders();
  }

  @Test
  public void generateClassesFolderAlreadyPresent() throws IOException {
    Path classesBasePath = basePath.resolve(CLASSES);
    createFolder(classesBasePath, FAKE_FILE_NAME, true);

    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_APPLICATION);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(basePath.resolve(CLASSES));
    assertFileExists(classesBasePath.resolve(FAKE_FILE_NAME));
  }

  @Test
  public void generateTestClassesFolderAlreadyPresent() throws IOException {
    Path classesBasePath = basePath.resolve(TEST_CLASSES);
    createFolder(classesBasePath, FAKE_FILE_NAME, true);

    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_APPLICATION);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(basePath.resolve(TEST_CLASSES));
    assertFileExists(classesBasePath.resolve(FAKE_FILE_NAME));
  }

  @Test
  public void generateMuleApplicationFolderAlreadyPresent() throws IOException {
    Path muleBasePath = basePath.resolve(MULE);
    createFolder(muleBasePath, FAKE_FILE_NAME, true);

    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_APPLICATION);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(basePath.resolve(MULE));
    assertFileExists(muleBasePath.resolve(FAKE_FILE_NAME));
  }

  @Test
  public void generateMulePolicyFolderAlreadyPresent() throws IOException {
    Path muleBasePath = basePath.resolve(POLICY);
    createFolder(muleBasePath, FAKE_FILE_NAME, true);

    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_POLICY);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(basePath.resolve(POLICY));
    assertFileExists(muleBasePath.resolve(FAKE_FILE_NAME));
  }

  @Test
  public void generateMuleDomainFolderAlreadyPresent() throws IOException {
    Path muleBasePath = basePath.resolve(MULE);
    createFolder(muleBasePath, FAKE_FILE_NAME, true);

    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_DOMAIN);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(basePath.resolve(MULE));
    assertFileExists(muleBasePath.resolve(FAKE_FILE_NAME));
  }

  @Test
  public void generateTestMuleFolderAlreadyPresent() throws IOException {
    Path munitTestMulePath = basePath.resolve(TEST_MULE).resolve(MUNIT);
    createFolder(munitTestMulePath, FAKE_FILE_NAME, true);

    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_APPLICATION);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(munitTestMulePath);
    assertFileExists(munitTestMulePath.resolve(FAKE_FILE_NAME));
  }

  @Test
  public void generateMuleSourceFolderAlreadyPresent() throws IOException {
    Path artifactIdMuleSrcMetaInfPath = basePath.resolve(META_INF).resolve(MULE_SRC).resolve(ARTIFACT_ID);
    createFolder(artifactIdMuleSrcMetaInfPath, FAKE_FILE_NAME, true);

    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_APPLICATION);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(artifactIdMuleSrcMetaInfPath);
    assertFileExists(artifactIdMuleSrcMetaInfPath.resolve(FAKE_FILE_NAME));
  }

  @Test
  public void generateArtifactIdGroupIdMavenMetaInfFoldersAlreadyPresent() throws IOException {
    Path artifactIdGroupIdMavenMetaInfPath = basePath.resolve(META_INF).resolve(MAVEN).resolve(GROUP_ID).resolve(ARTIFACT_ID);
    createFolder(artifactIdGroupIdMavenMetaInfPath, FAKE_FILE_NAME, true);

    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_APPLICATION);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(artifactIdGroupIdMavenMetaInfPath);
    assertFileExists(artifactIdGroupIdMavenMetaInfPath.resolve(FAKE_FILE_NAME));
  }

  @Test
  public void generateMuleArtifactMetaInfFoldersAlreadyPresent() throws IOException {
    Path muleArtifactMetaInfPath = basePath.resolve(META_INF).resolve(MULE_ARTIFACT);
    createFolder(muleArtifactMetaInfPath, FAKE_FILE_NAME, true);

    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_APPLICATION);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(muleArtifactMetaInfPath);
    assertFileExists(muleArtifactMetaInfPath.resolve(FAKE_FILE_NAME));
  }

  @Test
  public void generateRespositoryFoldersAlreadyPresent() throws IOException {
    Path repositoryPath = basePath.resolve(REPOSITORY);
    createFolder(repositoryPath, FAKE_FILE_NAME, true);

    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_APPLICATION);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(repositoryPath);
    assertFileExists(repositoryPath.resolve(FAKE_FILE_NAME));
  }

  private void checkNoPackageDependentFolders() {
    assertFolderExist(basePath.resolve(CLASSES));
    assertFolderIsEmpty(basePath.resolve(CLASSES));

    assertFolderExist(basePath.resolve(TEST_CLASSES));
    assertFolderIsEmpty(basePath.resolve(TEST_CLASSES));

    assertFolderExist(basePath.resolve(TEST_MULE).resolve(MUNIT));
    assertFolderIsEmpty(basePath.resolve(TEST_MULE).resolve(MUNIT));

    assertFolderExist(basePath.resolve(META_INF).resolve(MULE_SRC).resolve(ARTIFACT_ID));
    assertFolderIsEmpty(basePath.resolve(META_INF).resolve(MULE_SRC).resolve(ARTIFACT_ID));

    assertFolderExist(basePath.resolve(META_INF).resolve(MAVEN).resolve(GROUP_ID).resolve(ARTIFACT_ID));
    assertFolderIsEmpty(basePath.resolve(META_INF).resolve(MAVEN).resolve(GROUP_ID).resolve(ARTIFACT_ID));

    assertFolderExist(basePath.resolve(META_INF).resolve(MULE_ARTIFACT));
    assertFolderIsEmpty(basePath.resolve(META_INF).resolve(MULE_ARTIFACT));

    assertFolderExist(basePath.resolve(REPOSITORY));
    assertFolderIsEmpty(basePath.resolve(REPOSITORY));
  }
}

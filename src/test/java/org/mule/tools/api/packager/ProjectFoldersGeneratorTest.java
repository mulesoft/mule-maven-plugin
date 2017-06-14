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

import static org.mule.tools.api.packager.PackagerTestUtils.MAVEN;
import static org.mule.tools.api.packager.PackagerTestUtils.META_INF;
import static org.mule.tools.api.packager.PackagerTestUtils.MULE;
import static org.mule.tools.api.packager.PackagerTestUtils.MULE_ARTIFACT;
import static org.mule.tools.api.packager.PackagerTestUtils.MULE_SRC;
import static org.mule.tools.api.packager.PackagerTestUtils.MUNIT;
import static org.mule.tools.api.packager.PackagerTestUtils.POLICY;
import static org.mule.tools.api.packager.PackagerTestUtils.REPOSITORY;
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

import org.mule.tools.maven.mojo.model.PackagingType;

public class ProjectFoldersGeneratorTest {

  protected static final String GROUP_ID = "org.mule.munit";
  protected static final String ARTIFACT_ID = "fake-id";

  @Rule
  public TemporaryFolder projectBaseFolder = new TemporaryFolder();

  private Path basePath;

  private ProjectFoldersGenerator generator;

  @Before
  public void setUp() {
    basePath = projectBaseFolder.getRoot().toPath();
  }

  @Test
  public void generateMuleApplication() {
    generator = new ProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_APPLICATION);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(basePath.resolve(MULE));
    assertFolderIsEmpty(basePath.resolve(MULE));

    checkNoPackageDependentFolders();
  }

  @Test
  public void generateMulePolicy() {
    generator = new ProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_POLICY);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(basePath.resolve(POLICY));
    assertFolderIsEmpty(basePath.resolve(POLICY));

    checkNoPackageDependentFolders();
  }

  @Test
  public void generateMuleDomain() {
    generator = new ProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_DOMAIN);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(basePath.resolve(MULE));
    assertFolderIsEmpty(basePath.resolve(MULE));

    checkNoPackageDependentFolders();
  }

  @Test
  public void generateMuleApplicationFolderAlreadyPresent() throws IOException {
    String fakeFileName = "fakeFile.xml";

    Path muleBasePath = basePath.resolve(MULE);
    createFolder(muleBasePath, fakeFileName, true);

    generator = new ProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_APPLICATION);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(basePath.resolve(MULE));
    assertFileExists(muleBasePath.resolve(fakeFileName));
  }

  @Test
  public void generateMulePolicyFolderAlreadyPresent() throws IOException {
    String fakeFileName = "fakeFile.xml";

    Path muleBasePath = basePath.resolve(POLICY);
    createFolder(muleBasePath, fakeFileName, true);

    generator = new ProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_POLICY);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(basePath.resolve(POLICY));
    assertFileExists(muleBasePath.resolve(fakeFileName));
  }

  @Test
  public void generateMuleDomainFolderAlreadyPresent() throws IOException {
    String fakeFileName = "fakeFile.xml";

    Path muleBasePath = basePath.resolve(MULE);
    createFolder(muleBasePath, fakeFileName, true);

    generator = new ProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_DOMAIN);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(basePath.resolve(MULE));
    assertFileExists(muleBasePath.resolve(fakeFileName));
  }

  @Test
  public void generateFoldersAlreadyPresent() throws IOException {
    String fakeFileName = "fakeFile.xml";

    Path munitTestMulePath = basePath.resolve(TEST_MULE).resolve(MUNIT);
    createFolder(munitTestMulePath, fakeFileName, true);

    Path artifactIdMuleSrcMetaInfPath = basePath.resolve(META_INF).resolve(MULE_SRC).resolve(ARTIFACT_ID);
    createFolder(artifactIdMuleSrcMetaInfPath, fakeFileName, true);

    Path artifactIdGroupIdMavenMetaInfPath = basePath.resolve(META_INF).resolve(MAVEN).resolve(GROUP_ID).resolve(ARTIFACT_ID);
    createFolder(artifactIdGroupIdMavenMetaInfPath, fakeFileName, true);

    Path muleArtifactMetaInfPath = basePath.resolve(META_INF).resolve(MULE_ARTIFACT);
    createFolder(muleArtifactMetaInfPath, fakeFileName, true);

    Path repositoryPath = basePath.resolve(REPOSITORY);
    createFolder(repositoryPath, fakeFileName, true);


    generator = new ProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_APPLICATION);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(munitTestMulePath);
    assertFileExists(munitTestMulePath.resolve(fakeFileName));

    assertFolderExist(artifactIdMuleSrcMetaInfPath);
    assertFileExists(artifactIdMuleSrcMetaInfPath.resolve(fakeFileName));

    assertFolderExist(artifactIdGroupIdMavenMetaInfPath);
    assertFileExists(artifactIdGroupIdMavenMetaInfPath.resolve(fakeFileName));

    assertFolderExist(muleArtifactMetaInfPath);
    assertFileExists(muleArtifactMetaInfPath.resolve(fakeFileName));

    assertFolderExist(repositoryPath);
    assertFileExists(repositoryPath.resolve(fakeFileName));
  }

  private void checkNoPackageDependentFolders() {
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

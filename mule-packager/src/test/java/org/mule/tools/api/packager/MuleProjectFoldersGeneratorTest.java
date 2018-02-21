/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager;

import static org.mule.tools.api.packager.PackagerTestUtils.CLASSES;
import static org.mule.tools.api.packager.PackagerTestUtils.MULE;
import static org.mule.tools.api.packager.PackagerTestUtils.MUNIT;
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
    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE);
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

    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(basePath.resolve(CLASSES));
    assertFileExists(classesBasePath.resolve(FAKE_FILE_NAME));
  }

  @Test
  public void generateTestClassesFolderAlreadyPresent() throws IOException {
    Path classesBasePath = basePath.resolve(TEST_CLASSES);
    createFolder(classesBasePath, FAKE_FILE_NAME, true);

    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(basePath.resolve(TEST_CLASSES));
    assertFileExists(classesBasePath.resolve(FAKE_FILE_NAME));
  }

  @Test
  public void generateMuleApplicationFolderAlreadyPresent() throws IOException {
    Path muleBasePath = basePath.resolve(MULE);
    createFolder(muleBasePath, FAKE_FILE_NAME, true);

    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(basePath.resolve(MULE));
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

    generator = new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(munitTestMulePath);
    assertFileExists(munitTestMulePath.resolve(FAKE_FILE_NAME));
  }

  private void checkNoPackageDependentFolders() {
    assertFolderExist(basePath.resolve(CLASSES));
    assertFolderIsEmpty(basePath.resolve(CLASSES));

    assertFolderExist(basePath.resolve(TEST_CLASSES));
    assertFolderIsEmpty(basePath.resolve(TEST_CLASSES));

    assertFolderExist(basePath.resolve(TEST_MULE).resolve(MUNIT));
    assertFolderIsEmpty(basePath.resolve(TEST_MULE).resolve(MUNIT));
  }
}

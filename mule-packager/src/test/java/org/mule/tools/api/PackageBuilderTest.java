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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mule.tools.api.packager.structure.PackagerFolders.CLASSES;
import static org.mule.tools.api.packager.structure.PackagerFolders.MAVEN;
import static org.mule.tools.api.packager.structure.PackagerFolders.META_INF;
import static org.mule.tools.api.packager.structure.PackagerFolders.MULE;
import static org.mule.tools.api.packager.structure.PackagerFolders.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.PackagerFolders.MULE_SRC;
import static org.mule.tools.api.packager.structure.PackagerFolders.POLICY;
import static org.mule.tools.api.packager.structure.PackagerFolders.REPOSITORY;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import org.mule.tools.api.packager.MuleArchiver;
import org.mule.tools.api.packager.PackageBuilder;
import org.mule.tools.api.packager.packaging.PackagingType;

@RunWith(MockitoJUnitRunner.class)
public class PackageBuilderTest {

  private File destinationFileMock;
  private PackageBuilder packageBuilder;
  private PackageBuilder packageBuilderSpy;
  private File destinationFile;

  @Rule
  public TemporaryFolder targetFileFolder = new TemporaryFolder();
  @Rule
  public TemporaryFolder destinationFileParent = new TemporaryFolder();

  @Before
  public void before() throws IOException {
    this.packageBuilder = new PackageBuilder();
    this.packageBuilderSpy = spy(PackageBuilder.class);
    this.destinationFileMock = mock(File.class);
    doNothing().when(packageBuilderSpy).createDeployableFile();
    destinationFile = new File(destinationFileParent.getRoot(), "destinationFile.jar");
  }

  @Test(expected = IllegalArgumentException.class)
  public void setNullClassesFolderTest() {
    this.packageBuilder.withClasses(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setNullMuleFolderTest() {
    this.packageBuilder.withMule(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setNullRootResourceFileTest() {
    this.packageBuilder.withRootResource(null);
  }

  @Test
  public void addRootResourcesTest() throws NoSuchFieldException, IllegalAccessException {
    Class<?> clazz = this.packageBuilder.getClass();
    Field field = clazz.getDeclaredField("rootResources");
    field.setAccessible(true);
    List<File> actualRootResourcesList = (List<File>) field.get(this.packageBuilder);

    Assert.assertTrue("The list of root resources should be empty", actualRootResourcesList.isEmpty());
    this.packageBuilder.withRootResource(mock(File.class));
    Assert.assertEquals("The list of root resources should contain one element", 1, actualRootResourcesList.size());
    this.packageBuilder.withRootResource(mock(File.class));
    Assert.assertEquals("The list of root resources should contain two elements", 2, actualRootResourcesList.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void setNullDestinationFileTest() {
    this.packageBuilder.withDestinationFile(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setExistentDestinationFileTest() {
    when(destinationFileMock.exists()).thenReturn(true);
    this.packageBuilder.withDestinationFile(destinationFileMock);
  }

  @Test
  public void setDestinationFileTest() {
    when(destinationFileMock.exists()).thenReturn(false);
    this.packageBuilder.withDestinationFile(destinationFileMock);
    verify(destinationFileMock, times(1)).exists();
  }

  @Test(expected = NullPointerException.class)
  public void setNullArchiverTest() {
    this.packageBuilder.withArchiver(null);
  }

  @Test
  public void setArchiverTest() {
    Class expectedDefaultMuleArchiverClass = MuleArchiver.class;
    Class actualDefaultMuleArchiverClass = this.packageBuilder.getMuleArchiver().getClass();
    Assert.assertEquals("Expected and actual default mule org.mule.tools.artifact.archiver does not match",
                        expectedDefaultMuleArchiverClass, actualDefaultMuleArchiverClass);

    class MuleArchiverSubclass extends MuleArchiver {
    };
    Class expectedMuleArchiverClass = MuleArchiverSubclass.class;
    this.packageBuilder.withArchiver(new MuleArchiverSubclass());
    Class actualMuleArchiverClass = this.packageBuilder.getMuleArchiver().getClass();
    Assert.assertEquals("Expected and actual mule org.mule.tools.artifact.archiver does not match", expectedMuleArchiverClass,
                        actualMuleArchiverClass);
  }

  @Test
  public void createDeployableFileSettingMuleFolderTest() throws IOException {
    File muleFolderMock = mock(File.class);
    when(muleFolderMock.exists()).thenReturn(true);
    when(muleFolderMock.isDirectory()).thenReturn(true);
    this.packageBuilder.withMule(muleFolderMock);

    MuleArchiver muleArchiverMock = mock(MuleArchiver.class);
    this.packageBuilder.withArchiver(muleArchiverMock);

    this.packageBuilder.withDestinationFile(destinationFileMock);

    this.packageBuilder.createDeployableFile();

    verify(muleArchiverMock, times(1)).addMule(muleFolderMock, null, null);
    verify(muleArchiverMock, times(1)).setDestFile(destinationFileMock);
    verify(muleArchiverMock, times(1)).createArchive();
  }

  @Test
  public void createDeployableFileSettingClassesFolderTest() throws IOException {
    File classesFolderMock = mock(File.class);
    when(classesFolderMock.exists()).thenReturn(true);
    when(classesFolderMock.isDirectory()).thenReturn(true);
    this.packageBuilder.withClasses(classesFolderMock);

    MuleArchiver muleArchiverMock = mock(MuleArchiver.class);
    this.packageBuilder.withArchiver(muleArchiverMock);

    this.packageBuilder.withDestinationFile(destinationFileMock);

    this.packageBuilder.createDeployableFile();

    verify(muleArchiverMock, times(1)).addClasses(classesFolderMock, null, null);
    verify(muleArchiverMock, times(1)).setDestFile(destinationFileMock);
    verify(muleArchiverMock, times(1)).createArchive();
  }

  @Test
  public void fromWorkingDirectoryWithNonExistingFoldersInWorkingDir() throws IOException {
    Path workingDirectory = targetFileFolder.getRoot().toPath();
    MuleArchiver muleArchiverMock = mock(MuleArchiver.class);

    packageBuilder = new PackageBuilder();
    packageBuilder.withArchiver(muleArchiverMock);
    packageBuilder.withDestinationFile(destinationFileMock);
    packageBuilder.fromWorkingDirectory(workingDirectory);

    packageBuilder.createDeployableFile();

    verify(muleArchiverMock, times(0)).addClasses(workingDirectory.resolve(CLASSES).toFile(), null, null);
    verify(muleArchiverMock, times(0)).addMule(workingDirectory.resolve(MULE).toFile(), null, null);
    verify(muleArchiverMock, times(0)).addPolicy(workingDirectory.resolve(POLICY).toFile(), null, null);
    verify(muleArchiverMock, times(0)).addMaven(workingDirectory.resolve(META_INF).resolve(MAVEN).toFile(), null, null);
    verify(muleArchiverMock, times(0)).addMuleArtifact(workingDirectory.resolve(META_INF).resolve(MULE_ARTIFACT).toFile(),
                                                       null, null);
    verify(muleArchiverMock, times(0)).addMuleSrc(workingDirectory.resolve(META_INF).resolve(MULE_SRC).toFile(), null, null);
    verify(muleArchiverMock, times(0)).addRepository(workingDirectory.resolve(REPOSITORY).toFile(), null, null);
    verify(muleArchiverMock, times(1)).setDestFile(destinationFileMock);
    verify(muleArchiverMock, times(1)).createArchive();
  }

  @Test
  public void fromWorkingDirectory() throws IOException {
    Path workingDirectory = targetFileFolder.getRoot().toPath();
    MuleArchiver muleArchiverMock = mock(MuleArchiver.class);

    packageBuilder = new PackageBuilder();
    packageBuilder.withArchiver(muleArchiverMock);
    packageBuilder.withDestinationFile(destinationFileMock);
    packageBuilder.fromWorkingDirectory(workingDirectory);

    workingDirectory.resolve(CLASSES).toFile().mkdirs();
    workingDirectory.resolve(MULE).toFile().mkdirs();
    workingDirectory.resolve(POLICY).toFile().mkdirs();
    workingDirectory.resolve(META_INF).resolve(MAVEN).toFile().mkdirs();
    workingDirectory.resolve(META_INF).resolve(MULE_ARTIFACT).toFile().mkdirs();
    workingDirectory.resolve(META_INF).resolve(MULE_SRC).toFile().mkdirs();
    workingDirectory.resolve(REPOSITORY).toFile().mkdirs();

    packageBuilder.createDeployableFile();

    verify(muleArchiverMock, times(1)).addClasses(workingDirectory.resolve(CLASSES).toFile(), null, null);
    verify(muleArchiverMock, times(1)).addMule(workingDirectory.resolve(MULE).toFile(), null, null);
    verify(muleArchiverMock, times(1)).addPolicy(workingDirectory.resolve(POLICY).toFile(), null, null);
    verify(muleArchiverMock, times(1)).addMaven(workingDirectory.resolve(META_INF).resolve(MAVEN).toFile(), null, null);
    verify(muleArchiverMock, times(1)).addMuleArtifact(workingDirectory.resolve(META_INF).resolve(MULE_ARTIFACT).toFile(),
                                                       null, null);
    verify(muleArchiverMock, times(1)).addMuleSrc(workingDirectory.resolve(META_INF).resolve(MULE_SRC).toFile(), null, null);
    verify(muleArchiverMock, times(1)).addRepository(workingDirectory.resolve(REPOSITORY).toFile(), null, null);
    verify(muleArchiverMock, times(1)).setDestFile(destinationFileMock);
    verify(muleArchiverMock, times(1)).createArchive();
  }

  @Test
  public void createMuleAppOnlyMuleSourcesTest() throws IOException {
    packageBuilderSpy.createMuleApp(destinationFile, targetFileFolder.getRoot().getPath(), PackagingType.MULE_APPLICATION, true,
                                    true, true);
    verify(packageBuilderSpy, times(1)).withDestinationFile(any());
    verify(packageBuilderSpy, times(0)).withClasses(any());
    verify(packageBuilderSpy, times(0)).withMule(any());
    verify(packageBuilderSpy, times(0)).withRepository(any());
  }

  @Test
  public void createMuleAppWithBinariesTest() throws IOException {
    packageBuilderSpy.createMuleApp(destinationFile, targetFileFolder.getRoot().getPath(), PackagingType.MULE_APPLICATION, false,
                                    false, false);
    verify(packageBuilderSpy, times(1)).withDestinationFile(any());
    verify(packageBuilderSpy, times(1)).withClasses(any());
    verify(packageBuilderSpy, times(1)).withMule(any());
    verify(packageBuilderSpy, times(1)).withRepository(any());
    verify(packageBuilderSpy, times(1)).withMuleArtifact(any());
    verify(packageBuilderSpy, times(1)).withMaven(any());
  }

  @Test
  public void createMuleAppWithBinariesAndSourcesTest() throws IOException {
    packageBuilderSpy.createMuleApp(destinationFile, targetFileFolder.getRoot().getPath(), PackagingType.MULE_APPLICATION, false,
            false, true);
    verify(packageBuilderSpy, times(1)).withDestinationFile(any());
    verify(packageBuilderSpy, times(1)).withClasses(any());
    verify(packageBuilderSpy, times(1)).withMule(any());
    verify(packageBuilderSpy, times(1)).withRepository(any());
  }

}

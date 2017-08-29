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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.packager.structure.FolderNames.CLASSES;
import static org.mule.tools.api.packager.structure.FolderNames.MAVEN;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_SRC;
import static org.mule.tools.api.packager.structure.FolderNames.REPOSITORY;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

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
  public void setUp() throws IOException {
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
  public void setNullRootResourceFileTest() {
    this.packageBuilder.withRootResource(null);
  }

  @Test
  public void addRootResourcesTest() throws NoSuchFieldException, IllegalAccessException {
    Field field = PackageBuilder.class.getDeclaredField("rootResources");
    field.setAccessible(true);
    List<File> actualRootResourcesList = (List<File>) field.get(this.packageBuilder);

    assertTrue("The list of root resources should be empty", actualRootResourcesList.isEmpty());
    this.packageBuilder.withRootResource(mock(File.class));
    assertEquals("The list of root resources should contain one element", 1, actualRootResourcesList.size());
    this.packageBuilder.withRootResource(mock(File.class));
    assertEquals("The list of root resources should contain two elements", 2, actualRootResourcesList.size());
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
    assertEquals("Expected and actual default mule org.mule.tools.artifact.archiver does not match",
                 expectedDefaultMuleArchiverClass, actualDefaultMuleArchiverClass);

    class MuleArchiverSubclass extends MuleArchiver {
    };
    Class expectedMuleArchiverClass = MuleArchiverSubclass.class;
    this.packageBuilder.withArchiver(new MuleArchiverSubclass());
    Class actualMuleArchiverClass = this.packageBuilder.getMuleArchiver().getClass();
    assertEquals("Expected and actual mule org.mule.tools.artifact.archiver does not match", expectedMuleArchiverClass,
                 actualMuleArchiverClass);
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

    verify(muleArchiverMock, times(1)).addToRoot(classesFolderMock, null, null);
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

    verify(muleArchiverMock, times(0)).addToRoot(workingDirectory.resolve(CLASSES.value()).toFile(), null, null);

    verify(muleArchiverMock, times(0)).addMaven(workingDirectory.resolve(META_INF.value()).resolve(MAVEN.value()).toFile(), null,
                                                null);
    verify(muleArchiverMock, times(0))
        .addMuleArtifact(workingDirectory.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile(),
                         null, null);
    verify(muleArchiverMock, times(0)).addMuleSrc(workingDirectory.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile(),
                                                  null, null);
    verify(muleArchiverMock, times(0)).addRepository(workingDirectory.resolve(REPOSITORY.value()).toFile(), null, null);
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

    workingDirectory.resolve(CLASSES.value()).toFile().mkdirs();
    workingDirectory.resolve(META_INF.value()).resolve(MAVEN.value()).toFile().mkdirs();
    workingDirectory.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile().mkdirs();
    workingDirectory.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile().mkdirs();
    workingDirectory.resolve(REPOSITORY.value()).toFile().mkdirs();

    packageBuilder.createDeployableFile();

    verify(muleArchiverMock, times(1)).addToRoot(workingDirectory.resolve(CLASSES.value()).toFile(), null, null);
    verify(muleArchiverMock, times(1)).addMaven(workingDirectory.resolve(META_INF.value()).resolve(MAVEN.value()).toFile(), null,
                                                null);
    verify(muleArchiverMock, times(1))
        .addMuleArtifact(workingDirectory.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile(),
                         null, null);
    verify(muleArchiverMock, times(1)).addMuleSrc(workingDirectory.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile(),
                                                  null, null);
    verify(muleArchiverMock, times(1)).addRepository(workingDirectory.resolve(REPOSITORY.value()).toFile(), null, null);
    verify(muleArchiverMock, times(1)).setDestFile(destinationFileMock);
    verify(muleArchiverMock, times(1)).createArchive();
  }

  @Test
  public void createMuleAppOnlyMuleSourcesTest() throws IOException {
    packageBuilderSpy.createMuleApp(destinationFile, targetFileFolder.getRoot().getPath(), PackagingType.MULE_APPLICATION, true,
                                    true, true);
    verify(packageBuilderSpy, times(1)).withDestinationFile(any());
    verify(packageBuilderSpy, times(0)).withClasses(any());
    verify(packageBuilderSpy, times(0)).withRepository(any());
  }

  @Test
  public void createMuleAppWithBinariesTest() throws IOException {
    packageBuilderSpy.createMuleApp(destinationFile, targetFileFolder.getRoot().getPath(), PackagingType.MULE_APPLICATION, false,
                                    false, false);
    verify(packageBuilderSpy, times(1)).withDestinationFile(any());
    verify(packageBuilderSpy, times(1)).withClasses(any());
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
    verify(packageBuilderSpy, times(1)).withRepository(any());
  }

}

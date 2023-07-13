/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.builder;

//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.core.IsInstanceOf.instanceOf;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.packager.builder.MulePackageBuilder.CLASSLOADER_MODEL_JSON;
import static org.mule.tools.api.packager.structure.FolderNames.CLASSES;
import static org.mule.tools.api.packager.structure.FolderNames.MAVEN;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_SRC;
import static org.mule.tools.api.packager.structure.FolderNames.MUNIT;
import static org.mule.tools.api.packager.structure.FolderNames.REPOSITORY;
import static org.mule.tools.api.packager.structure.FolderNames.TEST_CLASSES;
import static org.mule.tools.api.packager.structure.FolderNames.TEST_MULE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//import org.junit.rules.TemporaryFolder;
//import org.junit.runner.RunWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.MockitoJUnitRunner;

import org.mule.tools.api.packager.MuleProjectFoldersGenerator;
import org.mule.tools.api.packager.archiver.MuleArchiver;
import org.mule.tools.api.packager.packaging.PackagingOptions;
import org.mule.tools.api.packager.packaging.PackagingType;

//@RunWith(MockitoJUnitRunner.class)
public class MulePackageBuilderTest {

  private static final String GROUP_ID = "com.fake.group";
  private static final String ARTIFACT_ID = "fake-id";
  private static final PackagingType PACKAGING_TYPE = PackagingType.MULE_APPLICATION;

  private MuleArchiver archiverMock;

  private File destinationFile;

  private MulePackageBuilder builder;

  @TempDir
  public File fakeTargetFolder;

  @BeforeEach
  public void setUp() throws IOException {
    archiverMock = mock(MuleArchiver.class);

    new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PACKAGING_TYPE).generate(fakeTargetFolder.toPath());

    destinationFile = new File(fakeTargetFolder.getPath(), "destination.jar");

    builder = new MulePackageBuilder();
    builder.withArchiver(archiverMock);

    if (destinationFile.exists()) {
      destinationFile.delete();
    }
  }

  @Test
  public void setNullClassesFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> this.builder.withClasses(null));
    String expectedMessage = "The folder must not be null";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNonExistentClassesFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> builder.withClasses(new File("fake")));
    String expectedMessage = "The folder must exists";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNullTestClassesFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> this.builder.withTestClasses(null));
    String expectedMessage = "The folder must not be null";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNonExistentTestClassesFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> this.builder.withTestClasses(new File("fake")));
    String expectedMessage = "The folder must exists";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNullTestMuleFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> this.builder.withTestMule(null));
    String expectedMessage = "The folder must not be null";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNonExistentTestMuleFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> this.builder.withTestMule(new File("fake")));
    String expectedMessage = "The folder must exists";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNullMavenFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> this.builder.withMaven(null));
    String expectedMessage = "The folder must not be null";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNonExistentMavenFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> this.builder.withMaven(new File("fake")));
    String expectedMessage = "The folder must exists";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNullMuleSrcFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> this.builder.withMuleSrc(null));
    String expectedMessage = "The folder must not be null";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNonExistentMuleSrcFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> this.builder.withMuleSrc(new File("fake")));
    String expectedMessage = "The folder must exists";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNullMuleArtifactFolderTest() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> this.builder.withMuleArtifact(null));
    String expectedMessage = "The folder must not be null";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNonExistentMuleArtifactFolderTest() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> this.builder.withMuleArtifact(new File("fake")));
    String expectedMessage = "The folder must exists";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNullRepositoryFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> this.builder.withRepository(null));
    String expectedMessage = "The folder must not be null";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNonExistentRepositoryFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> this.builder.withRepository(new File("fake")));
    String expectedMessage = "The folder must exists";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNullRootResourceFile() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> this.builder.withRootResource(null));
    String expectedMessage = "The resource must not be null";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNonExistentRootResourceFile() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> this.builder.withRootResource(new File("fake")));
    String expectedMessage = "The resource must exists";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void addRootResources() throws NoSuchFieldException, IllegalAccessException {
    List<File> actualRootResourcesList = builder.rootResources;
    assertThat(actualRootResourcesList.isEmpty()).describedAs("The list of root resources should be empty").isTrue();

    File rootResourceMock = mock(File.class);
    when(rootResourceMock.exists()).thenReturn(true);
    builder.withRootResource(rootResourceMock);
    assertThat(actualRootResourcesList.size()).describedAs("The list of root resources should contain one element").isEqualTo(1);
  }

  @Test
  public void setNullPackagingOptions() {
    assertThrows(IllegalArgumentException.class, () -> this.builder.withPackagingOptions(null));

  }

  @Test
  public void setNullArchiver() {
    assertThrows(IllegalArgumentException.class, () -> this.builder.withArchiver(null));
  }

  @Test
  public void setArchiver() {
    assertThat(builder.getArchiver()).describedAs("Default archiver type is wrong").isInstanceOf(MuleArchiver.class);

    class MuleArchiverSubclass extends MuleArchiver {
    }
    builder.withArchiver(new MuleArchiverSubclass());
    assertThat(builder.getArchiver()).describedAs("archiver type is wrong").isInstanceOf(MuleArchiverSubclass.class);
  }

  @Test
  public void createPackageNullOriginFolderPath() {
    assertThrows(IllegalArgumentException.class, () -> builder.createPackage(null, destinationFile.toPath()));
  }

  @Test
  public void createPackageNonExistingOriginFolderPath() {
    assertThrows(IllegalArgumentException.class,
                 () -> builder.createPackage(new File("fake").toPath(), destinationFile.toPath()));
  }

  @Test
  public void createPackageNullDestinationPath() {
    assertThrows(IllegalArgumentException.class, () -> {
      boolean onlyMuleSources = true;
      boolean lightweightPackage = false;
      boolean attachMuleSources = false;
      boolean testPackage = false;

      builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

      builder.createPackage(fakeTargetFolder.toPath(), null);
    });
  }

  @Test
  public void createPackageAlreadyExistingDestinationPath() {
    assertThrows(IllegalArgumentException.class, () -> {
      boolean onlyMuleSources = true;
      boolean lightweightPackage = false;
      boolean attachMuleSources = false;
      boolean testPackage = false;

      builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

      destinationFile.createNewFile();
      builder.createPackage(fakeTargetFolder.toPath(), destinationFile.toPath());
    });
  }

  @Test
  public void createPackageNoPackagingOptionsProvided() {
    assertThrows(IllegalStateException.class, () -> builder.createPackage(fakeTargetFolder.toPath(), destinationFile.toPath()));
  }

  @Test
  public void createPackageOnlyMuleSources() throws IOException {
    boolean onlyMuleSources = true;
    boolean lightweightPackage = false;
    boolean attachMuleSources = false;
    boolean testPackage = false;

    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.createPackage(fakeTargetFolder.toPath(), destinationFile.toPath());

    Path targetPath = fakeTargetFolder.toPath();
    verify(archiverMock, times(0)).addToRoot(targetPath.resolve(CLASSES.value()).toFile(), null, null);
    verify(archiverMock, times(0)).addMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile(), null, null);
    verify(archiverMock, times(0)).addMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile(),
                                                   null, null);
    verify(archiverMock, times(1)).addMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile(), null,
                                              null);
    verify(archiverMock, times(0)).addRepository(targetPath.resolve(REPOSITORY.value()).toFile(), null, null);
    verify(archiverMock, times(0)).addToRoot(targetPath.resolve(TEST_MULE.value()).toFile(), null, null);
    verify(archiverMock, times(0)).addToRoot(targetPath.resolve(TEST_CLASSES.value()).toFile(), null, null);

    verify(archiverMock, times(1)).setDestFile(destinationFile);
    verify(archiverMock, times(1)).createArchive();
  }

  @Test
  public void createPackageLightweight() throws IOException {
    boolean onlyMuleSources = false;
    boolean lightweightPackage = true;
    boolean attachMuleSources = true;
    boolean testPackage = false;

    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.createPackage(fakeTargetFolder.toPath(), destinationFile.toPath());

    Path targetPath = fakeTargetFolder.toPath();
    verify(archiverMock, times(1)).addToRoot(targetPath.resolve(CLASSES.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile(),
                                                   null, new String[] {CLASSLOADER_MODEL_JSON});
    verify(archiverMock, times(1)).addMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile(), null,
                                              null);
    verify(archiverMock, times(0)).addRepository(targetPath.resolve(REPOSITORY.value()).toFile(), null, null);
    verify(archiverMock, times(0)).addToRoot(targetPath.resolve(TEST_MULE.value()).toFile(), null, null);
    verify(archiverMock, times(0)).addToRoot(targetPath.resolve(TEST_CLASSES.value()).toFile(), null, null);

    verify(archiverMock, times(1)).setDestFile(destinationFile);
    verify(archiverMock, times(1)).createArchive();
  }

  @Test
  public void createPackageAttachMuleSources() throws IOException {
    boolean onlyMuleSources = false;
    boolean lightweightPackage = false;
    boolean attachMuleSources = true;
    boolean testPackage = false;


    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.createPackage(fakeTargetFolder.toPath(), destinationFile.toPath());

    Path targetPath = fakeTargetFolder.toPath();
    verify(archiverMock, times(1)).addToRoot(targetPath.resolve(CLASSES.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile(),
                                                   null, null);
    verify(archiverMock, times(1)).addMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile(), null,
                                              null);
    verify(archiverMock, times(1)).addRepository(targetPath.resolve(REPOSITORY.value()).toFile(), null, null);
    verify(archiverMock, times(0)).addToRoot(targetPath.resolve(TEST_MULE.value()).toFile(), null, null);
    verify(archiverMock, times(0)).addToRoot(targetPath.resolve(TEST_CLASSES.value()).toFile(), null, null);

    verify(archiverMock, times(1)).setDestFile(destinationFile);
    verify(archiverMock, times(1)).createArchive();
  }

  @Test
  public void createPackageTestPackage() throws IOException {
    boolean onlyMuleSources = false;
    boolean lightweightPackage = false;
    boolean attachMuleSources = true;
    boolean testPackage = true;


    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.createPackage(fakeTargetFolder.toPath(), destinationFile.toPath());

    Path targetPath = fakeTargetFolder.toPath();
    verify(archiverMock, times(1)).addToRoot(targetPath.resolve(CLASSES.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile(),
                                                   null, null);
    verify(archiverMock, times(1)).addMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile(), null,
                                              null);
    verify(archiverMock, times(1)).addRepository(targetPath.resolve(REPOSITORY.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addToRoot(targetPath.resolve(TEST_MULE.value()).resolve(MUNIT.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addToRoot(targetPath.resolve(TEST_CLASSES.value()).toFile(), null, null);

    verify(archiverMock, times(1)).setDestFile(destinationFile);
    verify(archiverMock, times(1)).createArchive();
  }

  @Test
  public void createPackage() throws IOException {
    boolean onlyMuleSources = false;
    boolean lightweightPackage = false;
    boolean attachMuleSources = true;
    boolean testPackage = false;

    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.createPackage(fakeTargetFolder.toPath(), destinationFile.toPath());

    Path targetPath = fakeTargetFolder.toPath();
    verify(archiverMock, times(1)).addToRoot(targetPath.resolve(CLASSES.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile(),
                                                   null, null);
    verify(archiverMock, times(1)).addMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile(), null,
                                              null);
    verify(archiverMock, times(1)).addRepository(targetPath.resolve(REPOSITORY.value()).toFile(), null, null);
    verify(archiverMock, times(0)).addToRoot(targetPath.resolve(TEST_MULE.value()).toFile(), null, null);
    verify(archiverMock, times(0)).addToRoot(targetPath.resolve(TEST_CLASSES.value()).toFile(), null, null);

    verify(archiverMock, times(1)).setDestFile(destinationFile);
    verify(archiverMock, times(1)).createArchive();
  }

  @Test
  public void wiredCreatePackageNoClasses() {
    assertThrows(IllegalStateException.class, () -> {
      boolean onlyMuleSources = false;
      boolean lightweightPackage = false;
      boolean attachMuleSources = true;
      boolean testPackage = false;

      Path targetPath = fakeTargetFolder.toPath();

      builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

      builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());
      builder.withMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile());
      builder.withMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile());
      builder.withRepository(targetPath.resolve(REPOSITORY.value()).toFile());

      builder.createPackage(destinationFile.toPath());
    });
  }

  @Test
  public void wiredCreatePackageNoMaven() {
    assertThrows(IllegalStateException.class, () -> {
      boolean onlyMuleSources = false;
      boolean lightweightPackage = false;
      boolean attachMuleSources = true;
      boolean testPackage = false;

      Path targetPath = fakeTargetFolder.toPath();

      builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

      builder.withClasses(targetPath.resolve(CLASSES.value()).toFile());

      builder.withMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile());
      builder.withMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile());
      builder.withRepository(targetPath.resolve(REPOSITORY.value()).toFile());

      builder.createPackage(destinationFile.toPath());
    });
  }

  @Test
  public void wiredCreatePackageNMuleArtifact() {
    assertThrows(IllegalStateException.class, () -> {
      boolean onlyMuleSources = false;
      boolean lightweightPackage = false;
      boolean attachMuleSources = true;
      boolean testPackage = false;

      Path targetPath = fakeTargetFolder.toPath();

      builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

      builder.withClasses(targetPath.resolve(CLASSES.value()).toFile());
      builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());
      builder.withMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile());
      builder.withRepository(targetPath.resolve(REPOSITORY.value()).toFile());

      builder.createPackage(destinationFile.toPath());
    });
  }

  @Test
  public void wiredCreatePackageNoMuleSources() {
    assertThrows(IllegalStateException.class, () -> {
      boolean onlyMuleSources = false;
      boolean lightweightPackage = false;
      boolean attachMuleSources = true;
      boolean testPackage = false;

      Path targetPath = fakeTargetFolder.toPath();

      builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

      builder.withClasses(targetPath.resolve(CLASSES.value()).toFile());
      builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());
      builder.withMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile());
      builder.withRepository(targetPath.resolve(REPOSITORY.value()).toFile());

      builder.createPackage(destinationFile.toPath());
    });
  }

  @Test
  public void wiredCreatePackageNoRepository() {
    assertThrows(IllegalStateException.class, () -> {
      boolean onlyMuleSources = false;
      boolean lightweightPackage = false;
      boolean attachMuleSources = true;
      boolean testPackage = false;

      Path targetPath = fakeTargetFolder.toPath();

      builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

      builder.withClasses(targetPath.resolve(CLASSES.value()).toFile());
      builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());
      builder.withMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile());
      builder.withMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile());

      builder.createPackage(destinationFile.toPath());
    });
  }

  @Test
  public void wiredCreatePackageNoRepositoryLightWeightPackage() throws IOException {
    boolean onlyMuleSources = false;
    boolean lightweightPackage = true;
    boolean attachMuleSources = true;
    boolean testPackage = false;

    Path targetPath = fakeTargetFolder.toPath();

    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.withClasses(targetPath.resolve(CLASSES.value()).toFile());
    builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());
    builder.withMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile());
    builder.withMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile());

    builder.createPackage(destinationFile.toPath());
  }

  @Test
  public void wiredCreatePackageNoTestClassesTestPackage() {
    assertThrows(IllegalStateException.class, () -> {
      boolean onlyMuleSources = false;
      boolean lightweightPackage = false;
      boolean attachMuleSources = true;
      boolean testPackage = true;

      Path targetPath = fakeTargetFolder.toPath();

      builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

      builder.withClasses(targetPath.resolve(CLASSES.value()).toFile());

      builder.withTestMule(targetPath.resolve(TEST_MULE.value()).toFile());

      builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());
      builder.withMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile());
      builder.withMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile());
      builder.withRepository(targetPath.resolve(REPOSITORY.value()).toFile());

      builder.createPackage(destinationFile.toPath());
    });
  }

  @Test
  public void wiredCreatePackageNoTestMuleTestPackage() {
    assertThrows(IllegalStateException.class, () -> {
      boolean onlyMuleSources = false;
      boolean lightweightPackage = false;
      boolean attachMuleSources = true;
      boolean testPackage = true;

      Path targetPath = fakeTargetFolder.toPath();

      builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

      builder.withClasses(targetPath.resolve(CLASSES.value()).toFile());

      builder.withTestClasses(targetPath.resolve(TEST_CLASSES.value()).toFile());

      builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());
      builder.withMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile());
      builder.withMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile());
      builder.withRepository(targetPath.resolve(REPOSITORY.value()).toFile());

      builder.createPackage(destinationFile.toPath());
    });
  }

  @Test
  public void wiredCreatePackageTestPackage() throws IOException {
    boolean onlyMuleSources = false;
    boolean lightweightPackage = false;
    boolean attachMuleSources = true;
    boolean testPackage = true;

    Path targetPath = fakeTargetFolder.toPath();

    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.withClasses(targetPath.resolve(CLASSES.value()).toFile());
    builder.withTestClasses(targetPath.resolve(TEST_CLASSES.value()).toFile());
    builder.withTestMule(targetPath.resolve(TEST_MULE.value()).toFile());
    builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());
    builder.withMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile());
    builder.withMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile());
    builder.withRepository(targetPath.resolve(REPOSITORY.value()).toFile());

    builder.createPackage(destinationFile.toPath());

    verify(archiverMock, times(1)).addToRoot(targetPath.resolve(CLASSES.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile(),
                                                   null, null);
    verify(archiverMock, times(1)).addMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile(), null,
                                              null);
    verify(archiverMock, times(1)).addRepository(targetPath.resolve(REPOSITORY.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addToRoot(targetPath.resolve(TEST_MULE.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addToRoot(targetPath.resolve(TEST_CLASSES.value()).toFile(), null, null);

    verify(archiverMock, times(1)).setDestFile(destinationFile);
    verify(archiverMock, times(1)).createArchive();
  }

  @Test
  public void wiredCreatePackage() throws IOException {
    boolean onlyMuleSources = false;
    boolean lightweightPackage = false;
    boolean attachMuleSources = true;
    boolean testPackage = false;

    Path targetPath = fakeTargetFolder.toPath();

    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.withClasses(targetPath.resolve(CLASSES.value()).toFile());

    builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());
    builder.withMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile());
    builder.withMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile());

    builder.withRepository(targetPath.resolve(REPOSITORY.value()).toFile());

    builder.createPackage(destinationFile.toPath());

    verify(archiverMock, times(1)).addToRoot(targetPath.resolve(CLASSES.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile(),
                                                   null, null);
    verify(archiverMock, times(1)).addMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile(), null,
                                              null);
    verify(archiverMock, times(1)).addRepository(targetPath.resolve(REPOSITORY.value()).toFile(), null, null);
    verify(archiverMock, times(0)).addToRoot(targetPath.resolve(TEST_MULE.value()).toFile(), null, null);
    verify(archiverMock, times(0)).addToRoot(targetPath.resolve(TEST_CLASSES.value()).toFile(), null, null);

    verify(archiverMock, times(1)).setDestFile(destinationFile);
    verify(archiverMock, times(1)).createArchive();
  }


}

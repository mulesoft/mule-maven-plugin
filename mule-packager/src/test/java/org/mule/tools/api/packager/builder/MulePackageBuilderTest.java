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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.mule.tools.api.packager.MuleProjectFoldersGenerator;
import org.mule.tools.api.packager.archiver.MuleArchiver;
import org.mule.tools.api.packager.packaging.PackagingOptions;
import org.mule.tools.api.packager.packaging.PackagingType;

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
    assertThatThrownBy(() -> this.builder.withClasses(null))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The folder must not be null");
  }

  @Test
  public void setNonExistentClassesFolder() {
    assertThatThrownBy(() -> builder.withClasses(new File("fake")))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The folder must exists");
  }

  @Test
  public void setNullTestClassesFolder() {
    assertThatThrownBy(() -> this.builder.withTestClasses(null))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The folder must not be null");
  }

  @Test
  public void setNonExistentTestClassesFolder() {
    assertThatThrownBy(() -> this.builder.withTestClasses(new File("fake")))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The folder must exists");
  }

  @Test
  public void setNullTestMuleFolder() {
    assertThatThrownBy(() -> this.builder.withTestMule(null))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The folder must not be null");
  }

  @Test
  public void setNonExistentTestMuleFolder() {
    assertThatThrownBy(() -> this.builder.withTestMule(new File("fake")))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The folder must exists");
  }

  @Test
  public void setNullMavenFolder() {
    assertThatThrownBy(() -> this.builder.withMaven(null))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The folder must not be null");
  }

  @Test
  public void setNonExistentMavenFolder() {
    assertThatThrownBy(() -> this.builder.withMaven(new File("fake")))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The folder must exists");
  }

  @Test
  public void setNullMuleSrcFolder() {
    assertThatThrownBy(() -> this.builder.withMuleSrc(null))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The folder must not be null");
  }

  @Test
  public void setNonExistentMuleSrcFolder() {
    assertThatThrownBy(() -> this.builder.withMuleSrc(new File("fake")))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The folder must exists");
  }

  @Test
  public void setNullMuleArtifactFolderTest() {
    assertThatThrownBy(() -> this.builder.withMuleArtifact(null))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The folder must not be null");
  }

  @Test
  public void setNonExistentMuleArtifactFolderTest() {
    assertThatThrownBy(() -> this.builder.withMuleArtifact(new File("fake")))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The folder must exists");
  }

  @Test
  public void setNullRepositoryFolder() {
    assertThatThrownBy(() -> this.builder.withRepository(null))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The folder must not be null");
  }

  @Test
  public void setNonExistentRepositoryFolder() {
    assertThatThrownBy(() -> this.builder.withRepository(new File("fake")))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The folder must exists");
  }

  @Test
  public void setNullRootResourceFile() {
    assertThatThrownBy(() -> this.builder.withRootResource(null))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The resource must not be null");
  }

  @Test
  public void setNonExistentRootResourceFile() {
    assertThatThrownBy(() -> this.builder.withRootResource(new File("fake")))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("The resource must exists");
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
    assertThatThrownBy(() -> this.builder.withPackagingOptions(null))
            .isExactlyInstanceOf(IllegalArgumentException.class);

  }

  @Test
  public void setNullArchiver() {
    assertThatThrownBy(() -> this.builder.withArchiver(null))
            .isExactlyInstanceOf(IllegalArgumentException.class);
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
    assertThatThrownBy(() -> builder.createPackage(null, destinationFile.toPath()))
            .isExactlyInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void createPackageNonExistingOriginFolderPath() {
    assertThatThrownBy(() -> builder.createPackage(new File("fake").toPath(), destinationFile.toPath()))
            .isExactlyInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void createPackageNullDestinationPath() {
    assertThatThrownBy(() -> {
      boolean onlyMuleSources = true;
      boolean lightweightPackage = false;
      boolean attachMuleSources = false;
      boolean testPackage = false;

      builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

      builder.createPackage(fakeTargetFolder.toPath(), null);
    }).isExactlyInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void createPackageAlreadyExistingDestinationPath() {
    assertThatThrownBy(() -> {
      boolean onlyMuleSources = true;
      boolean lightweightPackage = false;
      boolean attachMuleSources = false;
      boolean testPackage = false;

      builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

      destinationFile.createNewFile();
      builder.createPackage(fakeTargetFolder.toPath(), destinationFile.toPath());
    }).isExactlyInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void createPackageNoPackagingOptionsProvided() {
    assertThatThrownBy(() -> builder.createPackage(fakeTargetFolder.toPath(), destinationFile.toPath()))
            .isExactlyInstanceOf(IllegalStateException.class);
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
    assertThatThrownBy(() -> {
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
    }).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  public void wiredCreatePackageNoMaven() {
    assertThatThrownBy(() -> {
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
    }).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  public void wiredCreatePackageNMuleArtifact() {
    assertThatThrownBy(() -> {
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
    }).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  public void wiredCreatePackageNoMuleSources() {
    assertThatThrownBy(() -> {
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
    }).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  public void wiredCreatePackageNoRepository() {
    assertThatThrownBy(() -> {
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
    }).isExactlyInstanceOf(IllegalStateException.class);
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
    assertThatThrownBy(() -> {
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
    }).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  public void wiredCreatePackageNoTestMuleTestPackage() {
    assertThatThrownBy(() -> {
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
    }).isExactlyInstanceOf(IllegalStateException.class);
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

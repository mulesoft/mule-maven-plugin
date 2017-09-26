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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import org.mule.tools.api.packager.MuleProjectFoldersGenerator;
import org.mule.tools.api.packager.archiver.MuleArchiver;
import org.mule.tools.api.packager.packaging.PackagingOptions;
import org.mule.tools.api.packager.packaging.PackagingType;

@RunWith(MockitoJUnitRunner.class)
public class MulePackageBuilderTest {

  private static final String GROUP_ID = "com.fake.group";
  private static final String ARTIFACT_ID = "fake-id";
  private static final PackagingType PACKAGING_TYPE = PackagingType.MULE_APPLICATION;

  private MuleArchiver archiverMock;

  private File destinationFile;

  private MulePackageBuilder builder;

  @Rule
  public TemporaryFolder fakeTargetFolder = new TemporaryFolder();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() throws IOException {
    archiverMock = mock(MuleArchiver.class);

    new MuleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PACKAGING_TYPE).generate(fakeTargetFolder.getRoot().toPath());

    destinationFile = new File(fakeTargetFolder.getRoot(), "destination.jar");

    builder = new MulePackageBuilder();
    builder.withArchiver(archiverMock);

    if (destinationFile.exists()) {
      destinationFile.delete();
    }
  }

  @Test
  public void setNullClassesFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must not be null");
    this.builder.withClasses(null);
  }

  @Test
  public void setNonExistentClassesFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must exists");
    builder.withClasses(new File("fake"));
  }

  @Test
  public void setNullTestClassesFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must not be null");
    this.builder.withTestClasses(null);
  }

  @Test
  public void setNonExistentTestClassesFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must exists");
    this.builder.withTestClasses(new File("fake"));
  }

  @Test
  public void setNullTestMuleFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must not be null");
    this.builder.withTestMule(null);
  }

  @Test
  public void setNonExistentTestMuleFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must exists");
    this.builder.withTestMule(new File("fake"));
  }

  @Test
  public void setNullMavenFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must not be null");
    this.builder.withMaven(null);
  }

  @Test
  public void setNonExistentMavenFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must exists");
    this.builder.withMaven(new File("fake"));
  }

  @Test
  public void setNullMuleSrcFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must not be null");
    this.builder.withMuleSrc(null);
  }

  @Test
  public void setNonExistentMuleSrcFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must exists");
    this.builder.withMuleSrc(new File("fake"));
  }

  @Test
  public void setNullMuleArtifactFolderTest() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must not be null");
    this.builder.withMuleArtifact(null);
  }

  @Test
  public void setNonExistentMuleArtifactFolderTest() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must exists");
    this.builder.withMuleArtifact(new File("fake"));
  }

  @Test
  public void setNullRepositoryFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must not be null");
    this.builder.withRepository(null);
  }

  @Test
  public void setNonExistentRepositoryFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must exists");
    this.builder.withRepository(new File("fake"));
  }

  @Test
  public void setNullRootResourceFile() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The resource must not be null");
    this.builder.withRootResource(null);
  }

  @Test
  public void setNonExistentRootResourceFile() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The resource must exists");
    this.builder.withRootResource(new File("fake"));
  }

  @Test
  public void addRootResources() throws NoSuchFieldException, IllegalAccessException {
    List<File> actualRootResourcesList = builder.rootResources;
    assertTrue("The list of root resources should be empty", actualRootResourcesList.isEmpty());

    File rootResourceMock = mock(File.class);
    when(rootResourceMock.exists()).thenReturn(true);
    builder.withRootResource(rootResourceMock);
    assertEquals("The list of root resources should contain one element", 1, actualRootResourcesList.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void setNullPackagingOptions() {
    this.builder.withPackagingOptions(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setNullArchiver() {
    this.builder.withArchiver(null);
  }

  @Test
  public void setArchiver() {
    assertThat("Default archiver type is wrong", builder.getArchiver(), instanceOf(MuleArchiver.class));

    class MuleArchiverSubclass extends MuleArchiver {
    }
    builder.withArchiver(new MuleArchiverSubclass());
    assertThat("archiver type is wrong", builder.getArchiver(), instanceOf(MuleArchiverSubclass.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createPackageNullOriginFolderPath() throws IOException {
    builder.createPackage(null, destinationFile.toPath());
  }

  @Test(expected = IllegalArgumentException.class)
  public void createPackageNonExistingOriginFolderPath() throws IOException {
    builder.createPackage(new File("fake").toPath(), destinationFile.toPath());
  }

  @Test(expected = IllegalArgumentException.class)
  public void createPackageNullDestinationPath() throws IOException {
    boolean onlyMuleSources = true;
    boolean lightweightPackage = false;
    boolean attachMuleSources = false;
    boolean testPackage = false;

    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.createPackage(fakeTargetFolder.getRoot().toPath(), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createPackageAlreadyExistingDestinationPath() throws IOException {
    boolean onlyMuleSources = true;
    boolean lightweightPackage = false;
    boolean attachMuleSources = false;
    boolean testPackage = false;

    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    destinationFile.createNewFile();
    builder.createPackage(fakeTargetFolder.getRoot().toPath(), destinationFile.toPath());
  }

  @Test(expected = IllegalStateException.class)
  public void createPackageNoPackagingOptionsProvided() throws IOException {
    builder.createPackage(fakeTargetFolder.getRoot().toPath(), destinationFile.toPath());
  }

  @Test
  public void createPackageOnlyMuleSources() throws IOException {
    boolean onlyMuleSources = true;
    boolean lightweightPackage = false;
    boolean attachMuleSources = false;
    boolean testPackage = false;

    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.createPackage(fakeTargetFolder.getRoot().toPath(), destinationFile.toPath());

    Path targetPath = fakeTargetFolder.getRoot().toPath();
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

    builder.createPackage(fakeTargetFolder.getRoot().toPath(), destinationFile.toPath());

    Path targetPath = fakeTargetFolder.getRoot().toPath();
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

    builder.createPackage(fakeTargetFolder.getRoot().toPath(), destinationFile.toPath());

    Path targetPath = fakeTargetFolder.getRoot().toPath();
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

    builder.createPackage(fakeTargetFolder.getRoot().toPath(), destinationFile.toPath());

    Path targetPath = fakeTargetFolder.getRoot().toPath();
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

    builder.createPackage(fakeTargetFolder.getRoot().toPath(), destinationFile.toPath());

    Path targetPath = fakeTargetFolder.getRoot().toPath();
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

  @Test(expected = IllegalStateException.class)
  public void wiredCreatePackageNoClasses() throws IOException {
    boolean onlyMuleSources = false;
    boolean lightweightPackage = false;
    boolean attachMuleSources = true;
    boolean testPackage = false;

    Path targetPath = fakeTargetFolder.getRoot().toPath();

    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());
    builder.withMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile());
    builder.withMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile());
    builder.withRepository(targetPath.resolve(REPOSITORY.value()).toFile());

    builder.createPackage(destinationFile.toPath());
  }

  @Test(expected = IllegalStateException.class)
  public void wiredCreatePackageNoMaven() throws IOException {
    boolean onlyMuleSources = false;
    boolean lightweightPackage = false;
    boolean attachMuleSources = true;
    boolean testPackage = false;

    Path targetPath = fakeTargetFolder.getRoot().toPath();

    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.withClasses(targetPath.resolve(CLASSES.value()).toFile());

    builder.withMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile());
    builder.withMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile());
    builder.withRepository(targetPath.resolve(REPOSITORY.value()).toFile());

    builder.createPackage(destinationFile.toPath());
  }

  @Test(expected = IllegalStateException.class)
  public void wiredCreatePackageNMuleArtifact() throws IOException {
    boolean onlyMuleSources = false;
    boolean lightweightPackage = false;
    boolean attachMuleSources = true;
    boolean testPackage = false;

    Path targetPath = fakeTargetFolder.getRoot().toPath();

    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.withClasses(targetPath.resolve(CLASSES.value()).toFile());
    builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());
    builder.withMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile());
    builder.withRepository(targetPath.resolve(REPOSITORY.value()).toFile());

    builder.createPackage(destinationFile.toPath());
  }

  @Test(expected = IllegalStateException.class)
  public void wiredCreatePackageNoMuleSources() throws IOException {
    boolean onlyMuleSources = false;
    boolean lightweightPackage = false;
    boolean attachMuleSources = true;
    boolean testPackage = false;

    Path targetPath = fakeTargetFolder.getRoot().toPath();

    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.withClasses(targetPath.resolve(CLASSES.value()).toFile());
    builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());
    builder.withMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile());
    builder.withRepository(targetPath.resolve(REPOSITORY.value()).toFile());

    builder.createPackage(destinationFile.toPath());
  }

  @Test(expected = IllegalStateException.class)
  public void wiredCreatePackageNoRepository() throws IOException {
    boolean onlyMuleSources = false;
    boolean lightweightPackage = false;
    boolean attachMuleSources = true;
    boolean testPackage = false;

    Path targetPath = fakeTargetFolder.getRoot().toPath();

    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.withClasses(targetPath.resolve(CLASSES.value()).toFile());
    builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());
    builder.withMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile());
    builder.withMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile());

    builder.createPackage(destinationFile.toPath());
  }

  @Test
  public void wiredCreatePackageNoRepositoryLightWeightPackage() throws IOException {
    boolean onlyMuleSources = false;
    boolean lightweightPackage = true;
    boolean attachMuleSources = true;
    boolean testPackage = false;

    Path targetPath = fakeTargetFolder.getRoot().toPath();

    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.withClasses(targetPath.resolve(CLASSES.value()).toFile());
    builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());
    builder.withMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile());
    builder.withMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile());

    builder.createPackage(destinationFile.toPath());
  }

  @Test(expected = IllegalStateException.class)
  public void wiredCreatePackageNoTestClassesTestPackage() throws IOException {
    boolean onlyMuleSources = false;
    boolean lightweightPackage = false;
    boolean attachMuleSources = true;
    boolean testPackage = true;

    Path targetPath = fakeTargetFolder.getRoot().toPath();

    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.withClasses(targetPath.resolve(CLASSES.value()).toFile());

    builder.withTestMule(targetPath.resolve(TEST_MULE.value()).toFile());

    builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());
    builder.withMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile());
    builder.withMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile());
    builder.withRepository(targetPath.resolve(REPOSITORY.value()).toFile());

    builder.createPackage(destinationFile.toPath());
  }

  @Test(expected = IllegalStateException.class)
  public void wiredCreatePackageNoTestMuleTestPackage() throws IOException {
    boolean onlyMuleSources = false;
    boolean lightweightPackage = false;
    boolean attachMuleSources = true;
    boolean testPackage = true;

    Path targetPath = fakeTargetFolder.getRoot().toPath();

    builder.withPackagingOptions(new PackagingOptions(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage));

    builder.withClasses(targetPath.resolve(CLASSES.value()).toFile());

    builder.withTestClasses(targetPath.resolve(TEST_CLASSES.value()).toFile());

    builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());
    builder.withMuleArtifact(targetPath.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile());
    builder.withMuleSrc(targetPath.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile());
    builder.withRepository(targetPath.resolve(REPOSITORY.value()).toFile());

    builder.createPackage(destinationFile.toPath());
  }

  @Test
  public void wiredCreatePackageTestPackage() throws IOException {
    boolean onlyMuleSources = false;
    boolean lightweightPackage = false;
    boolean attachMuleSources = true;
    boolean testPackage = true;

    Path targetPath = fakeTargetFolder.getRoot().toPath();

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

    Path targetPath = fakeTargetFolder.getRoot().toPath();

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

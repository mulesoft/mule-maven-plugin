/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
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
import static org.mule.tools.api.packager.structure.FolderNames.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import org.mule.tools.api.packager.MuleProjectFoldersGenerator;
import org.mule.tools.api.packager.archiver.MuleArchiver;
import org.mule.tools.api.packager.packaging.PackagingOptions;
import org.mule.tools.api.packager.packaging.PackagingType;

public class MulePackageBuilderTest {

  private static final String GROUP_ID = "com.fake.group";
  private static final String ARTIFACT_ID = "fake-id";
  private static final PackagingType PACKAGING_TYPE = PackagingType.MULE;

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
  public void setNullRootResourceFile() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The resource must not be null");
    this.builder.withRootResource(null);
  }

  @Test
  public void setNonExistentRootResourceFile() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The resource must exist");
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
    builder.createPackage(fakeTargetFolder.getRoot().toPath(), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createPackageAlreadyExistingDestinationPath() throws IOException {
    destinationFile.createNewFile();
    builder.createPackage(fakeTargetFolder.getRoot().toPath(), destinationFile.toPath());
  }

  @Test
  public void createPackage() throws IOException {
    builder.createPackage(fakeTargetFolder.getRoot().toPath(), destinationFile.toPath());

    Path targetPath = fakeTargetFolder.getRoot().toPath();
    verify(archiverMock, times(1)).addToRoot(targetPath.resolve(MULE.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addApi(targetPath.resolve(API.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addClasses(targetPath.resolve(CLASSES.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addWsdl(targetPath.resolve(WSDL.value()).toFile(), null,
                                           null);
    verify(archiverMock, times(1)).addLib(targetPath.resolve(LIB.value()).toFile(), null,
                                          null);
    verify(archiverMock, times(1)).addMappings(targetPath.resolve(MAPPINGS.value()).toFile(), null,
                                               null);
    verify(archiverMock, times(0)).addToRoot(targetPath.resolve(TEST_MULE.value()).toFile(), null, null);
    verify(archiverMock, times(0)).addToRoot(targetPath.resolve(TEST_CLASSES.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addMetaInf(targetPath.resolve(META_INF.value()).toFile(), null, null);

    verify(archiverMock, times(1)).setDestFile(destinationFile);
    verify(archiverMock, times(1)).createArchive();
  }

}

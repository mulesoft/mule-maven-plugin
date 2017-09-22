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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.packager.structure.FolderNames.APPLICATIONS;
import static org.mule.tools.api.packager.structure.FolderNames.DOMAIN;
import static org.mule.tools.api.packager.structure.FolderNames.MAVEN;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import org.mule.tools.api.packager.DomainBundleProjectFoldersGenerator;
import org.mule.tools.api.packager.archiver.DomainBundleArchiver;
import org.mule.tools.api.packager.packaging.PackagingType;

public class DomainBundlePackageBuilderTest {

  private static final String GROUP_ID = "com.fake.group";
  private static final String ARTIFACT_ID = "fake-id";
  private static final PackagingType PACKAGING_TYPE = PackagingType.MULE_DOMAIN_BUNDLE;


  @Rule
  public TemporaryFolder fakeTargetFolder = new TemporaryFolder();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private DomainBundleArchiver archiverMock;

  private File destinationFile;

  private DomainBundlePackageBuilder builder;

  @Before
  public void setUp() throws IOException {
    archiverMock = mock(DomainBundleArchiver.class);

    fakeTargetFolder.create();
    destinationFile = new File(fakeTargetFolder.getRoot(), "destinationFile.jar");

    new DomainBundleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PACKAGING_TYPE).generate(fakeTargetFolder.getRoot().toPath());

    builder = new DomainBundlePackageBuilder();
    builder.withArchiver(archiverMock);
  }

  @Test
  public void setNullArchiver() {
    expectedException.expect(IllegalArgumentException.class);
    this.builder.withArchiver(null);
  }

  @Test
  public void setArchiver() {
    assertThat("Default archiver type is wrong", builder.getArchiver(), instanceOf(DomainBundleArchiver.class));

    class DomainBundleArchiverSubclass extends DomainBundleArchiver {
    }
    builder.withArchiver(new DomainBundleArchiverSubclass());
    assertThat("archiver type is wrong", builder.getArchiver(), instanceOf(DomainBundleArchiverSubclass.class));
  }

  @Test
  public void setNullMavenFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must not be null");
    builder.withMaven(null);
  }

  @Test
  public void setNonExistentMavenFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must exist");
    builder.withMaven(new File("fake"));
  }

  @Test
  public void setNullDomainFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must not be null");
    builder.withDomain(null);
  }

  @Test
  public void setNonExistentDomainFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must exist");
    builder.withDomain(new File("fake"));
  }

  @Test
  public void setNullApplicationsFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must not be null");
    builder.withApplications(null);
  }

  @Test
  public void setNonExistentApplicationsFolder() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must exist");
    builder.withApplications(new File("fake"));
  }

  @Test
  public void createPackageNullOriginalFolderPath() throws IOException {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The origin path must not be null");
    builder.createPackage(null, destinationFile.toPath());
  }

  @Test
  public void createPackageNonExistentOriginalFolderPath() throws IOException {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The origin path must exist");

    File fileMock = mock(File.class);
    when(fileMock.exists()).thenReturn(false);
    builder.createPackage(new File("fake").toPath(), destinationFile.toPath());
  }

  @Test
  public void createPackageNullDestinationPath() throws IOException {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The destination path must not be null");
    builder.createPackage(fakeTargetFolder.getRoot().toPath(), null);
  }

  @Test
  public void createPackageAlreadyExistentNullDestinationPath() throws IOException {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The destination file must not be duplicated");

    destinationFile.createNewFile();
    builder.createPackage(fakeTargetFolder.getRoot().toPath(), destinationFile.toPath());
  }

  @Test
  public void createPackage() throws IOException {
    builder.createPackage(fakeTargetFolder.getRoot().toPath(), destinationFile.toPath());

    Path targetPath = fakeTargetFolder.getRoot().toPath();
    verify(archiverMock, times(1)).addDomain(targetPath.resolve(DOMAIN.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addApplications(targetPath.resolve(APPLICATIONS.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile(), null, null);

    verify(archiverMock, times(1)).setDestFile(destinationFile);
    verify(archiverMock, times(1)).createArchive();
  }

  @Test
  public void wiredCreatePackageNoDomain() throws IOException {
    expectedException.expect(IllegalStateException.class);

    Path targetPath = fakeTargetFolder.getRoot().toPath();

    builder.withApplications(targetPath.resolve(APPLICATIONS.value()).toFile());
    builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());

    builder.createPackage(destinationFile.toPath());
  }

  @Test
  public void wiredCreatePackageNoApplications() throws IOException {
    expectedException.expect(IllegalStateException.class);

    Path targetPath = fakeTargetFolder.getRoot().toPath();

    builder.withDomain(targetPath.resolve(DOMAIN.value()).toFile());
    builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());

    builder.createPackage(destinationFile.toPath());
  }

  @Test
  public void wiredCreatePackageNoMaven() throws IOException {
    expectedException.expect(IllegalStateException.class);

    Path targetPath = fakeTargetFolder.getRoot().toPath();

    builder.withDomain(targetPath.resolve(DOMAIN.value()).toFile());
    builder.withApplications(targetPath.resolve(APPLICATIONS.value()).toFile());

    builder.createPackage(destinationFile.toPath());
  }

  @Test
  public void wiredCreatePackageNullDestinationPath() throws IOException {
    expectedException.expect(IllegalArgumentException.class);

    Path targetPath = fakeTargetFolder.getRoot().toPath();

    builder.withDomain(targetPath.resolve(DOMAIN.value()).toFile());
    builder.withApplications(targetPath.resolve(APPLICATIONS.value()).toFile());
    builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());

    builder.createPackage(null);
  }

  @Test
  public void wiredCreatePackageAlreadyExistentDestinationPath() throws IOException {
    expectedException.expect(IllegalArgumentException.class);

    Path targetPath = fakeTargetFolder.getRoot().toPath();

    builder.withDomain(targetPath.resolve(DOMAIN.value()).toFile());
    builder.withApplications(targetPath.resolve(APPLICATIONS.value()).toFile());
    builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());

    destinationFile.createNewFile();
    builder.createPackage(destinationFile.toPath());
  }

  @Test
  public void wiredCreatePackage() throws IOException {
    Path targetPath = fakeTargetFolder.getRoot().toPath();

    builder.withDomain(targetPath.resolve(DOMAIN.value()).toFile());
    builder.withApplications(targetPath.resolve(APPLICATIONS.value()).toFile());
    builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());

    builder.createPackage(destinationFile.toPath());

    verify(archiverMock, times(1)).addDomain(targetPath.resolve(DOMAIN.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addApplications(targetPath.resolve(APPLICATIONS.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile(), null, null);

    verify(archiverMock, times(1)).setDestFile(destinationFile);
    verify(archiverMock, times(1)).createArchive();
  }
}

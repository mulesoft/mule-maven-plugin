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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.api.packager.DomainBundleProjectFoldersGenerator;
import org.mule.tools.api.packager.archiver.DomainBundleArchiver;
import org.mule.tools.api.packager.packaging.PackagingType;

public class DomainBundlePackageBuilderTest {

  private static final String GROUP_ID = "com.fake.group";
  private static final String ARTIFACT_ID = "fake-id";
  private static final PackagingType PACKAGING_TYPE = PackagingType.MULE_DOMAIN_BUNDLE;

  @TempDir
  public File fakeTargetFolder;

  private DomainBundleArchiver archiverMock;

  private File destinationFile;

  private DomainBundlePackageBuilder builder;

  @BeforeEach
  public void setUp() throws IOException {
    archiverMock = mock(DomainBundleArchiver.class);

    fakeTargetFolder.toPath();
    destinationFile = new File(fakeTargetFolder.getPath(), "destinationFile.jar");

    new DomainBundleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PACKAGING_TYPE).generate(fakeTargetFolder.toPath());

    builder = new DomainBundlePackageBuilder();
    builder.withArchiver(archiverMock);
  }

  @Test
  public void setNullArchiver() {
    assertThrows(IllegalArgumentException.class, () -> {
      this.builder.withArchiver(null);
    });
  }

  @Test
  public void setArchiver() {
    assertThat(builder.getArchiver()).describedAs("Default archiver type is wrong").isInstanceOf(DomainBundleArchiver.class);

    class DomainBundleArchiverSubclass extends DomainBundleArchiver {
    }
    builder.withArchiver(new DomainBundleArchiverSubclass());
    assertThat(builder.getArchiver()).describedAs("archiver type is wrong").isInstanceOf(DomainBundleArchiverSubclass.class);
  }

  @Test
  public void setNullMavenFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> builder.withMaven(null));

    String expectedMessage = "The folder must not be null";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNonExistentMavenFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> builder.withMaven(new File("fake")));

    String expectedMessage = "The folder must exist";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNullDomainFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> builder.withDomain(null));

    String expectedMessage = "The folder must not be null";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNonExistentDomainFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> builder.withDomain(new File("fake")));

    String expectedMessage = "The folder must exist";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));

  }

  @Test
  public void setNullApplicationsFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> builder.withApplications(null));

    String expectedMessage = "The folder must not be null";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setNonExistentApplicationsFolder() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> builder.withApplications(new File("fake")));

    String expectedMessage = "The folder must exist";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void createPackageNullOriginalFolderPath() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> builder.createPackage(null, destinationFile.toPath()));

    String expectedMessage = "The origin path must not be null";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void createPackageNonExistentOriginalFolderPath() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      File fileMock = mock(File.class);
      when(fileMock.exists()).thenReturn(false);
      builder.createPackage(new File("fake").toPath(), destinationFile.toPath());
    });

    String expectedMessage = "The origin path must exist";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void createPackageNullDestinationPath() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> builder.createPackage(fakeTargetFolder.toPath(), null));

    String expectedMessage = "The destination path must not be null";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void createPackageAlreadyExistentNullDestinationPath() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      destinationFile.createNewFile();
      builder.createPackage(fakeTargetFolder.toPath(), destinationFile.toPath());
    });
    String expectedMessage = "The destination file must not be duplicated";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void createPackage() throws IOException {
    builder.createPackage(fakeTargetFolder.toPath(), destinationFile.toPath());

    Path targetPath = fakeTargetFolder.toPath();
    verify(archiverMock, times(1)).addDomain(targetPath.resolve(DOMAIN.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addApplications(targetPath.resolve(APPLICATIONS.value()).toFile(), null, null);
    verify(archiverMock, times(1)).addMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile(), null, null);

    verify(archiverMock, times(1)).setDestFile(destinationFile);
    verify(archiverMock, times(1)).createArchive();
  }

  @Test
  public void wiredCreatePackageNoDomain() {
    assertThrows(IllegalStateException.class, () -> {
      Path targetPath = fakeTargetFolder.toPath();

      builder.withApplications(targetPath.resolve(APPLICATIONS.value()).toFile());
      builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());

      builder.createPackage(destinationFile.toPath());
    });
  }

  @Test
  public void wiredCreatePackageNoApplications() {
    assertThrows(IllegalStateException.class, () -> {
      Path targetPath = fakeTargetFolder.toPath();

      builder.withDomain(targetPath.resolve(DOMAIN.value()).toFile());
      builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());

      builder.createPackage(destinationFile.toPath());
    });
  }

  @Test
  public void wiredCreatePackageNoMaven() {
    assertThrows(IllegalStateException.class, () -> {
      Path targetPath = fakeTargetFolder.toPath().toFile().toPath();

      builder.withDomain(targetPath.resolve(DOMAIN.value()).toFile());
      builder.withApplications(targetPath.resolve(APPLICATIONS.value()).toFile());

      builder.createPackage(destinationFile.toPath());
    });
  }

  @Test
  public void wiredCreatePackageNullDestinationPath() {
    assertThrows(IllegalArgumentException.class, () -> {
      Path targetPath = fakeTargetFolder.toPath();

      builder.withDomain(targetPath.resolve(DOMAIN.value()).toFile());
      builder.withApplications(targetPath.resolve(APPLICATIONS.value()).toFile());
      builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());

      builder.createPackage(null);
    });
  }

  @Test
  public void wiredCreatePackageAlreadyExistentDestinationPath() {
    assertThrows(IllegalArgumentException.class, () -> {
      Path targetPath = fakeTargetFolder.toPath();

      builder.withDomain(targetPath.resolve(DOMAIN.value()).toFile());
      builder.withApplications(targetPath.resolve(APPLICATIONS.value()).toFile());
      builder.withMaven(targetPath.resolve(META_INF.value()).resolve(MAVEN.value()).toFile());

      destinationFile.createNewFile();
      builder.createPackage(destinationFile.toPath());
    });
  }

  @Test
  public void wiredCreatePackage() throws IOException {
    Path targetPath = fakeTargetFolder.toPath();

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

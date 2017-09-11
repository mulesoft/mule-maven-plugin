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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.*;

public class DomainBundlePackageBuilderTest {

  private DomainBundlePackageBuilder builder;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public TemporaryFolder temporaryOriginFolder = new TemporaryFolder();
  @Rule
  public TemporaryFolder temporaryDestinationFileFolder = new TemporaryFolder();

  private File fileMock;
  private File destinationFile;

  @Before
  public void setUp() throws IOException {
    builder = new DomainBundlePackageBuilder();
    fileMock = mock(File.class);
    temporaryOriginFolder.create();
    temporaryDestinationFileFolder.create();
    destinationFile = new File(temporaryDestinationFileFolder.getRoot(), "destinationFile.jar");
  }

  @Test
  public void getDomainBundleArchiverTest() {
    assertThat("Archiver should not be null", builder.getDomainBundleArchiver(), notNullValue());
  }

  @Test
  public void withDestinationFileNullTest() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The file must not be null");
    builder.withDestinationFile(null);
  }

  @Test
  public void withDestinationFileThatAlreadyExistTest() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The file must not be duplicated");
    when(fileMock.exists()).thenReturn(true);
    builder.withDestinationFile(fileMock);
  }

  @Test
  public void withMavenNullTest() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must not be null");
    builder.withMaven(null);
  }

  @Test
  public void withDomainNullTest() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must not be null");
    builder.withDomain(null);
  }

  @Test
  public void withApplicationsNullTest() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The folder must not be null");
    builder.withApplications(null);
  }

  @Test
  public void createPackageTest() throws IOException {
    DomainBundlePackageBuilder builderSpy = spy(builder);

    doNothing().when(builderSpy).createDeployableFile();

    File applicationsFolder = temporaryOriginFolder.newFolder("applications");
    File domainFolder = temporaryOriginFolder.newFolder("domain");
    File metaInfFolder = temporaryOriginFolder.newFolder("META-INF");
    File mavenFolder = new File(metaInfFolder, "maven");
    mavenFolder.mkdir();

    builderSpy.createPackage(destinationFile, temporaryOriginFolder.getRoot().getAbsolutePath());

    verify(builderSpy, times(1)).withDestinationFile(destinationFile);
    verify(builderSpy, times(1)).withDomain(domainFolder);
    verify(builderSpy, times(1)).withApplications(applicationsFolder);
    verify(builderSpy, times(1)).withMaven(mavenFolder);
  }

  @Test
  public void createDeployableFileDestinationFileNullTest() throws IOException {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("The destination file has not been set");
    builder.createDeployableFile();
  }

  @Test
  public void createDeployableFileDomainFolderNullTest() throws IOException {
    DomainBundlePackageBuilder builderSpy = spy(builder);
    builderSpy.withDestinationFile(destinationFile);
    builderSpy.withApplications(temporaryOriginFolder.newFolder("applications"));
    builderSpy.createDeployableFile();
    verify(builderSpy, times(0)).withDomain(any());
  }

  @Test
  public void createDeployableFileApplicationsFolderNullTest() throws IOException {
    DomainBundlePackageBuilder builderSpy = spy(builder);
    builderSpy.withDestinationFile(destinationFile);
    builderSpy.withDomain(temporaryOriginFolder.newFolder("domain"));
    builderSpy.createDeployableFile();
    verify(builderSpy, times(0)).withApplications(any());
  }

  @Test
  public void createDeployableFileMavenFolderNullTest() throws IOException {
    DomainBundlePackageBuilder builderSpy = spy(builder);
    builderSpy.withDestinationFile(destinationFile);
    builderSpy.withDomain(temporaryOriginFolder.newFolder("application"));
    builderSpy.createDeployableFile();
    verify(builderSpy, times(0)).withMaven(any());
  }
}

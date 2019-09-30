/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.ClassLoaderModel;
import org.mule.tools.api.util.PackagerLog;

public class ArtifactInstallerTest {

  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String TYPE = "jar";
  private static final String CLASSIFIER = "classifier";
  private static final String FILE_NAME = "file";
  private static final String PREFIX_GROUP_ID = "group";
  private static final String POSFIX_GROUP_ID = "id";
  private static final String GROUP_ID = PREFIX_GROUP_ID + "." + POSFIX_GROUP_ID;
  private static final String ARTIFACT_FILE_NAME = "artifact-file";
  private static final String GENERATED_PACKAGE_NAME = "artifact-id-1.0.0-classifier.jar";
  private static final String OUTPUT_DIRECTORY =
      PREFIX_GROUP_ID + File.separator + POSFIX_GROUP_ID + File.separator + ARTIFACT_ID + File.separator + VERSION;
  private static final String POM_FILE_NAME = ARTIFACT_ID + "-" + VERSION + ".pom";
  private static final String DEFAULT_POM_FILE_NAME = "pom.xml";
  private PackagerLog logMock;
  private ArtifactInstaller installer;
  private Artifact artifact;

  @Rule
  public ExpectedException exception = ExpectedException.none();
  @Rule
  public TemporaryFolder outputFolder = new TemporaryFolder();
  @Rule
  public TemporaryFolder artifactFileFolder = new TemporaryFolder();
  private ClassLoaderModel classLoaderModel;

  @Before
  public void before() throws IOException {
    logMock = mock(PackagerLog.class);
    installer = new ArtifactInstaller(logMock);
    outputFolder.create();
    artifactFileFolder.create();
    ArtifactCoordinates coordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    artifact = new Artifact(coordinates, artifactFileFolder.getRoot().toURI());
    classLoaderModel = mock(ClassLoaderModel.class);
  }

  @Test
  public void installArtifactTest() throws IOException {
    ArtifactCoordinates coordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    artifact = new Artifact(coordinates, artifactFileFolder.newFile(ARTIFACT_FILE_NAME).toURI());
    artifactFileFolder.newFile(POM_FILE_NAME);
    File installedFile = new File(outputFolder.getRoot(), OUTPUT_DIRECTORY + File.separator + GENERATED_PACKAGE_NAME);
    File pomFile = new File(outputFolder.getRoot(), OUTPUT_DIRECTORY + File.separator + POM_FILE_NAME);

    assertThat("File should not be installed yet", !installedFile.exists());
    installer.installArtifact(outputFolder.getRoot(), artifact, Optional.empty());
    assertThat("File was not installed", installedFile.exists());
    assertThat("Pom file was not copied", pomFile.exists());
  }

  @Test
  public void installNullArtifactTest() throws IOException {
    exception.expect(IllegalArgumentException.class);
    installer.installArtifact(outputFolder.getRoot(), null, Optional.empty());
  }

  @Test
  public void installArtifactToReadOnlyDestinationTest() throws IOException {
    exception.expect(IOException.class);
    File destination = new File(outputFolder.getRoot(), FILE_NAME);
    destination.setReadOnly();
    artifact.setUri(destination.toURI());
    installer.installArtifact(outputFolder.getRoot(), artifact, Optional.empty());
  }

  @Test
  public void generateDependencyDescriptorFileWhenClassloaderIsPresentTest() throws IOException {
    ArtifactInstaller artifactInstallerSpy = spy(installer);
    doNothing().when(artifactInstallerSpy).generateClassloderModelFile(classLoaderModel, artifactFileFolder.getRoot(), false);

    artifactInstallerSpy.generateDependencyDescriptorFile(artifact, artifactFileFolder.getRoot(), Optional.of(classLoaderModel),
                                                          false);

    verify(artifactInstallerSpy, times(1)).generateClassloderModelFile(classLoaderModel, artifactFileFolder.getRoot(), false);
    verify(artifactInstallerSpy, times(0)).generatePomFile(any(), any());
  }

  @Test
  public void generatePomFileWhenPomFileNameDoesNotExistTest() throws IOException {
    artifact.setUri(artifactFileFolder.newFile(DEFAULT_POM_FILE_NAME).toURI());
    File generatedPomFile = new File(outputFolder.getRoot(), POM_FILE_NAME);

    assertThat("Pom file should not exist", generatedPomFile.exists(), is(false));

    installer.generatePomFile(artifact, outputFolder.getRoot());

    assertThat("Pom file should have been created", generatedPomFile.exists(), is(true));
  }

}

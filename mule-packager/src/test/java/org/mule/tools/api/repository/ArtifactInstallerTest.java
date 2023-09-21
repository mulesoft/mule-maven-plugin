/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.repository;


import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.api.muleclassloader.model.Artifact;
import org.mule.tools.api.muleclassloader.model.ArtifactCoordinates;
import org.mule.tools.api.muleclassloader.model.ClassLoaderModel;
import org.mule.tools.api.util.PackagerLog;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

  @TempDir
  public Path outputFolder;

  @TempDir
  public Path artifactFileFolder;

  private ClassLoaderModel classLoaderModel;

  @BeforeEach
  public void before() throws IOException {
    logMock = mock(PackagerLog.class);
    installer = new ArtifactInstaller(logMock);
    outputFolder.toFile();
    artifactFileFolder.toFile();
    ArtifactCoordinates coordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    artifact = new Artifact(coordinates, artifactFileFolder.toUri());
    classLoaderModel = mock(ClassLoaderModel.class);
  }

  @Test
  public void installArtifactTest() throws IOException {
    ArtifactCoordinates coordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    artifact = new Artifact(coordinates, Files.createFile(artifactFileFolder.resolve(ARTIFACT_FILE_NAME)).toUri());
    Files.createFile(artifactFileFolder.resolve(POM_FILE_NAME)).toFile();
    File installedFile =
        new File(outputFolder.toAbsolutePath().toFile(), OUTPUT_DIRECTORY + File.separator + GENERATED_PACKAGE_NAME);
    File pomFile = new File(outputFolder.toAbsolutePath().toFile(), OUTPUT_DIRECTORY + File.separator + POM_FILE_NAME);

    assertThat(!installedFile.exists()).describedAs("File should not be installed yet");
    installer.installArtifact(outputFolder.toAbsolutePath().toFile(), artifact, Optional.empty());
    assertThat(installedFile.exists()).describedAs("File was not installed");
    assertThat(pomFile.exists()).describedAs("Pom file was not copied");
  }

  @Test
  public void installNullArtifactTest() {
    assertThatThrownBy(() -> installer.installArtifact(outputFolder.toFile(), null, Optional.empty()))
        .isExactlyInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void installArtifactToReadOnlyDestinationTest() throws IOException {
    assertThatThrownBy(() -> {
      File destination = outputFolder.resolve(FILE_NAME).toFile();//new File(outputFolder.getRoot().toFile(), FILE_NAME);
      destination.setReadOnly();
      artifact.setUri(destination.toURI());
      installer.installArtifact(outputFolder.toFile(), artifact, Optional.empty());
    }).isExactlyInstanceOf(IOException.class);
  }

  @Test
  public void generateDependencyDescriptorFileWhenClassloaderIsPresentTest() throws IOException {
    ArtifactInstaller artifactInstallerSpy = spy(installer);
    doNothing().when(artifactInstallerSpy).generateClassloderModelFile(classLoaderModel, artifactFileFolder.toFile(), false);

    artifactInstallerSpy.generateDependencyDescriptorFile(artifact, artifactFileFolder.toFile(), Optional.of(classLoaderModel),
                                                          false);

    verify(artifactInstallerSpy, times(1)).generateClassloderModelFile(classLoaderModel, artifactFileFolder.toFile(), false);
    verify(artifactInstallerSpy, times(0)).generatePomFile(any(), any());
  }

  @Test
  public void generatePomFileWhenPomFileNameDoesNotExistTest() throws IOException {
    artifact.setUri(Files.createFile(artifactFileFolder.resolve(DEFAULT_POM_FILE_NAME)).toUri());
    File generatedPomFile = new File(outputFolder.toAbsolutePath().toString(), POM_FILE_NAME);

    assertThat(generatedPomFile.exists()).describedAs("Pom file should not exist").isFalse();

    installer.generatePomFile(artifact, outputFolder.toAbsolutePath().toFile());

    assertThat(generatedPomFile.exists()).describedAs("Pom file should have been created").isTrue();
  }

  @Test
  public void generatePomFileWhenPomFileDoesNotExistTest() throws IOException {
    File generatedPomFile = outputFolder.resolve(POM_FILE_NAME).toFile();//new File(outputFolder.getRoot().toFile(), POM_FILE_NAME);

    assertThat(generatedPomFile.exists()).describedAs("Pom file should not exist").isFalse();

    installer.generatePomFile(artifact, outputFolder.toFile());
  }

}

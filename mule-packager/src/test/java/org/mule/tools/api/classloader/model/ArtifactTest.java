/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.classloader.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.api.muleclassloader.model.Artifact;
import org.mule.tools.api.muleclassloader.model.ArtifactCoordinates;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

class ArtifactTest {

  private static final String RESOURCE_FULL_PATH = "User/lala/repository/aaa/bbb.jar";
  private static final URI EXPECTED_URI = URI.create("repository/aaa/bbb.jar");
  private Artifact artifact;
  private Artifact newArtifact;
  private File newArtifactFile;

  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String TYPE = "zip";
  private static final String CLASSIFIER = "classifier";
  private static final String PREFIX_GROUP_ID = "group";
  private static final String POSFIX_GROUP_ID = "id";
  private static final String GROUP_ID = PREFIX_GROUP_ID + "." + POSFIX_GROUP_ID;
  private static final String OUTPUT_DIRECTORY =
      PREFIX_GROUP_ID + File.separator + POSFIX_GROUP_ID + File.separator + ARTIFACT_ID + File.separator + VERSION;

  private ArtifactCoordinates artifactCoordinates;

  @BeforeEach
  void setUp() {
    artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    artifact = new Artifact(artifactCoordinates, URI.create(RESOURCE_FULL_PATH));
    newArtifact = new Artifact(artifactCoordinates, URI.create(RESOURCE_FULL_PATH));
  }

  @Test
  void setNewArtifactURIWindowsRelativePathTest() throws URISyntaxException {
    newArtifactFile = new File("repository\\aaa\\bbb.jar");
    artifact.setNewArtifactURI(newArtifact, newArtifactFile);
    assertThat(newArtifact.getUri()).as("Relative path is not the expected").isEqualTo(EXPECTED_URI);
  }

  @Test
  void setNewArtifactURIUnixRelativePathTest() throws URISyntaxException {
    newArtifactFile = new File("repository/aaa/bbb.jar");
    artifact.setNewArtifactURI(newArtifact, newArtifactFile);
    assertThat(newArtifact.getUri()).as("Relative path is not the expected").isEqualTo(EXPECTED_URI);
  }

  @Test
  void getFormattedMavenDirectoryTest(@TempDir File outputFolder) {
    File actual = artifact.getFormattedMavenDirectory(outputFolder);
    File expected = new File(outputFolder, OUTPUT_DIRECTORY);
    assertThat(actual.getAbsolutePath()).as("Actual formatted output directory is not the expected")
        .isEqualTo(expected.getAbsolutePath());
  }

  @Test
  void getFormattedArtifactFileNameTest() {
    String actual = artifact.getFormattedArtifactFileName();
    String expected = ARTIFACT_ID + "-" + VERSION + "-" + CLASSIFIER + "." + TYPE;
    assertThat(actual).as("Formatted file name is different from the expected").isEqualTo(expected);
  }

  @Test
  void getFormattedFileNameWithoutClassifierTest() {
    artifactCoordinates.setClassifier(StringUtils.EMPTY);
    artifact = new Artifact(artifactCoordinates, URI.create(RESOURCE_FULL_PATH));
    String actual = artifact.getFormattedArtifactFileName();
    String expected = ARTIFACT_ID + "-" + VERSION + "." + TYPE;
    assertThat(actual).as("Formatted file name is different from the expected").isEqualTo(expected);
  }
}

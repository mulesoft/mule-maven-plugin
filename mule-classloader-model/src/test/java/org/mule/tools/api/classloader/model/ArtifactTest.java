/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.classloader.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ArtifactTest {

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

  @Rule
  public TemporaryFolder outputFolder = new TemporaryFolder();
  private ArtifactCoordinates artifactCoordinates;

  @Before
  public void setUp() {
    artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    artifact = new Artifact(artifactCoordinates, URI.create(RESOURCE_FULL_PATH));
    newArtifact = new Artifact(artifactCoordinates, URI.create(RESOURCE_FULL_PATH));
  }

  @Test
  public void setNewArtifactURIWindowsRelativePathTest() throws URISyntaxException {
    newArtifactFile = new File("repository\\aaa\\bbb.jar");
    artifact.setNewArtifactURI(newArtifact, newArtifactFile);
    assertThat("Relative path is not the expected", newArtifact.getUri(), equalTo(EXPECTED_URI));
  }

  @Test
  public void setNewArtifactURIUnixRelativePathTest() throws URISyntaxException {
    newArtifactFile = new File("repository/aaa/bbb.jar");
    artifact.setNewArtifactURI(newArtifact, newArtifactFile);
    assertThat("Relative path is not the expected", newArtifact.getUri(), equalTo(EXPECTED_URI));
  }

  @Test
  public void getFormattedMavenDirectoryTest() {
    File actual = artifact.getFormattedMavenDirectory(outputFolder.getRoot());
    File expected = new File(outputFolder.getRoot(),
                             OUTPUT_DIRECTORY);
    assertThat("Actual formatted output directory is not the expected", actual.getAbsolutePath(),
               equalTo(expected.getAbsolutePath()));
  }

  @Test
  public void getFormattedArtifactFileNameTest() {
    String actual = artifact.getFormattedArtifactFileName();
    String expected = ARTIFACT_ID + "-" + VERSION + "-" + CLASSIFIER + "." + TYPE;
    assertThat("Formatted file name is different from the expected", actual, equalTo(expected));
  }

  @Test
  public void getFormattedFileNameWithoutClassifierTest() {
    artifactCoordinates.setClassifier(StringUtils.EMPTY);
    artifact = new Artifact(artifactCoordinates, URI.create(RESOURCE_FULL_PATH));
    String actual = artifact.getFormattedArtifactFileName();
    String expected = ARTIFACT_ID + "-" + VERSION + "." + TYPE;
    assertThat("Formatted file name is different from the expected", actual, equalTo(expected));
  }

  @Test
  public void artifactsAreNotSharedByDefault() {
    artifact = new Artifact(artifactCoordinates, EXPECTED_URI);
    assertThat(artifact.isShared(), equalTo(false));
  }

  @Test
  public void artifactsWithDifferentClassifierAreNotEqual() {
    ArtifactCoordinates classifiedCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, null);
    Artifact classifiedArtifact = new Artifact(classifiedCoordinates, URI.create(RESOURCE_FULL_PATH));

    TreeSet<Artifact> artifacts = new TreeSet<>();
    artifacts.add(classifiedArtifact);
    artifacts.add(artifact);

    assertThat(artifacts, hasSize(2));
  }
}

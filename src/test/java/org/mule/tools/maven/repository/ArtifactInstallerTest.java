/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class ArtifactInstallerTest {

  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String SNAPSHOT_VERSION = "SNAPSHOT";
  private static final String SCOPE = "compile";
  private static final String TYPE = "zip";
  private static final String CLASSIFIER = "classifier";
  private static final String TIMESTAMP = "20100101.101010-1";
  private static final String FILE_NAME = "file";
  private static final String PREFIX_GROUP_ID = "group";
  private static final String POSFIX_GROUP_ID = "id";
  private static final String GROUP_ID = PREFIX_GROUP_ID + "." + POSFIX_GROUP_ID;
  private static final String ARTIFACT_FILE_NAME = "artifact-file";
  private static final String GENERATED_PACKAGE_NAME = "artifact-id-1.0.0-classifier.zip";
  private static final String OUTPUT_DIRECTORY =
      PREFIX_GROUP_ID + File.separator + POSFIX_GROUP_ID + File.separator + ARTIFACT_ID + File.separator + VERSION;
  private Log logMock;
  private ArtifactHandler handler;
  private ArtifactInstaller installer;
  private Artifact artifact;

  @Rule
  public ExpectedException exception = ExpectedException.none();
  @Rule
  public TemporaryFolder outputFolder = new TemporaryFolder();;
  @Rule
  public TemporaryFolder artifactFileFolder = new TemporaryFolder();;
  private RepositorySystem aetherRepositorySystemMock;
  private RepositorySystemSession aetherRepositorySystemSessionMock;

  @Before
  public void before() throws IOException {
    logMock = mock(Log.class);
    handler = new DefaultArtifactHandler(TYPE);
    List<ArtifactRepository> remoteArtifactRepositories = new ArrayList<>();
    aetherRepositorySystemMock = mock(RepositorySystem.class);
    aetherRepositorySystemSessionMock = mock(RepositorySystemSession.class);
    installer = new ArtifactInstaller(logMock, remoteArtifactRepositories, aetherRepositorySystemMock,
                                      aetherRepositorySystemSessionMock);
    outputFolder.create();
    artifactFileFolder.create();
    artifact = new DefaultArtifact(GROUP_ID, ARTIFACT_ID, VERSION, SCOPE, TYPE, CLASSIFIER, handler);
  }

  @Test
  public void getNormalizedVersionFromBaseVersionTest() {
    artifact = new DefaultArtifact(GROUP_ID, ARTIFACT_ID, VERSION + "-" + TIMESTAMP, SCOPE, TYPE, CLASSIFIER, handler);
    String actual = installer.getNormalizedVersion(artifact);
    assertThat("The normalized version is different from the expected", actual, equalTo(VERSION + "-" + SNAPSHOT_VERSION));
  }


  @Test
  public void getNormalizedVersionFromVersionTest() {
    String expected = installer.getNormalizedVersion(artifact);
    assertThat("The normalized version is different from the expected", expected, equalTo(VERSION));
  }

  @Test
  public void getFormattedOutputDirectoryTest() {
    File actual = ArtifactInstaller.getFormattedOutputDirectory(outputFolder.getRoot(), artifact);
    File expected = new File(outputFolder.getRoot(),
                             OUTPUT_DIRECTORY);
    assertThat("Actual formatted output directory is not the expected", actual.getAbsolutePath(),
               equalTo(expected.getAbsolutePath()));
  }

  @Test
  public void getFormattedFileNameTest() {
    String actual = installer.getFormattedFileName(artifact);
    String expected = ARTIFACT_ID + "-" + VERSION + "-" + CLASSIFIER + "." + TYPE;
    assertThat("Formatted file name is different from the expected", actual, equalTo(expected));
  }

  @Test
  public void getFormattedFileNameWithoutClassifierTest() {
    artifact = new DefaultArtifact(GROUP_ID, ARTIFACT_ID, VERSION, SCOPE, TYPE, null, handler);
    String actual = installer.getFormattedFileName(artifact);
    String expected = ARTIFACT_ID + "-" + VERSION + "." + TYPE;
    assertThat("Formatted file name is different from the expected", actual, equalTo(expected));
  }

  @Test
  public void getFormattedFileNameWithoutHandlerTest() {
    artifact = new DefaultArtifact(GROUP_ID, ARTIFACT_ID, VERSION, SCOPE, TYPE, CLASSIFIER, null);
    String actual = installer.getFormattedFileName(artifact);
    String expected = ARTIFACT_ID + "-" + VERSION + "-" + CLASSIFIER + "." + TYPE;
    assertThat("Formatted file name is different from the expected", actual, equalTo(expected));
  }

  @Test
  public void installArtifactTest() throws MojoExecutionException, IOException {
    artifact = new DefaultArtifact(GROUP_ID, ARTIFACT_ID, VERSION, SCOPE, TYPE, CLASSIFIER, handler);
    artifact.setFile(artifactFileFolder.newFile(ARTIFACT_FILE_NAME));
    File installedFile = new File(outputFolder.getRoot(), OUTPUT_DIRECTORY + File.separator + GENERATED_PACKAGE_NAME);
    assertThat("File should not be installed yet", !installedFile.exists());
    installer.installArtifact(outputFolder.getRoot(), artifact);
    assertThat("File was not installed", installedFile.exists());
  }

  @Test
  public void installNullArtifactTest() throws MojoExecutionException {
    exception.expect(IllegalArgumentException.class);
    installer.installArtifact(outputFolder.getRoot(), null);
  }

  @Test
  public void installArtifactToReadOnlyDestinationTest() throws MojoExecutionException {
    exception.expect(MojoExecutionException.class);
    File destination = new File(outputFolder.getRoot(), FILE_NAME);
    destination.setReadOnly();
    artifact.setFile(destination);
    installer.installArtifact(outputFolder.getRoot(), artifact);
  }
}

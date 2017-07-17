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
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mule.tools.api.util.FileUtils;

public class RepositoryGeneratorTest {

  private static final int NUMBER_ARTIFACTS = 10;
  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String SCOPE = "compile";
  private static final String TYPE = "zip";
  private static final String CLASSIFIER = "classifier";
  private static final String REPOSITORY_FOLDER = "repository";
  private TemporaryFolder temporaryFolder;
  private RepositoryGenerator repositoryGenerator;
  private MavenProject projectMock;
  private ProjectBuilder projectBuilderMock;
  private ArtifactInstaller artifactInstallerMock;
  private List<ArtifactRepository> remoteArtifactRepositoriesMock;
  private ProjectBuildingResult resultMock;
  private Log logMock;
  private ArtifactHandler artifactHandler;
  private Set<Artifact> artifacts;

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Spy
  RepositoryGenerator repositoryGeneratorSpy;

  @Before
  public void before() throws IOException, ProjectBuildingException {
    temporaryFolder = new TemporaryFolder();
    temporaryFolder.create();
    projectMock = mock(MavenProject.class);
    projectBuilderMock = mock(ProjectBuilder.class);
    resultMock = mock(ProjectBuildingResult.class);
    when(resultMock.getProject()).thenReturn(projectMock);
    when(projectBuilderMock.build(Mockito.any(Artifact.class), any(ProjectBuildingRequest.class))).thenReturn(resultMock);
    remoteArtifactRepositoriesMock = new ArrayList<>();
    logMock = mock(Log.class);
    artifactInstallerMock = mock(ArtifactInstaller.class);
    repositoryGenerator = new RepositoryGenerator(projectMock,
                                                  remoteArtifactRepositoriesMock,
                                                  temporaryFolder.getRoot(), logMock);
    repositoryGeneratorSpy = spy(repositoryGenerator);
    artifactHandler = new DefaultArtifactHandler(TYPE);
  }

  private void buildArtifacts() {
    artifacts = new HashSet<>();
    for (int i = 0; i < NUMBER_ARTIFACTS; ++i) {
      artifacts.add(createArtifact(i));
    }
  }

  @Test
  public void generateMarkerFileInRepositoryFolderTest() throws MojoExecutionException {
    File generatedMarkerFile = new File(temporaryFolder.getRoot(), ".marker");

    assertThat("Marker file already exists", !generatedMarkerFile.exists());

    repositoryGenerator.generateMarkerFileInRepositoryFolder(temporaryFolder.getRoot());

    assertThat("Marker file was not generated", generatedMarkerFile.exists());
  }

  @Test
  public void generateMarkerFileInRepositoryFolderWhenFolderIsNotWritableTest() throws MojoExecutionException, IOException {
    exception.expect(MojoExecutionException.class);
    File generatedMarkerFile = new File(temporaryFolder.getRoot(), ".marker");

    assertThat("Marker file already exists", !generatedMarkerFile.exists());

    File readOnlyFolder = temporaryFolder.getRoot();
    FileUtils.markAsReadOnly(readOnlyFolder);

    repositoryGenerator.generateMarkerFileInRepositoryFolder(readOnlyFolder);
  }

  @Test
  public void installEmptySetArtifactsTest() throws MojoExecutionException {
    File repositoryFolder = temporaryFolder.getRoot();
    repositoryGeneratorSpy.installArtifacts(repositoryFolder, Collections.emptySet(), artifactInstallerMock);
    verify(repositoryGeneratorSpy, times(1)).generateMarkerFileInRepositoryFolder(repositoryFolder);
    verify(artifactInstallerMock, times(0)).installArtifact(any(), any());
  }

  @Test
  public void installArtifactsTest() throws MojoExecutionException {
    File repositoryFolder = temporaryFolder.getRoot();
    buildArtifacts();
    repositoryGeneratorSpy.installArtifacts(repositoryFolder, artifacts, artifactInstallerMock);
    verify(repositoryGeneratorSpy, times(0)).generateMarkerFileInRepositoryFolder(repositoryFolder);
    verify(artifactInstallerMock, times(NUMBER_ARTIFACTS)).installArtifact(any(), any());
  }

  @Test
  public void getRepositoryFolderIfDoesNotExistTest() {
    temporaryFolder.delete();
    File repositoryFolder = new File(temporaryFolder.getRoot(), REPOSITORY_FOLDER);
    assertThat("Repository folder already exists", !repositoryFolder.exists());
    repositoryFolder = repositoryGenerator.getRepositoryFolder();
    assertThat("Repository folder was not created", repositoryFolder.exists());
  }

  @Test
  public void getRepositoryFolderIfAlreadyExistsTest() {
    File expectedRepositoryFolder = temporaryFolder.newFolder(REPOSITORY_FOLDER);
    assertThat("Repository folder does not exist", expectedRepositoryFolder.exists());
    File actualRepositoryFolder = repositoryGenerator.getRepositoryFolder();
    assertThat("Repository folder was modified", actualRepositoryFolder, equalTo(expectedRepositoryFolder));
  }

  private Artifact createArtifact(int i) {
    return new DefaultArtifact(GROUP_ID + "." + i, ARTIFACT_ID + "-" + i, VERSION, SCOPE, TYPE,
                               CLASSIFIER, artifactHandler);
  }
}

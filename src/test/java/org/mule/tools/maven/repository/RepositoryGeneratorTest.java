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
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.*;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.*;
import org.apache.maven.repository.RepositorySystem;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mule.tools.maven.util.FileUtils;

public class RepositoryGeneratorTest {

  private static final int NUMBER_ARTIFACTS = 10;
  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String SCOPE = "compile";
  private static final String TYPE = "zip";
  private static final String CLASSIFIER = "classifier";
  private static final String REPOSITORY_FOLDER = "repository";
  private static final int NUMBER_ARTIFACT_REPOSITORIES = 10;
  private TemporaryFolder temporaryFolder;
  private RepositoryGenerator repositoryGenerator;
  private MavenSession sessionMock;
  private MavenProject projectMock;
  private ProjectBuilder projectBuilderMock;
  private RepositorySystem repositorySystemMock;
  private ArtifactRepository localRepositoryMock;
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
    sessionMock = mock(MavenSession.class);
    projectMock = mock(MavenProject.class);
    projectBuilderMock = mock(ProjectBuilder.class);
    resultMock = mock(ProjectBuildingResult.class);
    when(resultMock.getProject()).thenReturn(projectMock);
    when(projectBuilderMock.build(Mockito.any(Artifact.class), any(ProjectBuildingRequest.class))).thenReturn(resultMock);
    repositorySystemMock = mock(RepositorySystem.class);
    localRepositoryMock = mock(ArtifactRepository.class);
    remoteArtifactRepositoriesMock = new ArrayList<>();
    logMock = mock(Log.class);
    artifactInstallerMock = mock(ArtifactInstaller.class);
    repositoryGenerator = new RepositoryGenerator(sessionMock,
                                                  projectMock,
                                                  projectBuilderMock,
                                                  repositorySystemMock,
                                                  localRepositoryMock,
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

  @Test
  public void initializeProjectBuildingRequestTest() {
    when(localRepositoryMock.getBasedir()).thenReturn(temporaryFolder.getRoot().getAbsolutePath());

    when(sessionMock.getProjectBuildingRequest()).thenReturn(new DefaultProjectBuildingRequest());

    List<ArtifactRepository> remoteArtifactRepositories = new ArrayList<>();

    for (int i = 0; i < NUMBER_ARTIFACT_REPOSITORIES; ++i) {
      remoteArtifactRepositories.add(createArtifactRepository(i));
    }

    repositoryGenerator = new RepositoryGenerator(sessionMock,
                                                  projectMock,
                                                  projectBuilderMock,
                                                  repositorySystemMock,
                                                  localRepositoryMock,
                                                  remoteArtifactRepositories,
                                                  temporaryFolder.getRoot(), logMock);

    repositoryGenerator.initializeProjectBuildingRequest();

    verify(localRepositoryMock, times(1)).getBasedir();

    for (ArtifactRepository repository : remoteArtifactRepositories) {
      verify(repository, times(1)).getId();
      verify(repository, times(1)).getUrl();
    }
  }

  @Test
  public void generateTest() throws MojoFailureException, MojoExecutionException, ProjectBuildingException {
    buildArtifacts();
    when(sessionMock.getProjectBuildingRequest()).thenReturn(new DefaultProjectBuildingRequest());

    ArtifactLocator artifactLocatorMock = mock(ArtifactLocator.class);

    when(artifactLocatorMock.getArtifacts()).thenReturn(artifacts);

    doNothing().when(repositoryGeneratorSpy).initializeProjectBuildingRequest();
    when(repositoryGeneratorSpy.buildArtifactLocator()).thenReturn(artifactLocatorMock);
    doNothing().when(repositoryGeneratorSpy).installArtifacts(any(File.class), anySet(), any(ArtifactInstaller.class));
    File repositoryFolder = temporaryFolder.newFolder(REPOSITORY_FOLDER);
    when(repositoryGeneratorSpy.buildArtifactInstaller()).thenReturn(artifactInstallerMock);
    when(repositoryGeneratorSpy.getRepositoryFolder()).thenReturn(repositoryFolder);

    repositoryGeneratorSpy.generate();

    verify(artifactLocatorMock, times(1)).getArtifacts();
    verify(repositoryGeneratorSpy, times(1)).initializeProjectBuildingRequest();
    verify(repositoryGeneratorSpy, times(1)).getRepositoryFolder();
    verify(repositoryGeneratorSpy, times(1)).installArtifacts(repositoryFolder, artifacts,
                                                              artifactInstallerMock);

  }

  private ArtifactRepository createArtifactRepository(int i) {
    ArtifactRepository repository = mock(ArtifactRepository.class);
    when(repository.getId()).thenReturn("id" + i);
    when(repository.getUrl()).thenReturn("url" + i);
    return repository;
  }

  private Artifact createArtifact(int i) {
    return new DefaultArtifact(GROUP_ID + "." + i, ARTIFACT_ID + "-" + i, VERSION, SCOPE, TYPE,
                               CLASSIFIER, artifactHandler);
  }
}

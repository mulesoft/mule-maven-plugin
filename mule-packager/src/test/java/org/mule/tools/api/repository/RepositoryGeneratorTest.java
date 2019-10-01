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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import org.apache.maven.project.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Spy;
import org.mule.tools.api.classloader.model.ApplicationClassLoaderModelAssembler;
import org.mule.tools.api.classloader.model.ApplicationClassloaderModel;
import org.mule.tools.api.classloader.model.ApplicationGAVModel;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.util.FileUtils;

public class RepositoryGeneratorTest {

  private static final int NUMBER_ARTIFACTS = 10;
  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String TYPE = "jar";
  private static final String CLASSIFIER = "classifier";
  private static final String REPOSITORY_FOLDER = "repository";
  private TemporaryFolder temporaryFolder;
  private RepositoryGenerator repositoryGenerator;
  private MavenProject projectMock;
  private ArtifactInstaller artifactInstallerMock;
  private ProjectBuildingResult resultMock;
  private Set<Artifact> artifacts;
  private ApplicationClassloaderModel appModelMock;
  private ApplicationGAVModel appGAVModel;

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Spy
  RepositoryGenerator repositoryGeneratorSpy;

  @Before
  public void before() throws IOException, ProjectBuildingException {
    temporaryFolder = new TemporaryFolder();
    temporaryFolder.create();
    projectMock = mock(MavenProject.class);
    resultMock = mock(ProjectBuildingResult.class);
    when(resultMock.getProject()).thenReturn(projectMock);
    artifactInstallerMock = mock(ArtifactInstaller.class);
    ApplicationClassLoaderModelAssembler applicationClassloaderModelAssemblerMock =
        mock(ApplicationClassLoaderModelAssembler.class);
    appGAVModel = new ApplicationGAVModel(GROUP_ID, ARTIFACT_ID, VERSION);
    repositoryGenerator = new RepositoryGenerator(temporaryFolder.newFile("pom.xml"),
                                                  temporaryFolder.getRoot(), artifactInstallerMock,
                                                  applicationClassloaderModelAssemblerMock, appGAVModel);
    repositoryGeneratorSpy = spy(repositoryGenerator);
    appModelMock = mock(ApplicationClassloaderModel.class);
  }



  @Test
  public void generateMarkerFileInRepositoryFolderTest() throws IOException {
    File generatedMarkerFile = new File(temporaryFolder.getRoot(), ".marker");

    assertThat("Marker file already exists", !generatedMarkerFile.exists());

    repositoryGenerator.generateMarkerFileInRepositoryFolder(temporaryFolder.getRoot());

    assertThat("Marker file was not generated", generatedMarkerFile.exists());
  }

  @Test
  public void generateMarkerFileInRepositoryFolderWhenFolderIsNotWritableTest() throws IOException {
    exception.expect(IOException.class);
    File generatedMarkerFile = new File(temporaryFolder.getRoot(), ".marker");

    assertThat("Marker file already exists", !generatedMarkerFile.exists());

    File readOnlyFolder = temporaryFolder.getRoot();
    FileUtils.markAsReadOnly(readOnlyFolder);

    repositoryGenerator.generateMarkerFileInRepositoryFolder(readOnlyFolder);
  }

  @Test
  public void installEmptySetArtifactsTest() throws IOException {
    File repositoryFolder = temporaryFolder.getRoot();
    when(appModelMock.getArtifacts()).thenReturn(Collections.emptySet());
    repositoryGeneratorSpy.installArtifacts(repositoryFolder, artifactInstallerMock, appModelMock, false);
    verify(repositoryGeneratorSpy, times(1)).generateMarkerFileInRepositoryFolder(repositoryFolder);
    verify(artifactInstallerMock, times(0)).installArtifact(any(), any(), any(), eq(false));
  }

  @Test
  public void installArtifactsTest() throws IOException {
    File repositoryFolder = temporaryFolder.getRoot();
    buildArtifacts();
    when(appModelMock.getArtifacts()).thenReturn(artifacts);
    repositoryGeneratorSpy.installArtifacts(repositoryFolder, artifactInstallerMock, appModelMock, true);
    verify(repositoryGeneratorSpy, times(0)).generateMarkerFileInRepositoryFolder(repositoryFolder);
    verify(artifactInstallerMock, times(NUMBER_ARTIFACTS)).installArtifact(any(), any(), any(), eq(true));
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
  public void getRepositoryFolderIfAlreadyExistsTest() throws IOException {
    File expectedRepositoryFolder = temporaryFolder.newFolder(REPOSITORY_FOLDER);
    assertThat("Repository folder does not exist", expectedRepositoryFolder.exists());
    File actualRepositoryFolder = repositoryGenerator.getRepositoryFolder();
    assertThat("Repository folder was modified", actualRepositoryFolder, equalTo(expectedRepositoryFolder));
  }

  private void buildArtifacts() {
    artifacts = new HashSet<>();
    for (int i = 0; i < NUMBER_ARTIFACTS; ++i) {
      artifacts.add(createArtifact(i));
    }
  }

  private Artifact createArtifact(int i) {
    return new Artifact(new ArtifactCoordinates(GROUP_ID + "." + i, ARTIFACT_ID + "-" + i, VERSION, TYPE,
                                                CLASSIFIER),
                        URI.create("/"));
  }
}

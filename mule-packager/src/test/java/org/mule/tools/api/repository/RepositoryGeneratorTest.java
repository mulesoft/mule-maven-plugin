/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;

import org.apache.maven.project.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Spy;
import org.mule.tools.api.muleclassloader.model.ApplicationClassLoaderModelAssembler;
import org.mule.tools.api.classloader.model.ApplicationClassloaderModel;
import org.mule.tools.api.classloader.model.ApplicationGAVModel;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;

class RepositoryGeneratorTest {

  private static final int NUMBER_ARTIFACTS = 10;
  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String TYPE = "jar";
  private static final String CLASSIFIER = "classifier";
  private static final String REPOSITORY_FOLDER = "repository";
  @TempDir
  public Path temporaryFolder;
  private RepositoryGenerator repositoryGenerator;
  private MavenProject projectMock;
  private ArtifactInstaller artifactInstallerMock;
  private ProjectBuildingResult resultMock;
  private Set<Artifact> artifacts;
  private ApplicationClassloaderModel appModelMock;
  private ApplicationGAVModel appGAVModel;

  @Spy
  RepositoryGenerator repositoryGeneratorSpy;

  @BeforeEach
  public void before() throws IOException, ProjectBuildingException {
    projectMock = mock(MavenProject.class);
    resultMock = mock(ProjectBuildingResult.class);
    when(resultMock.getProject()).thenReturn(projectMock);
    artifactInstallerMock = mock(ArtifactInstaller.class);
    ApplicationClassLoaderModelAssembler applicationClassloaderModelAssemblerMock =
        mock(ApplicationClassLoaderModelAssembler.class);
    appGAVModel = new ApplicationGAVModel(GROUP_ID, ARTIFACT_ID, VERSION);
    repositoryGenerator = new RepositoryGenerator(temporaryFolder.resolve("pom.xml").toFile(),
                                                  temporaryFolder.toFile(), artifactInstallerMock,
                                                  applicationClassloaderModelAssemblerMock, appGAVModel, new ArrayList<String>());
    repositoryGeneratorSpy = spy(repositoryGenerator);
    appModelMock = mock(ApplicationClassloaderModel.class);
  }



  @Test
  void generateMarkerFileInRepositoryFolderTest() throws IOException {
    File generatedMarkerFile = temporaryFolder.resolve(".marker").toFile();

    assertThat(generatedMarkerFile).describedAs("Marker file already exists").doesNotExist();

    repositoryGenerator.generateMarkerFileInRepositoryFolder(temporaryFolder.toFile());

    assertThat(generatedMarkerFile).describedAs("Marker file was not generated").exists();
  }

  @Test
  void generateMarkerFileInRepositoryFolderWhenFolderIsNotWritableTest() throws IOException {
    File generatedMarkerFile = temporaryFolder.resolve(".marker").toFile();

    assertThat(generatedMarkerFile).describedAs("Marker file already exists").doesNotExist();

    assertThatThrownBy(() -> {
      repositoryGenerator.generateMarkerFileInRepositoryFolder(mockReadOnlyFolder(true));
    }).isExactlyInstanceOf(IOException.class);
  }

  @Test
  void installEmptySetArtifactsTest() throws IOException {
    File repositoryFolder = temporaryFolder.toFile();
    when(appModelMock.getArtifacts()).thenReturn(Collections.emptySet());
    repositoryGeneratorSpy.installArtifacts(repositoryFolder, artifactInstallerMock, appModelMock, false);
    verify(repositoryGeneratorSpy, times(1)).generateMarkerFileInRepositoryFolder(repositoryFolder);
    verify(artifactInstallerMock, times(0)).installArtifact(any(), any(), any(), eq(false));
  }

  @Test
  void installArtifactsTest() throws IOException {
    File repositoryFolder = temporaryFolder.toFile();
    buildArtifacts();
    when(appModelMock.getArtifacts()).thenReturn(artifacts);
    repositoryGeneratorSpy.installArtifacts(repositoryFolder, artifactInstallerMock, appModelMock, true);
    verify(repositoryGeneratorSpy, times(0)).generateMarkerFileInRepositoryFolder(repositoryFolder);
    verify(artifactInstallerMock, times(NUMBER_ARTIFACTS)).installArtifact(any(), any(), any(), eq(true));
  }

  @Test
  void getRepositoryFolderIfDoesNotExistTest() {
    File repositoryFolder = temporaryFolder.resolve(REPOSITORY_FOLDER).toFile();
    assertThat(repositoryFolder).describedAs("Repository folder already exists").doesNotExist();
    repositoryFolder = repositoryGenerator.getRepositoryFolder();
    assertThat(repositoryFolder).describedAs("Repository folder was not created").exists();
  }

  @Test
  void getRepositoryFolderIfAlreadyExistsTest() throws IOException {
    File expectedRepositoryFolder = temporaryFolder.resolve(REPOSITORY_FOLDER).toFile();
    expectedRepositoryFolder.mkdirs();
    assertThat(expectedRepositoryFolder).describedAs("Repository folder does not exist").exists();
    File actualRepositoryFolder = repositoryGenerator.getRepositoryFolder();
    assertThat(actualRepositoryFolder).describedAs("Repository folder was modified").isEqualTo(expectedRepositoryFolder);
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

  public static File mockReadOnlyFolder(boolean readOnly) throws IOException {
    Path path = mock(Path.class);
    FileSystem fileSystem = mock(FileSystem.class);
    FileSystemProvider fileSystemProvider = mock(FileSystemProvider.class);
    FileStore fileStore = mock(FileStore.class);
    DosFileAttributeView dosFileAttributeView = mock(DosFileAttributeView.class);
    DosFileAttributes dosFileAttributes = mock(DosFileAttributes.class);

    File file = new File("") {

      @Override
      public Path toPath() {
        return path;
      }
    };

    when(path.getFileSystem()).thenReturn(fileSystem);
    when(fileSystem.provider()).thenReturn(fileSystemProvider);
    when(fileSystemProvider.getFileStore(any(Path.class))).thenReturn(fileStore);
    when(fileStore.supportsFileAttributeView(DosFileAttributeView.class)).thenReturn(true);
    when(fileSystemProvider.getFileAttributeView(any(Path.class), eq(DosFileAttributeView.class), any()))
        .thenReturn(dosFileAttributeView);
    when(dosFileAttributeView.readAttributes()).thenReturn(dosFileAttributes);
    when(dosFileAttributes.isReadOnly()).thenReturn(readOnly);

    return file;
  }
}

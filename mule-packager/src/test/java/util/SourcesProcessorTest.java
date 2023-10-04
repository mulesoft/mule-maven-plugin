/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package util;

import static java.nio.file.Paths.get;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.api.util.MavenComponents;
import org.mule.tools.api.util.SourcesProcessor;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;

class SourcesProcessorTest {

  @TempDir
  public File temporaryFolder;

  private SourcesProcessor sourcesProcessor;

  private MavenProject project;

  @BeforeEach
  void setUp() {
    MavenSession session = mock(MavenSession.class);
    this.project = buildMavenProjectMock();
    ProjectBuildingRequest projectBuildingRequest = mock(ProjectBuildingRequest.class);

    when(projectBuildingRequest.getRepositoryMerging()).thenReturn(ProjectBuildingRequest.RepositoryMerging.REQUEST_DOMINANT);
    when(session.getRequest()).thenReturn(mock(MavenExecutionRequest.class));
    when(session.getProjectBuildingRequest()).thenReturn(projectBuildingRequest);
    when(session.getCurrentProject()).thenReturn(project);
    when(session.getGoals()).thenReturn(Lists.newArrayList());

    Properties systemProperties = new Properties();
    systemProperties.put("muleDeploy", "false");
    when(session.getSystemProperties()).thenReturn(systemProperties);

    MavenComponents mavenComponents =
        new MavenComponents().withLog(mock(Log.class))
            .withProject(project)
            .withOutputDirectory(new File(temporaryFolder, "target"))
            .withSession(session)
            .withSharedLibraries(new Vector<>())
            .withProjectBuilder(mock(ProjectBuilder.class))
            .withRepositorySystem(mock(RepositorySystem.class))
            .withLocalRepository(mock(ArtifactRepository.class))
            .withRemoteArtifactRepositories(new Vector<>())
            .withClassifier("")
            .withAdditionalPluginDependencies(new Vector<>())
            .withProjectBaseFolder(temporaryFolder);

    this.sourcesProcessor = new SourcesProcessor(mavenComponents);
  }

  @Test
  void nullMavenComponents() {
    assertThatThrownBy(() -> new SourcesProcessor(null)).isExactlyInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void lightweightTest() throws Exception {
    sourcesProcessor
        .process(true, true, false, true, new File(temporaryFolder, "target"),
                 temporaryFolder.toPath().resolve("target").resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
                     .toFile(),
                 empty());

    assertThat(get(temporaryFolder.getAbsolutePath(), "target", "META-INF", "mule-artifact")).doesNotExist();
    assertThat(get(temporaryFolder.getAbsolutePath(), "target", "META-INF", "mule-artifact", "classloader-model.json"))
        .doesNotExist();
  }

  @Test
  void lightweight() throws Exception {
    sourcesProcessor
        .process(true, true, false, false, new File(temporaryFolder, "target"),
                 temporaryFolder.toPath().resolve("target").resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
                     .toFile(),
                 empty());

    assertThat(get(temporaryFolder.getAbsolutePath(), "target", "META-INF", "mule-artifact")).doesNotExist();
    assertThat(get(temporaryFolder.getAbsolutePath(), "target", "META-INF", "mule-artifact", "classloader-model.json"))
        .doesNotExist();
  }

  public MavenProject buildMavenProjectMock() {

    Build buildMock = mock(Build.class);

    try {
      FileUtils.copyDirectory(get("src", "test", "resources", "test-app").toFile(), temporaryFolder);
    } catch (IOException e) {
      e.printStackTrace();
    }

    when(buildMock.getDirectory())
        .thenReturn(get(temporaryFolder.getAbsolutePath(), "target").toAbsolutePath().toString());
    when(buildMock.getOutputDirectory())
        .thenReturn(get(temporaryFolder.getAbsolutePath(), "target").toAbsolutePath().toString());
    when(buildMock.getTestOutputDirectory()).thenReturn(temporaryFolder.getAbsolutePath());

    MavenProject mavenProjectMock = mock(MavenProject.class);
    when(mavenProjectMock.getBuild()).thenReturn(buildMock);
    when(mavenProjectMock.getGroupId()).thenReturn("com.mycompany");
    when(mavenProjectMock.getVersion()).thenReturn("1.0.0-SNAPSHOT");
    when(mavenProjectMock.getArtifactId()).thenReturn("test-app");
    when(mavenProjectMock.getFile()).thenReturn(new File(temporaryFolder, "pom.xml"));
    when(mavenProjectMock.getBasedir()).thenReturn(temporaryFolder);
    when(mavenProjectMock.getPackaging()).thenReturn("mule-application");
    when(mavenProjectMock.getModel()).thenReturn(mock(Model.class));

    return mavenProjectMock;
  }

}

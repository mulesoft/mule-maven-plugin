/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package util;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.*;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.repository.RepositorySystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.util.*;
import org.mule.tools.api.util.MavenProjectBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static java.nio.file.Paths.get;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MavenProjectBuilderTest {

  @TempDir
  public File temporaryFolder;

  @Test
  public void buildProject() throws Exception {
    MavenSession session = mock(MavenSession.class);
    when(session.getProjectBuildingRequest()).thenReturn(null);
    ProjectBuildingRequest projectBuildingRequest = mock(ProjectBuildingRequest.class);

    when(projectBuildingRequest.getRepositoryMerging()).thenReturn(ProjectBuildingRequest.RepositoryMerging.REQUEST_DOMINANT);
    when(session.getRequest()).thenReturn(mock(MavenExecutionRequest.class));
    when(session.getProjectBuildingRequest()).thenReturn(projectBuildingRequest);
    RepositorySystem repository = mock(RepositorySystem.class);
    when(repository.createProjectArtifact(any(), any(), any()))
        .thenReturn(new DefaultArtifact("groupId", "artifactId", "version", "scope", "type", "classifier", null));
    ProjectBuilder projectBuilder = mock(ProjectBuilder.class);

    List<ProjectBuildingResult> results = new ArrayList<ProjectBuildingResult>();
    results.add(new ProjectBuildingResult() {

      @Override
      public String getProjectId() {
        return "";
      }

      @Override
      public File getPomFile() {
        return null;
      }

      @Override
      public MavenProject getProject() {
        return null;
      }

      @Override
      public List<ModelProblem> getProblems() {
        return new ArrayList<>();
      }

      @Override
      public DependencyResolutionResult getDependencyResolutionResult() {
        return null;
      }
    });

    when(projectBuilder.build(any(Artifact.class), any()))
        .thenThrow(new org.apache.maven.project.ProjectBuildingException(results));
    MavenComponents mavenComponents =
        new MavenComponents().withLog(mock(Log.class))
            .withProject(buildMavenProjectMock())
            .withOutputDirectory(new File(temporaryFolder, "target"))
            .withSession(session)
            .withSharedLibraries(new Vector<>())
            .withProjectBuilder(projectBuilder)
            .withRepositorySystem(repository)
            .withLocalRepository(mock(ArtifactRepository.class))
            .withRemoteArtifactRepositories(new Vector<>())
            .withClassifier("")
            .withAdditionalPluginDependencies(new Vector<>())
            .withProjectBaseFolder(temporaryFolder);
    ArtifactCoordinates coordinates = new ArtifactCoordinates("org.mule.tools.maven", "mule-classloader-model", "4.1.0");
    MavenProjectBuilder builder =
        new MavenProjectBuilder(mavenComponents.getLog(), mavenComponents.getSession(), mavenComponents.getProjectBuilder(),
                                mavenComponents.getRepositorySystem(), mavenComponents.getLocalRepository(),
                                mavenComponents.getRemoteArtifactRepositories());
    Project project = builder.buildProject(coordinates);
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

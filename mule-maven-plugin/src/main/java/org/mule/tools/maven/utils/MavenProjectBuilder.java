/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.utils;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.*;
import org.apache.maven.repository.RepositorySystem;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class MavenProjectBuilder {


  private static final String CREATION_ERROR_MESSAGE =
      "There was an issue while trying to create a maven project from the artifact [%s]";
  private static final String CREATION_SEVERAL_ERRORS_MESSAGE = CREATION_ERROR_MESSAGE + ", several FATAL errors were found";
  private Log log;
  private MavenSession session;
  private ProjectBuilder projectBuilder;
  private RepositorySystem repositorySystem;
  private ArtifactRepository localRepository;
  private List<ArtifactRepository> remoteArtifactRepositories;


  private ProjectBuildingRequest projectBuildingRequest;

  public MavenProjectBuilder(Log log, MavenSession session,
                             ProjectBuilder projectBuilder,
                             RepositorySystem repositorySystem,
                             ArtifactRepository localRepository,
                             List<ArtifactRepository> remoteArtifactRepositories) {
    this.log = log;
    this.session = session;
    this.projectBuilder = projectBuilder;
    this.repositorySystem = repositorySystem;
    this.localRepository = localRepository;
    this.remoteArtifactRepositories = remoteArtifactRepositories;
    initialize();
  }

  private void initialize() {
    projectBuildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
    projectBuildingRequest.setLocalRepository(localRepository);
    projectBuildingRequest.setRemoteRepositories(remoteArtifactRepositories);

    log.debug(format("Local repository [%s]", projectBuildingRequest.getLocalRepository().getBasedir()));
    for (ArtifactRepository artifactRepository : projectBuildingRequest.getRemoteRepositories()) {
      log.debug(format("Remote repository ID [%s], URL [%s]", artifactRepository.getId(), artifactRepository.getUrl()));
    }
  }

  public MavenProject buildMavenProject(Dependency dependency) throws MojoExecutionException {
    Artifact projectArtifact =
        repositorySystem.createProjectArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    return buildMavenProjectFromArtifact(projectArtifact);
  }

  private MavenProject buildMavenProjectFromArtifact(Artifact artifact) throws MojoExecutionException {
    MavenProject mavenProject;

    try {
      Artifact projectArtifact = repositorySystem
          .createProjectArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
      mavenProject = projectBuilder.build(projectArtifact, projectBuildingRequest).getProject();
    } catch (ProjectBuildingException e) {
      log.warn(format("The artifact [%s] seems to have some warnings", artifact.toString()));
      log.debug(format("The artifact [%s] had the following issue ", artifact.toString()), e);
      mavenProject = buildMavenMavenProjectWithErrors(artifact, e);
    }
    return mavenProject;
  }

  /**
   * It will build a maven project as long as there are not fatal errors
   *
   * @param artifact
   * @param e
   * @return
   * @throws MojoExecutionException
   */
  private MavenProject buildMavenMavenProjectWithErrors(Artifact artifact, ProjectBuildingException e)
      throws MojoExecutionException {
    List<ProjectBuildingResult> results = e.getResults();
    if (results == null || results.size() != 1) {
      throw new MojoExecutionException(format(CREATION_ERROR_MESSAGE, artifact.toString()), e);
    }
    ProjectBuildingResult projectBuildingResult = results.get(0);
    List<ModelProblem> fatalProblems = projectBuildingResult.getProblems().stream()
        .filter(modelProblem -> modelProblem.getSeverity().equals(ModelProblem.Severity.FATAL)).collect(
                                                                                                        Collectors.toList());
    if (!fatalProblems.isEmpty()) {
      throw new MojoExecutionException(format(CREATION_SEVERAL_ERRORS_MESSAGE, artifact.toString()), e);
    }

    return projectBuildingResult.getProject();
  }
}

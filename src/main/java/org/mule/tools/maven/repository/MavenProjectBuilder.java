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

import static java.lang.String.format;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.*;
import org.apache.maven.repository.RepositorySystem;

public class MavenProjectBuilder {

  private final RepositorySystem repositorySystem;
  private final Log log;
  private final ProjectBuilder projectBuilder;
  private final ProjectBuildingRequest projectBuildingRequest;

  public MavenProjectBuilder(ProjectBuilder projectBuilder, ProjectBuildingRequest projectBuildingRequest,
                             RepositorySystem repositorySystem, Log log) {
    this.projectBuilder = projectBuilder;
    this.projectBuildingRequest = projectBuildingRequest;
    this.repositorySystem = repositorySystem;
    this.log = log;
  }

  public MavenProject buildProjectFromArtifact(Artifact artifact)
      throws MojoExecutionException {
    MavenProject mavenProject;
    Artifact projectArtifact =
        repositorySystem.createProjectArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
    try {
      mavenProject = projectBuilder.build(projectArtifact, projectBuildingRequest).getProject();
    } catch (ProjectBuildingException e) {
      log
          .warn(format("The artifact [%s] seems to have some warnings, enable logs for more information",
                       artifact.toString()));
      if (log.isDebugEnabled()) {
        log.warn(format("The artifact [%s] had the following issue ", artifact.toString()), e);
      }
      if (e.getResults() == null || e.getResults().size() != 1) {
        throw new MojoExecutionException(
                                         format("There was an issue while trying to create a maven project from the artifact [%s]",
                                                artifact.toString()),
                                         e);
      }
      final ProjectBuildingResult projectBuildingResult = e.getResults().get(0);
      final List<ModelProblem> collect = projectBuildingResult.getProblems().stream()
          .filter(modelProblem -> modelProblem.getSeverity().equals(ModelProblem.Severity.FATAL)).collect(
                                                                                                          Collectors.toList());
      if (!collect.isEmpty()) {
        throw new MojoExecutionException(format(
                                                "There was an issue while trying to create a maven project from the artifact [%s], several FATAL errors were found",
                                                artifact.toString()),
                                         e);
      }
      mavenProject = projectBuildingResult.getProject();
    }
    return mavenProject;
  }

  public Artifact createProjectArtifact(Artifact dep) {
    return repositorySystem.createProjectArtifact(dep.getGroupId(), dep.getArtifactId(), dep.getVersion());
  }
}

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.*;
import org.apache.maven.repository.RepositorySystem;

public class ArtifactLocator {

  private Log log;
  private MavenProject project;
  private ProjectBuilder projectBuilder;
  private RepositorySystem repositorySystem;
  private ArtifactRepository localRepository;
  private ProjectBuildingRequest projectBuildingRequest;


  public ArtifactLocator(Log log, MavenProject project, ProjectBuilder projectBuilder,
                         RepositorySystem repositorySystem, ArtifactRepository localRepository,
                         ProjectBuildingRequest projectBuildingRequest) {
    this.log = log;
    this.project = project;
    this.projectBuilder = projectBuilder;
    this.repositorySystem = repositorySystem;
    this.localRepository = localRepository;
    this.projectBuildingRequest = projectBuildingRequest;
  }

  public Set<Artifact> getArtifacts() throws MojoExecutionException {
    Set<Artifact> artifacts = new HashSet<>(project.getArtifacts());
    for (Artifact dep : new ArrayList<>(artifacts)) {
      addThirdPartyParentPomArtifacts(artifacts, dep);
    }
    addParentPomArtifacts(artifacts);
    return artifacts;
  }

  private void addThirdPartyParentPomArtifacts(Set<Artifact> artifacts, Artifact dep) throws MojoExecutionException {
    MavenProject project = buildProjectFromArtifact(dep);
    addParentDependencyPomArtifacts(project, artifacts);

    Artifact pomArtifact = repositorySystem.createProjectArtifact(dep.getGroupId(), dep.getArtifactId(), dep.getVersion());
    artifacts.add(getResolvedArtifactUsingLocalRepository(pomArtifact));
  }

  private void addParentPomArtifacts(Set<Artifact> artifacts) throws MojoExecutionException {
    MavenProject currentProject = project;
    boolean projectParent = true;
    while (currentProject.hasParent() && projectParent) {
      currentProject = currentProject.getParent();
      if (currentProject.getFile() == null) {
        projectParent = false;
      } else {
        Artifact pomArtifact = currentProject.getArtifact();
        pomArtifact.setFile(currentProject.getFile());
        validatePomArtifactFile(pomArtifact);
        if (!artifacts.add(pomArtifact)) {
          break;
        }
      }
    }
    if (!projectParent) {
      final Artifact unresolvedParentPomArtifact = currentProject.getArtifact();
      addThirdPartyParentPomArtifacts(artifacts, unresolvedParentPomArtifact);
    }
  }

  private MavenProject buildProjectFromArtifact(Artifact artifact)
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

  private void addParentDependencyPomArtifacts(MavenProject projectDependency, Set<Artifact> artifacts)
      throws MojoExecutionException {
    MavenProject currentProject = projectDependency;
    while (currentProject.hasParent()) {
      currentProject = currentProject.getParent();
      final Artifact pomArtifact = currentProject.getArtifact();
      if (!artifacts.add(getResolvedArtifactUsingLocalRepository(pomArtifact))) {
        break;
      }
    }
  }

  private Artifact getResolvedArtifactUsingLocalRepository(Artifact pomArtifact) throws MojoExecutionException {
    final Artifact resolvedPomArtifact = localRepository.find(pomArtifact);
    validatePomArtifactFile(resolvedPomArtifact);
    return resolvedPomArtifact;
  }

  private void validatePomArtifactFile(Artifact resolvedPomArtifact) throws MojoExecutionException {
    if (resolvedPomArtifact.getFile() == null) {
      throw new MojoExecutionException(
                                       format("There was a problem trying to resolve the artifact's file location for [%s], file was null",
                                              resolvedPomArtifact.toString()));
    }
    if (!resolvedPomArtifact.getFile().exists()) {
      throw new MojoExecutionException(
                                       format("There was a problem trying to resolve the artifact's file location for [%s], file [%s] doesn't exists",
                                              resolvedPomArtifact.toString(), resolvedPomArtifact.getFile().getAbsolutePath()));
    }
  }

}

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
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.mule.tools.maven.resolver.ExcludedDependenciesResolver;

public class ArtifactLocator {

  private Log log;
  private MavenProject project;
  private RepositorySystem repositorySystem;
  private ArtifactRepository localRepository;
  private DependencyTreeBuilder treeBuilder;
  private ArtifactFactory artifactFactory;
  private ArtifactMetadataSource artifactMetadataSource;
  private ArtifactCollector artifactCollector;
  private Set<String> exclusions;
  private MavenProjectBuilder mavenProjectBuilder;


  public ArtifactLocator(Log log, MavenProject project, ProjectBuilder projectBuilder,
                         RepositorySystem repositorySystem, ArtifactRepository localRepository,
                         ProjectBuildingRequest projectBuildingRequest, DependencyTreeBuilder treeBuilder,
                         ArtifactFactory artifactFactory,
                         ArtifactMetadataSource artifactMetadataSource, ArtifactCollector artifactCollector,
                         MavenProjectBuilder mavenProjectBuilder) {
    this.log = log;
    this.project = project;
    this.repositorySystem = repositorySystem;
    this.localRepository = localRepository;
    this.treeBuilder = treeBuilder;
    this.artifactFactory = artifactFactory;
    this.artifactMetadataSource = artifactMetadataSource;
    this.artifactCollector = artifactCollector;
    this.exclusions = new HashSet<>();
    this.mavenProjectBuilder = mavenProjectBuilder;
  }

  public Set<Artifact> getArtifacts() throws MojoExecutionException {
    Set<Artifact> artifacts = new HashSet<>(project.getArtifacts());
    for (Artifact dep : new ArrayList<>(artifacts)) {
      addThirdPartyParentPomArtifacts(artifacts, dep);
    }
    addParentPomArtifacts(artifacts);
    return artifacts;
  }

  protected void addThirdPartyParentPomArtifacts(Set<Artifact> artifacts, Artifact dep) throws MojoExecutionException {
    MavenProject project = mavenProjectBuilder.buildProjectFromArtifact(dep);
    addParentDependencyPomArtifacts(project, artifacts);

    Artifact pomArtifact = mavenProjectBuilder.createProjectArtifact(dep);
    artifacts.add(getResolvedArtifactUsingLocalRepository(pomArtifact));
  }

  protected void addParentPomArtifacts(Set<Artifact> artifacts) throws MojoExecutionException {
    MavenProject currentProject = project;
    boolean projectParent = true;
    addExclusions(currentProject);
    while (currentProject.hasParent() && projectParent) {
      currentProject = currentProject.getParent();
      addExclusions(currentProject);
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

  private void addExclusions(MavenProject project) {
    if (project != null) {
      for (Dependency dependency : project.getDependencies()) {
        for (Exclusion exclusion : dependency.getExclusions()) {
          exclusions.add(exclusion.getGroupId() + ":" + exclusion.getArtifactId());
        }
      }
    }
  }

  protected void addParentDependencyPomArtifacts(MavenProject projectDependency, Set<Artifact> artifacts)
      throws MojoExecutionException {
    MavenProject currentProject = projectDependency;
    addExclusions(currentProject);
    while (currentProject.hasParent()) {
      currentProject = currentProject.getParent();
      addExclusions(currentProject);
      final Artifact pomArtifact = currentProject.getArtifact();
      if (!artifacts.add(getResolvedArtifactUsingLocalRepository(pomArtifact))) {
        break;
      }
    }
  }

  protected Artifact getResolvedArtifactUsingLocalRepository(Artifact pomArtifact) throws MojoExecutionException {
    final Artifact resolvedPomArtifact = localRepository.find(pomArtifact);
    validatePomArtifactFile(resolvedPomArtifact);
    return resolvedPomArtifact;
  }

  protected void validatePomArtifactFile(Artifact resolvedPomArtifact) throws MojoExecutionException {
    if (resolvedPomArtifact.getFile() == null) {
      throw new MojoExecutionException(
                                       format("There was a problem trying to resolve the artifact's file location for [%s], file was null",
                                              resolvedPomArtifact.toString()));
    }
    if (!resolvedPomArtifact.getFile().exists()) {
      throw new MojoExecutionException(
                                       format("There was a problem trying to resolve the artifact's file location for [%s], file [%s] doesn't exist",
                                              resolvedPomArtifact.toString(), resolvedPomArtifact.getFile().getAbsolutePath()));
    }
  }

  public Set<Artifact> getExclusions() throws DependencyTreeBuilderException, MojoExecutionException {
    ExcludedDependenciesResolver exclusionsResolver =
        new ExcludedDependenciesResolver(treeBuilder, localRepository, artifactFactory, artifactMetadataSource, artifactCollector,
                                         mavenProjectBuilder);
    return exclusionsResolver.resolve(project, exclusions);
  }


}

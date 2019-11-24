/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.util;

import static com.google.common.base.Preconditions.checkArgument;
import org.mule.tools.api.classloader.model.SharedLibraryDependency;
import org.mule.tools.api.classloader.model.resolver.Plugin;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.repository.RepositorySystem;

public class MavenComponents {

  private MavenProject project;
  private File outputDirectory;
  private MavenSession session;
  private List<SharedLibraryDependency> sharedLibraries;
  private ProjectBuilder projectBuilder;
  private RepositorySystem repositorySystem;
  private ArtifactRepository localRepository;
  private List<ArtifactRepository> remoteArtifactRepositories;
  private String classifier;
  private List<Plugin> additionalPluginDependencies;
  private File projectBaseFolder;
  private Log log;

  public MavenComponents(Log log, MavenProject project,
                         File outputDirectory, MavenSession session,
                         List<SharedLibraryDependency> sharedLibraries, ProjectBuilder projectBuilder,
                         RepositorySystem repositorySystem, ArtifactRepository localRepository,
                         List<ArtifactRepository> remoteArtifactRepositories, String classifier,
                         List<Plugin> additionalPluginDependencies, File projectBaseFolder) {

    checkArgument(log != null, "The log must not be null");
    checkArgument(project != null, "The project must not be null");
    checkArgument(outputDirectory != null, "The outputDirectory must not be null");
    checkArgument(session != null, "The session must not be null");
    checkArgument(sharedLibraries != null, "The sharedLibraries must not be null");
    checkArgument(projectBuilder != null, "The projectBuilder must not be null");
    checkArgument(repositorySystem != null, "The repositorySystem must not be null");
    checkArgument(localRepository != null, "The localRepository must not be null");
    checkArgument(remoteArtifactRepositories != null, "The remoteArtifactRepositories must not be null");
    checkArgument(projectBaseFolder != null, "The projectBaseFolder must not be null");

    this.log = log;
    this.project = project;
    this.outputDirectory = outputDirectory;
    this.session = session;
    this.sharedLibraries = sharedLibraries;
    this.projectBuilder = projectBuilder;
    this.repositorySystem = repositorySystem;
    this.localRepository = localRepository;
    this.remoteArtifactRepositories = remoteArtifactRepositories;
    this.classifier = classifier;
    this.additionalPluginDependencies = additionalPluginDependencies;
    this.projectBaseFolder = projectBaseFolder;
  }

  public MavenProject getProject() {
    return project;
  }

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public MavenSession getSession() {
    return session;
  }

  public List<SharedLibraryDependency> getSharedLibraries() {
    return sharedLibraries;
  }

  public ProjectBuilder getProjectBuilder() {
    return projectBuilder;
  }

  public RepositorySystem getRepositorySystem() {
    return repositorySystem;
  }

  public ArtifactRepository getLocalRepository() {
    return localRepository;
  }

  public List<ArtifactRepository> getRemoteArtifactRepositories() {
    return remoteArtifactRepositories;
  }

  public String getClassifier() {
    return classifier;
  }

  public List<Plugin> getAdditionalPluginDependencies() {
    return additionalPluginDependencies;
  }

  public File getProjectBaseFolder() {
    return projectBaseFolder;
  }

  public Log getLog() {
    return log;
  }
}

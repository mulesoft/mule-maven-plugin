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

  public MavenComponents withLog(Log log) {
    checkArgument(log != null, "The log must not be null");
    this.log = log;
    return this;
  }

  public MavenComponents withProject(MavenProject project) {
    checkArgument(project != null, "The project must not be null");
    this.project = project;
    return this;
  }

  public MavenComponents withOutputDirectory(File outputDirectory) {
    checkArgument(outputDirectory != null, "The outputDirectory must not be null");
    this.outputDirectory = outputDirectory;
    return this;
  }

  public MavenComponents withSession(MavenSession session) {
    checkArgument(session != null, "The session must not be null");
    this.session = session;
    return this;
  }

  public MavenComponents withSharedLibraries(List<SharedLibraryDependency> sharedLibraries) {
    checkArgument(sharedLibraries != null, "The sharedLibraries must not be null");
    this.sharedLibraries = sharedLibraries;
    return this;
  }

  public MavenComponents withProjectBuilder(ProjectBuilder projectBuilder) {
    checkArgument(projectBuilder != null, "The projectBuilder must not be null");
    this.projectBuilder = projectBuilder;
    return this;
  }

  public MavenComponents withRepositorySystem(RepositorySystem repositorySystem) {
    checkArgument(repositorySystem != null, "The repositorySystem must not be null");
    this.repositorySystem = repositorySystem;
    return this;
  }

  public MavenComponents withLocalRepository(ArtifactRepository localRepository) {
    checkArgument(localRepository != null, "The localRepository must not be null");
    this.localRepository = localRepository;
    return this;
  }

  public MavenComponents withRemoteArtifactRepositories(List<ArtifactRepository> remoteArtifactRepositories) {
    checkArgument(remoteArtifactRepositories != null, "The remoteArtifactRepositories must not be null");
    this.remoteArtifactRepositories = remoteArtifactRepositories;
    return this;
  }

  public MavenComponents withClassifier(String classifier) {
    this.classifier = classifier;
    return this;
  }

  public MavenComponents withAdditionalPluginDependencies(List<Plugin> additionalPluginDependencies) {
    this.additionalPluginDependencies = additionalPluginDependencies;
    return this;
  }

  public MavenComponents withProjectBaseFolder(File projectBaseFolder) {
    checkArgument(projectBaseFolder != null, "The projectBaseFolder must not be null");
    this.projectBaseFolder = projectBaseFolder;
    return this;
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

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
import static org.mule.tools.artifact.archiver.api.PackagerFolders.REPOSITORY;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;

public class RepositoryGenerator {

  private Log log;

  private MavenSession session;
  private MavenProject project;
  private ProjectBuilder projectBuilder;
  private RepositorySystem repositorySystem;
  private ArtifactRepository localRepository;
  private List<ArtifactRepository> remoteArtifactRepositories;

  protected File outputDirectory;
  private ProjectBuildingRequest projectBuildingRequest;

  public RepositoryGenerator(MavenSession session,
                             MavenProject project,
                             ProjectBuilder projectBuilder,
                             RepositorySystem repositorySystem,
                             ArtifactRepository localRepository,
                             List<ArtifactRepository> remoteArtifactRepositories,
                             File outputDirectory, Log log) {

    this.log = log;
    this.session = session;
    this.project = project;
    this.projectBuilder = projectBuilder;
    this.repositorySystem = repositorySystem;
    this.localRepository = localRepository;
    this.remoteArtifactRepositories = remoteArtifactRepositories;
    this.outputDirectory = outputDirectory;
  }


  public void generate() throws MojoExecutionException, MojoFailureException {
    log.info(format("Mirroring repository for [%s]", project.toString()));
    try {
      initializeProjectBuildingRequest();

      ArtifactLocator artifactLocator = buildArtifactLocator();
      Set<Artifact> artifacts = artifactLocator.getArtifacts();

      File repositoryFolder = getRepositoryFolder();
      ArtifactInstaller artifactInstaller = buildArtifactInstaller();
      installArtifacts(repositoryFolder, artifacts, artifactInstaller);
    } catch (Exception e) {
      log.debug(format("There was an exception while building [%s]", project.toString()), e);
      throw e;
    }
  }

  protected ArtifactInstaller buildArtifactInstaller() {
    return new ArtifactInstaller(log);
  }

  protected ArtifactLocator buildArtifactLocator() {
    return new ArtifactLocator(log, project, projectBuilder, repositorySystem, localRepository, projectBuildingRequest);
  }

  protected void initializeProjectBuildingRequest() {
    projectBuildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
    projectBuildingRequest.setLocalRepository(localRepository);
    projectBuildingRequest.setRemoteRepositories(remoteArtifactRepositories);


    log.debug(format("Local repository [%s]", projectBuildingRequest.getLocalRepository().getBasedir()));
    projectBuildingRequest.getRemoteRepositories()
        .forEach(artifactRepository -> log.debug(format("Remote repository ID [%s], URL [%s]", artifactRepository.getId(),
                                                        artifactRepository.getUrl())));
  }

  protected File getRepositoryFolder() {
    File repositoryFolder = new File(outputDirectory, REPOSITORY);
    if (!repositoryFolder.exists()) {
      repositoryFolder.mkdirs();
    }
    return repositoryFolder;
  }

  protected void installArtifacts(File repositoryFile, Set<Artifact> artifacts, ArtifactInstaller installer)
      throws MojoExecutionException {
    TreeSet<Artifact> sortedArtifacts = new TreeSet<>(artifacts);
    if (sortedArtifacts.isEmpty()) {
      generateMarkerFileInRepositoryFolder(repositoryFile);
    }
    for (Artifact artifact : sortedArtifacts) {
      installer.installArtifact(repositoryFile, artifact);
    }
  }

  protected void generateMarkerFileInRepositoryFolder(File repositoryFile) throws MojoExecutionException {
    File markerFile = new File(repositoryFile, ".marker");
    log.info(format("No artifacts to add, adding marker file <%s/%s>", REPOSITORY, markerFile.getName()));
    try {
      markerFile.createNewFile();
    } catch (IOException e) {
      throw new MojoExecutionException(
                                       format("The current repository has no artifacts to install, and trying to create [%s] failed",
                                              markerFile.toString()),
                                       e);
    }
  }
}

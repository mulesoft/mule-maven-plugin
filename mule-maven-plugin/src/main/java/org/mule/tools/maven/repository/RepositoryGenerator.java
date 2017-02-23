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
import static java.util.Collections.sort;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

  public static final String REPOSITORY_FOLDER = "repository";

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

      ArtifactLocator artifactLocator =
          new ArtifactLocator(log, project, projectBuilder, repositorySystem, localRepository, projectBuildingRequest);
      Set<Artifact> artifacts = artifactLocator.getArtifacts();

      File repositoryFolder = getRepositoryFolder();

      installArtifacts(repositoryFolder, artifacts);
    } catch (Exception e) {
      log.debug(format("There was an exception while building [%s]", project.toString()), e);
      throw e;
    }
  }

  private void initializeProjectBuildingRequest() {
    projectBuildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
    projectBuildingRequest.setLocalRepository(localRepository);
    projectBuildingRequest.setRemoteRepositories(remoteArtifactRepositories);


    log.debug(format("Local repository [%s]", projectBuildingRequest.getLocalRepository().getBasedir()));
    projectBuildingRequest.getRemoteRepositories()
        .forEach(artifactRepository -> log.debug(format("Remote repository ID [%s], URL [%s]", artifactRepository.getId(),
                                                        artifactRepository.getUrl())));
  }

  private File getRepositoryFolder() {
    File repositoryFolder = new File(outputDirectory, REPOSITORY_FOLDER);
    if (!repositoryFolder.exists()) {
      repositoryFolder.mkdirs();
    }
    return repositoryFolder;
  }

  private void installArtifacts(File repositoryFile, Set<Artifact> artifacts) throws MojoExecutionException {
    List<Artifact> sortedArtifacts = new ArrayList<>(artifacts);
    sort(sortedArtifacts);
    if (sortedArtifacts.isEmpty()) {
      generateMarkerFileInRepositoryFolder(repositoryFile);
    }

    ArtifactInstaller installer = new ArtifactInstaller(log);
    for (Artifact artifact : sortedArtifacts) {
      installer.installArtifact(repositoryFile, artifact);
    }
  }

  private void generateMarkerFileInRepositoryFolder(File repositoryFile)
      throws MojoExecutionException {
    File markerFile = new File(repositoryFile, ".marker");
    log.info(format("No artifacts to add, adding marker file <%s/%s>", REPOSITORY_FOLDER, markerFile.getName()));
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

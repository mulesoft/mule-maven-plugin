/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.repository;

import static java.lang.String.format;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.isValidMulePlugin;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toArtifactCoordinates;
import static org.mule.tools.api.packager.structure.PackagerFolders.REPOSITORY;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.repository.RemoteRepository;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.ApplicationClassloaderModel;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.ClassLoaderModel;
import org.mule.tools.api.classloader.model.ApplicationClassLoaderModelAssembler;
import org.mule.tools.api.util.FileUtils;

public class RepositoryGenerator {

  private Log log;

  private MavenProject project;
  private List<ArtifactRepository> remoteArtifactRepositories;

  protected File outputDirectory;

  public RepositoryGenerator(MavenProject project, List<ArtifactRepository> remoteArtifactRepositories, File outputDirectory,
                             Log log) {
    this.log = log;
    this.project = project;
    this.remoteArtifactRepositories = remoteArtifactRepositories;
    this.outputDirectory = outputDirectory;
  }


  public ClassLoaderModel generate() throws MojoExecutionException, MojoFailureException {
    log.info(format("Mirroring repository for [%s]", project.toString()));
    ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssembler = buildClassLoaderModelAssembler();
    File pomFile = project.getFile();
    ApplicationClassloaderModel appModel =
        applicationClassLoaderModelAssembler.getApplicationClassLoaderModel(pomFile, outputDirectory);
    Set<Artifact> artifacts = appModel.getArtifacts();
    File repositoryFolder = getRepositoryFolder();
    ArtifactInstaller artifactInstaller = buildArtifactInstaller();
    installArtifacts(repositoryFolder, artifacts, artifactInstaller, appModel);
    return appModel.getClassLoaderModel();
  }

  protected ArtifactInstaller buildArtifactInstaller() {
    return new ArtifactInstaller(log);
  }

  protected ApplicationClassLoaderModelAssembler buildClassLoaderModelAssembler() {
    List<RemoteRepository> remoteRepositories = RepositoryUtils.toRepos(remoteArtifactRepositories);
    return new ApplicationClassLoaderModelAssembler((AetherMavenClient) new MuleMavenPluginClientProvider(remoteRepositories, log)
        .buildMavenClient());
  }


  protected File getRepositoryFolder() {
    File repositoryFolder = new File(outputDirectory, REPOSITORY);
    if (!repositoryFolder.exists()) {
      repositoryFolder.mkdirs();
    }
    return repositoryFolder;
  }

  protected void installArtifacts(File repositoryFile, Set<Artifact> artifacts, ArtifactInstaller installer, ApplicationClassloaderModel appModel)
      throws MojoExecutionException {
    Map<ArtifactCoordinates, ClassLoaderModel> mulePluginsClassloaderModels = appModel.getMulePluginsClassloaderModels().stream().collect(Collectors.toMap(ClassLoaderModel::getArtifactCoordinates, Function.identity()));
    TreeSet<Artifact> sortedArtifacts = new TreeSet<>(artifacts);
    if (sortedArtifacts.isEmpty()) {
      generateMarkerFileInRepositoryFolder(repositoryFile);
    }
    for (Artifact artifact : sortedArtifacts) {
      ClassLoaderModel mulePluginClassloader = null;
      if(isValidMulePlugin(artifact)) {
        mulePluginClassloader = mulePluginsClassloaderModels.get(toArtifactCoordinates(artifact));
      }
      installer.installArtifact(repositoryFile, artifact, Optional.ofNullable(mulePluginClassloader));
    }
  }

  protected void generateMarkerFileInRepositoryFolder(File repositoryFile) throws MojoExecutionException {
    File markerFile = new File(repositoryFile, ".marker");
    log.info(format("No artifacts to add, adding marker file <%s/%s>", REPOSITORY, markerFile.getName()));
    try {
      FileUtils.checkReadOnly(repositoryFile);
      markerFile.createNewFile();
    } catch (IOException e) {
      throw new MojoExecutionException(format("The current repository has no artifacts to install, and trying to create [%s] failed",
                                              markerFile.toString()),
                                       e);
    }
  }
}

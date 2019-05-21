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
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_PLUGIN;
import static org.mule.tools.api.packager.structure.FolderNames.REPOSITORY;

import org.mule.tools.api.classloader.model.ApplicationClassLoaderModelAssembler;
import org.mule.tools.api.classloader.model.ApplicationClassloaderModel;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ClassLoaderModel;
import org.mule.tools.api.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

public class RepositoryGenerator {

  private static final String PROVIDED_SCOPE = "provided";
  private final ArtifactInstaller artifactInstaller;
  private final ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssembler;
  protected File outputDirectory;
  private File projectPomFile;
  private boolean legacyMode;

  public RepositoryGenerator(File projectPomFile, File outputDirectory, ArtifactInstaller artifactInstaller,
                             ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssembler, boolean legacyMode) {
    this.projectPomFile = projectPomFile;
    this.outputDirectory = outputDirectory;
    this.artifactInstaller = artifactInstaller;
    this.applicationClassLoaderModelAssembler = applicationClassLoaderModelAssembler;
    this.legacyMode = legacyMode;
  }

  public ClassLoaderModel generate() throws IOException, IllegalStateException {
    ApplicationClassloaderModel appModel =
        applicationClassLoaderModelAssembler
            .getApplicationClassLoaderModel(projectPomFile, legacyMode);
    installArtifacts(getRepositoryFolder(), artifactInstaller, appModel, legacyMode);
    return appModel.getClassLoaderModel();
  }

  public ClassLoaderModel generate(boolean installArtifactsToArtifactRepository) throws IOException, IllegalStateException {
    ApplicationClassloaderModel appModel =
        applicationClassLoaderModelAssembler.getApplicationClassLoaderModel(projectPomFile, legacyMode);
    if (installArtifactsToArtifactRepository) {
      installArtifacts(getRepositoryFolder(), artifactInstaller, appModel, legacyMode);
    }
    return appModel.getClassLoaderModel();
  }

  protected File getRepositoryFolder() {
    File repositoryFolder = new File(outputDirectory, REPOSITORY.value());
    if (!repositoryFolder.exists()) {
      repositoryFolder.mkdirs();
    }
    return repositoryFolder;
  }

  protected void installArtifacts(File repositoryFile, ArtifactInstaller installer, ApplicationClassloaderModel appModel,
                                  boolean legacyMode)
      throws IOException {
    TreeSet<Artifact> sortedArtifacts = new TreeSet<>(removeProvidedArtifacts(appModel.getArtifacts()));
    if (sortedArtifacts.isEmpty()) {
      generateMarkerFileInRepositoryFolder(repositoryFile);
    }
    for (Artifact artifact : sortedArtifacts) {
      Optional<ClassLoaderModel> classLoaderModelOptional = empty();
      if (legacyMode && MULE_PLUGIN.equals(artifact.getArtifactCoordinates().getClassifier())) {
        classLoaderModelOptional = of(appModel.getClassLoaderModel(artifact));
      }
      installer.installArtifact(repositoryFile, artifact, classLoaderModelOptional);
    }
  }

  private List<Artifact> removeProvidedArtifacts(List<Artifact> artifacts) {
    return artifacts.stream()
        .filter(artifact -> !StringUtils.equals(artifact.getArtifactCoordinates().getScope(), PROVIDED_SCOPE))
        .collect(toList());
  }

  protected void generateMarkerFileInRepositoryFolder(File repositoryFile) throws IOException {
    File markerFile = new File(repositoryFile, ".marker");
    try {
      FileUtils.checkReadOnly(repositoryFile);
      markerFile.createNewFile();
    } catch (IOException e) {
      throw new IOException(format("The current repository has no artifacts to install, and trying to create [%s] failed",
                                   markerFile.toString()),
                            e);
    }
  }
}

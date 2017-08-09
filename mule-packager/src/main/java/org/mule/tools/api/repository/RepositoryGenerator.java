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
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toArtifactCoordinates;
import static org.mule.tools.api.packager.structure.PackagerFolders.REPOSITORY;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.mule.tools.api.classloader.model.ApplicationClassloaderModel;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.ClassLoaderModel;
import org.mule.tools.api.classloader.model.ApplicationClassLoaderModelAssembler;
import org.mule.tools.api.util.FileUtils;

public class RepositoryGenerator {

  private final ArtifactInstaller artifactInstaller;
  private final ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssembler;
  protected File outputDirectory;
  private File projectPomFile;

  public RepositoryGenerator(File projectPomFile, File outputDirectory, ArtifactInstaller artifactInstaller,
                             ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssembler) {
    this.projectPomFile = projectPomFile;
    this.outputDirectory = outputDirectory;
    this.artifactInstaller = artifactInstaller;
    this.applicationClassLoaderModelAssembler = applicationClassLoaderModelAssembler;
  }

  public ClassLoaderModel generate() throws IOException, IllegalStateException {
    ApplicationClassloaderModel appModel =
        applicationClassLoaderModelAssembler.getApplicationClassLoaderModel(projectPomFile, outputDirectory);
    installArtifacts(getRepositoryFolder(), artifactInstaller, appModel);
    return appModel.getClassLoaderModel();
  }

  protected File getRepositoryFolder() {
    File repositoryFolder = new File(outputDirectory, REPOSITORY);
    if (!repositoryFolder.exists()) {
      repositoryFolder.mkdirs();
    }
    return repositoryFolder;
  }

  protected void installArtifacts(File repositoryFile, ArtifactInstaller installer, ApplicationClassloaderModel appModel)
      throws IOException {
    Map<ArtifactCoordinates, ClassLoaderModel> mulePluginsClassloaderModels = appModel.getMulePluginsClassloaderModels().stream()
        .collect(Collectors.toMap(ClassLoaderModel::getArtifactCoordinates, Function.identity()));
    TreeSet<Artifact> sortedArtifacts = new TreeSet<>(appModel.getArtifacts());
    if (sortedArtifacts.isEmpty()) {
      generateMarkerFileInRepositoryFolder(repositoryFile);
    }
    for (Artifact artifact : sortedArtifacts) {
      Optional<ClassLoaderModel> mulePluginClassloaderOptional =
          Optional.ofNullable(mulePluginsClassloaderModels.get(toArtifactCoordinates(artifact)));
      installer.installArtifact(repositoryFile, artifact, mulePluginClassloaderOptional);
    }
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

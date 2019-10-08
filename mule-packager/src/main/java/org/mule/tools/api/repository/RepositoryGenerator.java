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
import static org.mule.tools.api.packager.sources.MuleContentGenerator.createClassLoaderModelJsonFile;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.FolderNames.REPOSITORY;

import org.mule.tools.api.classloader.model.ApplicationClassLoaderModelAssembler;
import org.mule.tools.api.classloader.model.ApplicationClassloaderModel;
import org.mule.tools.api.classloader.model.ApplicationGAVModel;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.ClassLoaderModel;
import org.mule.tools.api.classloader.model.NotParameterizedClassLoaderModel;
import org.mule.tools.api.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class RepositoryGenerator {

  private static final String PROVIDED_SCOPE = "provided";
  private final ArtifactInstaller artifactInstaller;
  private final ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssembler;
  protected File outputDirectory;
  private File projectPomFile;
  private ApplicationGAVModel appGAVModel;

  public RepositoryGenerator(File projectPomFile, File outputDirectory, ArtifactInstaller artifactInstaller,
                             ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssembler,
                             ApplicationGAVModel appGAVModel) {
    this.projectPomFile = projectPomFile;
    this.outputDirectory = outputDirectory;
    this.artifactInstaller = artifactInstaller;
    this.applicationClassLoaderModelAssembler = applicationClassLoaderModelAssembler;
    this.appGAVModel = appGAVModel;
  }

  @Deprecated
  public ClassLoaderModel generate() throws IOException, IllegalStateException {
    ApplicationClassloaderModel appModel =
        applicationClassLoaderModelAssembler.getApplicationClassLoaderModel(projectPomFile, outputDirectory, appGAVModel, false);
    installArtifacts(getRepositoryFolder(), artifactInstaller, appModel, false);
    return appModel.getClassLoaderModel();
  }

  public ClassLoaderModel generate(boolean lightweight, boolean useLocalRepository, boolean prettyPrinting,
                                   boolean includeTestDependencies)
      throws IOException, IllegalStateException {
    ApplicationClassloaderModel appModel =
        applicationClassLoaderModelAssembler.getApplicationClassLoaderModel(projectPomFile, outputDirectory, appGAVModel,
                                                                            includeTestDependencies);
    if (!lightweight) {
      installArtifacts(getRepositoryFolder(), artifactInstaller, appModel, prettyPrinting);
    }
    if (useLocalRepository) {
      generateClassLoaderModelRepositoryFiles(appModel, prettyPrinting);
    }
    return appModel.getClassLoaderModel();
  }

  private void generateClassLoaderModelRepositoryFiles(ApplicationClassloaderModel appModel, boolean prettyPrinting) {
    appModel.getMulePluginsClassloaderModels().stream().forEach(mulePluginClassLoaderModel -> {
      Artifact artifact = appModel.getArtifacts().stream()
          .filter(possibleArtifact -> possibleArtifact.getArtifactCoordinates()
              .equals(mulePluginClassLoaderModel.getArtifactCoordinates()))
          .findFirst()
          .orElseThrow(() -> new RuntimeException(format("Cannot resolve artifact folder for class loader model: [%s]",
                                                         mulePluginClassLoaderModel.getArtifactCoordinates())));
      File artifactFolderDestination = artifact.getFormattedMavenDirectory(outputDirectory.toPath().resolve(META_INF.value())
          .resolve(MULE_ARTIFACT.value()).toFile());
      if (!artifactFolderDestination.exists()) {
        artifactFolderDestination.mkdirs();
      }
      createClassLoaderModelJsonFile(new NotParameterizedClassLoaderModel(mulePluginClassLoaderModel),
                                     artifactFolderDestination, prettyPrinting);
    });

  }

  @Deprecated
  public ClassLoaderModel generate(boolean lightweight) throws IOException, IllegalStateException {
    return generate(lightweight, false, false, false);
  }

  protected File getRepositoryFolder() {
    File repositoryFolder = new File(outputDirectory, REPOSITORY.value());
    if (!repositoryFolder.exists()) {
      repositoryFolder.mkdirs();
    }
    return repositoryFolder;
  }

  protected void installArtifacts(File repositoryFile, ArtifactInstaller installer, ApplicationClassloaderModel appModel,
                                  boolean prettyPrinting)
      throws IOException {
    Map<ArtifactCoordinates, ClassLoaderModel> mulePluginsClassloaderModels = appModel.getMulePluginsClassloaderModels().stream()
        .collect(Collectors.toMap(ClassLoaderModel::getArtifactCoordinates, Function.identity()));
    TreeSet<Artifact> sortedArtifacts = new TreeSet<>(removeProvidedArtifacts(appModel.getArtifacts()));
    if (sortedArtifacts.isEmpty()) {
      generateMarkerFileInRepositoryFolder(repositoryFile);
    }
    for (Artifact artifact : sortedArtifacts) {
      Optional<ClassLoaderModel> classLoaderModelOptional =
          Optional.ofNullable(mulePluginsClassloaderModels.get(artifact.getArtifactCoordinates()));
      installer.installArtifact(repositoryFile, artifact, classLoaderModelOptional, prettyPrinting);
    }
  }

  private Set<Artifact> removeProvidedArtifacts(Set<Artifact> artifacts) {
    return artifacts.stream()
        .filter(artifact -> !StringUtils.equals(artifact.getArtifactCoordinates().getScope(), PROVIDED_SCOPE))
        .collect(Collectors.toSet());
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

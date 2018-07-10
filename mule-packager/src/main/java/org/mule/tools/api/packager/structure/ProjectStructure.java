/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.structure;

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.tools.api.packager.structure.FolderNames.CLASSES;
import static org.mule.tools.api.packager.structure.FolderNames.JAVA;
import static org.mule.tools.api.packager.structure.FolderNames.MAIN;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.FolderNames.MULE;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.FolderNames.MUNIT;
import static org.mule.tools.api.packager.structure.FolderNames.RESOURCES;
import static org.mule.tools.api.packager.structure.FolderNames.SRC;
import static org.mule.tools.api.packager.structure.FolderNames.TARGET;
import static org.mule.tools.api.packager.structure.FolderNames.TEST;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class ProjectStructure {

  private final Path projectBaseFolder;
  private final Path projectBuildDirectory;
  private final boolean includeTestSuites;

  public ProjectStructure(Path projectBaseFolder, boolean includeTestSuites) {
    this(projectBaseFolder, Paths.get(TARGET.value()), includeTestSuites);
  }

  public ProjectStructure(Path projectBaseFolder, Path projectBuildFolder, boolean includeTestSuites) {
    checkArgument(projectBaseFolder != null, "Project base folder should not be null");
    checkArgument(projectBuildFolder != null, "Project build folder should not be null");
    this.projectBaseFolder = projectBaseFolder;
    this.projectBuildDirectory = projectBuildFolder;
    this.includeTestSuites = includeTestSuites;
  }

  public Path getProjectBaseFolder() {
    return projectBaseFolder;
  }

  /**
   * Resolves the exported packages path based on the project base folder
   */
  public Path getExportedPackagesPath() {
    return projectBaseFolder.resolve(SRC.value()).resolve(MAIN.value()).resolve(JAVA.value());
  }

  /**
   * Resolves the configs path based on the project base folder
   */
  public Path getConfigsPath() {
    return projectBaseFolder.resolve(SRC.value()).resolve(MAIN.value()).resolve(MULE.value());
  }

  /**
   * Resolves the test configs path based on the project base folder
   */
  public Optional<Path> getTestConfigsPath() {
    return includeTestSuites ? Optional.of(projectBaseFolder.resolve(SRC.value()).resolve(TEST.value()).resolve(MUNIT.value()))
        : Optional.empty();
  }

  /**
   * Resolves the test exported resources path based on the project base folder
   */
  public Optional<Path> getTestExportedResourcesPath() {
    return includeTestSuites
        ? Optional.of(projectBaseFolder.resolve(SRC.value()).resolve(TEST.value()).resolve(RESOURCES.value()))
        : Optional.empty();
  }

  public Path getMuleArtifactJsonPath() {
    return projectBuildDirectory.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value());
  }

  public Path getOutputDirectory() {
    return projectBuildDirectory.resolve(CLASSES.value());
  }
}

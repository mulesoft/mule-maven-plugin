/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.structure;

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.tools.api.packager.structure.FolderNames.*;
import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_DEPLOY_PROPERTIES;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ProjectStructure {

  private final Path projectBaseFolder;
  private final Path projectBuildDirectory;

  public ProjectStructure(Path projectBaseFolder) {
    this(projectBaseFolder, Paths.get(TARGET.value()));
  }

  public ProjectStructure(Path projectBaseFolder, Path projectBuildFolder) {
    checkArgument(projectBaseFolder != null, "Project base folder should not be null");
    checkArgument(projectBuildFolder != null, "Project build folder should not be null");
    this.projectBaseFolder = projectBaseFolder;
    this.projectBuildDirectory = projectBuildFolder;
  }

  public Path getProjectBaseFolder() {
    return projectBaseFolder;
  }

  /**
   * Resolves the resources path based on the project base folder
   */
  public Path getResourcesPath() {
    return projectBaseFolder.resolve(SRC.value()).resolve(MAIN.value()).resolve(RESOURCES.value());
  }

  /**
   * Resolves the configs path based on the project base folder
   */
  public Path getConfigsPath() {
    return projectBaseFolder.resolve(SRC.value()).resolve(MAIN.value()).resolve(APP.value());
  }

  /**
   * Resolves the test configs path based on the project base folder
   */
  public Path getTestConfigsPath() {
    return projectBaseFolder.resolve(SRC.value()).resolve(TEST.value()).resolve(MUNIT.value());
  }

  /**
   * Resolves the test resources path based on the project base folder
   */
  public Path getTestResourcesPath() {
    return projectBaseFolder.resolve(SRC.value()).resolve(TEST.value()).resolve(RESOURCES.value());
  }

  public Path getMuleDeployPropertiesPath() {
    return getConfigsPath().resolve(MULE_DEPLOY_PROPERTIES);
  }

  public Path getProjectBuildDirectory() {
    return projectBuildDirectory;
  }

  public Path getApiFolder() {
    return projectBaseFolder.resolve(SRC.value()).resolve(MAIN.value()).resolve(API.value());
  }

  public Path getWsdlFolder() {
    return projectBaseFolder.resolve(SRC.value()).resolve(MAIN.value()).resolve(WSDL.value());
  }

  public Path getMappingsFolder() {
    return projectBaseFolder.resolve(MAPPINGS.value());
  }
}

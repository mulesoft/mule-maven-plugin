package org.mule.tools.api.packager.structure;

import java.nio.file.Path;
/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.tools.api.packager.structure.FolderNames.*;

public class ProjectStructure {

  private final Path projectBaseFolder;
  private final boolean includeTestSuites;

  public ProjectStructure(Path projectBaseFolder, boolean includeTestSuites) {
    checkArgument(projectBaseFolder != null, "Project base folder should not be null");
    this.projectBaseFolder = projectBaseFolder;
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
   * Resolves the exported resources path based on the project base folder
   */
  public Path getExportedResourcesPath() {
    return projectBaseFolder.resolve(SRC.value()).resolve(MAIN.value()).resolve(RESOURCES.value());
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

  public Path getMuleArtifactJsonPath() {
    return projectBaseFolder.resolve(TARGET.value()).resolve(META_INF.value()).resolve(MULE_ARTIFACT.value());
  }
}

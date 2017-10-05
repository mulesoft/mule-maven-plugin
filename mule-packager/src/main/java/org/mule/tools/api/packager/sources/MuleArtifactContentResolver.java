/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.sources;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.mule.tools.api.packager.structure.ProjectStructure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.tools.api.packager.structure.FolderNames.*;

/**
 * Resolves the content of resources defined in mule-artifact.json based on the project base folder.
 */
public class MuleArtifactContentResolver {

  private final ProjectStructure projectStructure;
  private List<String> configs;
  private List<String> testConfigs;
  private List<String> exportedPackages;
  private List<String> exportedResources;

  public MuleArtifactContentResolver(ProjectStructure projectStructure) {
    checkArgument(projectStructure != null, "Project structure should not be null");
    this.projectStructure = projectStructure;
  }

  /**
   * Returns the resolved list of exported packages paths.
   */
  public ProjectStructure getProjectStructure() throws IOException {
    return projectStructure;
  }


  /**
   * Returns the resolved list of exported packages paths.
   */
  public List<String> getExportedPackages() throws IOException {
    if (exportedPackages == null) {
      exportedPackages = getResources(projectStructure.getExportedPackagesPath());
    }
    return exportedPackages;
  }

  /**
   * Returns the resolved list of exported resources paths.
   */
  public List<String> getExportedResources() throws IOException {
    if (exportedResources == null) {
      exportedResources = getResources(projectStructure.getExportedResourcesPath());
    }
    return exportedResources;
  }

  /**
   * Returns the resolved list of configs paths.
   */
  public List<String> getConfigs() throws IOException {
    if (configs == null) {
      configs = getResources(projectStructure.getConfigsPath());
    }
    return configs;
  }

  /**
   * Returns the resolved list of test configs paths.
   */
  public List<String> getTestConfigs() throws IOException {
    if (testConfigs == null) {
      Optional<Path> testConfigsPath = projectStructure.getTestConfigsPath();
      testConfigs = testConfigsPath.isPresent() ? getResources(testConfigsPath.get()) : Collections.emptyList();
    }
    return testConfigs;
  }

  /**
   * Returns a list of resources within a given path.
   *
   * @param path base path of resources that are going to be listed.
   */
  private List<String> getResources(Path path) throws IOException {
    File resourcesFolder = path.toFile();
    if (resourcesFolder == null) {
      throw new IOException("The resources folder is invalid");
    }
    if (!resourcesFolder.exists()) {
      return Collections.emptyList();
    }
    Collection<File> resourcesFolderContent =
        FileUtils.listFiles(path.toFile(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

    return resourcesFolderContent.stream().map(File::toPath).map(p -> resourcesFolder.toPath().relativize(p)).map(Path::toString)
        .collect(Collectors.toList());
  }
}

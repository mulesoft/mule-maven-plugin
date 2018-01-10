/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.tools.api.packager.structure.FolderNames.CLASSES;
import static org.mule.tools.api.packager.structure.FolderNames.SITE;
import static org.mule.tools.api.packager.structure.FolderNames.TEST_CLASSES;

import java.nio.file.Paths;

import org.apache.maven.project.MavenProject;

/**
 * Is knows how to properly update an redirect folders that are related in a {@link MavenProject}
 * 
 * @author Mulesoft Inc.
 * @since 3.1.0
 */
public class ProjectDirectoryUpdater {

  private final MavenProject project;

  public ProjectDirectoryUpdater(MavenProject project) {
    checkArgument(project != null, "The project must not be null");
    this.project = project;
  }

  public void updateBuildDirectory(String buildDirectory) {
    checkArgument(buildDirectory != null, "The project build directory must not be null");

    project.getBuild().setDirectory(buildDirectory);
    updateBuildOutputDirectory(buildDirectory);
    updateBuildTestOputputDirectory(buildDirectory);
    updateReportingOutputDirectory(buildDirectory);
  }

  public void updateBuildOutputDirectory(String buildDirectory) {
    checkArgument(buildDirectory != null, "The project build directory must not be null");
    project.getModel().getBuild().setOutputDirectory(Paths.get(buildDirectory, CLASSES.value()).toString());
  }

  public void updateBuildTestOputputDirectory(String buildDirectory) {
    checkArgument(buildDirectory != null, "The project build directory must not be null");
    project.getModel().getBuild().setTestOutputDirectory(Paths.get(buildDirectory, TEST_CLASSES.value()).toString());
  }

  public void updateReportingOutputDirectory(String buildDirectory) {
    checkArgument(buildDirectory != null, "The project build directory must not be null");
    project.getModel().getReporting().setOutputDirectory(Paths.get(buildDirectory, SITE.value()).toString());
  }


}

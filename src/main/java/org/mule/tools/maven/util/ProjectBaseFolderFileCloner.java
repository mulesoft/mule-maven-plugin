/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.maven.project.MavenProject;

public class ProjectBaseFolderFileCloner {

  private Path sourceFilePath;
  private MavenProject project;
  private File projectBaseFolder;

  public ProjectBaseFolderFileCloner(MavenProject project) {
    this.project = project;
    this.projectBaseFolder = project.getBasedir();
  }

  public ProjectBaseFolderFileCloner clone(String descriptorName) throws IOException {
    sourceFilePath = new File(projectBaseFolder.getCanonicalPath() + File.separator + descriptorName).toPath();
    return this;
  }

  public void toPath(String... destinationPath) throws IOException {
    File targetFolder = Paths.get(project.getBuild().getDirectory(), destinationPath).toFile();
    Path targetFilePath = new File(targetFolder.toPath().toString(), sourceFilePath.toFile().getName()).toPath();
    Files.copy(sourceFilePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
  }
}

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

import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.packaging.PackagingType;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.tools.api.packager.structure.FolderNames.MAVEN;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.PackagerFiles.POM_PROPERTIES;
import static org.mule.tools.api.packager.structure.PackagerFiles.POM_XML;

/**
 * Generates the required content for each of the mandatory folders of a mule package
 */
public abstract class ContentGenerator {

  protected final ProjectInformation projectInformation;

  public ContentGenerator(ProjectInformation projectInformation) {
    checkArgument(projectInformation.getProjectBaseFolder().toFile().exists(), "Project base folder should exist");
    checkArgument(projectInformation.getBuildDirectory().toFile().exists(), "Project build folder should exist");
    this.projectInformation = projectInformation;
  }

  /**
   * It create all the package content in the required folders
   *
   * @throws IOException
   */
  public abstract void createContent() throws IOException;

  protected void copyPomFile() throws IOException {
    Path originPath = projectInformation.getProjectBaseFolder().resolve(POM_XML);
    Path destinationPath =
        projectInformation.getBuildDirectory().resolve(META_INF.value()).resolve(MAVEN.value())
            .resolve(projectInformation.getGroupId()).resolve(projectInformation.getArtifactId());
    String destinationFileName = originPath.getFileName().toString();

    copyFile(originPath, destinationPath, destinationFileName);
  }

  public static void checkPathExist(Path path) {
    checkArgument(path.toFile().exists(), "The path: " + path.toString() + " should exits");
  }

  public static void copyFile(Path originPath, Path destinationPath, String destinationFileName) throws IOException {
    checkPathExist(originPath);
    checkPathExist(destinationPath);
    Files.copy(originPath, destinationPath.resolve(destinationFileName), StandardCopyOption.REPLACE_EXISTING);
  }

  protected void createPomProperties() {
    Path pomPropertiesDestinationPath =
        projectInformation.getBuildDirectory().resolve(META_INF.value()).resolve(MAVEN.value())
            .resolve(projectInformation.getGroupId()).resolve(projectInformation.getArtifactId());
    checkPathExist(pomPropertiesDestinationPath);

    Path pomPropertiesFilePath = pomPropertiesDestinationPath.resolve(POM_PROPERTIES);
    try {
      PrintWriter writer = new PrintWriter(pomPropertiesFilePath.toString(), "UTF-8");
      writer.println("version=" + projectInformation.getVersion());
      writer.println("groupId=" + projectInformation.getGroupId());
      writer.println("artifactId=" + projectInformation.getArtifactId());
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException("Could not create pom.properties", e);
    }
  }

  protected void createMavenDescriptors() throws IOException {
    copyPomFile();
    createPomProperties();
  }
}

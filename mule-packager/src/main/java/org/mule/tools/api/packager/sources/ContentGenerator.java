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

import org.apache.commons.lang3.StringUtils;
import org.mule.tools.api.packager.packaging.PackagingType;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.tools.api.packager.structure.FolderNames.MAVEN;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_ARTIFACT_JSON;
import static org.mule.tools.api.packager.structure.PackagerFiles.POM_PROPERTIES;
import static org.mule.tools.api.packager.structure.PackagerFiles.POM_XML;

public abstract class ContentGenerator {

  protected String groupId;
  protected String artifactId;
  protected String version;
  protected PackagingType packagingType;

  protected Path projectBaseFolder;
  protected Path projectTargetFolder;

  public ContentGenerator(String groupId, String artifactId, String version, PackagingType packagingType, Path projectBaseFolder,
                          Path projectTargetFolder) {
    checkArgument(StringUtils.isNotEmpty(groupId), "The groupId must not be null nor empty");
    checkArgument(StringUtils.isNotEmpty(artifactId), "The artifactId must not be null nor empty");
    checkArgument(StringUtils.isNotEmpty(version), "The version must not be null nor empty");

    checkArgument(packagingType != null, "The packagingType must not be null");

    checkPathExist(projectBaseFolder);
    checkPathExist(projectTargetFolder);

    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;

    this.packagingType = packagingType;

    this.projectBaseFolder = projectBaseFolder;
    this.projectTargetFolder = projectTargetFolder;
  }

  /**
   * It create all the package content in the required folders
   *
   * @throws IOException
   */
  public abstract void createContent() throws IOException;

  protected void copyPomFile() throws IOException {
    Path originPath = projectBaseFolder.resolve(POM_XML);
    Path destinationPath =
        projectTargetFolder.resolve(META_INF.value()).resolve(MAVEN.value()).resolve(groupId).resolve(artifactId);
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
        projectTargetFolder.resolve(META_INF.value()).resolve(MAVEN.value()).resolve(groupId).resolve(artifactId);
    checkPathExist(pomPropertiesDestinationPath);

    Path pomPropertiesFilePath = pomPropertiesDestinationPath.resolve(POM_PROPERTIES);
    try {
      PrintWriter writer = new PrintWriter(pomPropertiesFilePath.toString(), "UTF-8");
      writer.println("version=" + version);
      writer.println("groupId=" + groupId);
      writer.println("artifactId=" + artifactId);
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException("Could not create pom.properties", e);
    }
  }

  /**
   * It creates the descriptors files, pom.xml, pom.properties, and the mule-*.json file. The name of the the last one depends on
   * the {@link PackagingType}
   *
   * @throws IOException
   */
  public void createDescriptors() throws IOException {
    createMavenDescriptors();
    copyDescriptorFile();
  }

  protected void createMavenDescriptors() throws IOException {
    copyPomFile();
    createPomProperties();
  }

  private void copyDescriptorFile() throws IOException {
    Path originPath = projectBaseFolder.resolve(MULE_ARTIFACT_JSON);
    Path destinationPath = projectTargetFolder.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value());
    String destinationFileName = originPath.getFileName().toString();

    copyFile(originPath, destinationPath, destinationFileName);
  }

}

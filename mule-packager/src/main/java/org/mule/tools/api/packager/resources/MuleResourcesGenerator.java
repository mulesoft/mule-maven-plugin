/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.resources;

import org.apache.maven.plugin.MojoExecutionException;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.filter.DependenciesFilter;
import org.mule.tools.api.packager.packaging.Exclusion;
import org.mule.tools.api.packager.packaging.Inclusion;
import org.mule.tools.api.packager.structure.MuleExportProperties;
import org.mule.tools.api.util.Artifact;
import org.mule.tools.api.util.ZipArchiver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.mule.tools.api.packager.sources.ContentGenerator.copyFile;
import static org.mule.tools.api.packager.structure.FolderNames.LIB;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.MuleExportProperties.MULE_EXPORT_PROPERTIES_FILE;

public class MuleResourcesGenerator {

  public static final String[] BLACK_LIST = new String[] {".DS_STORE", "target"};

  private final DependenciesFilter dependenciesFilter;
  private final ProjectInformation projectInformation;
  private final Boolean attachMuleSources;

  public MuleResourcesGenerator(Set<Artifact> projectArtifacts, List<? extends Exclusion> excludes,
                                List<? extends Inclusion> includes,
                                boolean excludeMuleArtifacts, ProjectInformation projectInformation, boolean attachMuleSources) {
    this.dependenciesFilter = new DependenciesFilter(projectArtifacts, includes, excludes, excludeMuleArtifacts);
    this.projectInformation = projectInformation;
    this.attachMuleSources = attachMuleSources;
  }

  public void generate(boolean prependGroupId) throws IOException {
    Path destinationPath = projectInformation.getBuildDirectory().resolve(LIB.value());
    for (Artifact artifact : dependenciesFilter.getArtifactsToArchive()) {
      copyArtifact(artifact, destinationPath, prependGroupId);
    }

    if (attachMuleSources) {
      File metaInfFolder = createMetaInfFolderInTarget();

      File projectBaseFolder = projectInformation.getProjectBaseFolder().toFile();
      generateExportedZipFile(metaInfFolder, projectBaseFolder);

      String projectName = projectBaseFolder.getName();
      generateMuleExportProperties(metaInfFolder, projectName);
    }
  }

  private void copyArtifact(Artifact artifact, Path destinationPath, boolean prependGroupId) throws IOException {
    Path originPath = artifact.getFile().toPath();
    String filename = filenameInArchive(artifact, prependGroupId);
    copyFile(originPath, destinationPath, filename);
  }

  private String filenameInArchive(Artifact artifact, boolean prependGroupId) {
    StringBuilder buf = new StringBuilder();
    if (prependGroupId) {
      buf.append(artifact.getGroupId());
      buf.append(".");
    }
    buf.append(artifact.getFile().getName());
    return buf.toString();
  }

  /**
   * Creates the META-INF folder in the target directory.
   *
   * @return the file representing META-INF
   */
  private File createMetaInfFolderInTarget() {
    File metaInfFolder = projectInformation.getBuildDirectory().resolve(META_INF.value()).toFile();
    metaInfFolder.mkdir();
    return metaInfFolder;
  }

  /**
   * Generates a zip file containing all the project content. The file is named as the projectBaseFolderName + .zip
   *
   * @param metaInfFolder
   * @param projectBaseFolder
   * @throws MojoExecutionException
   */
  protected void generateExportedZipFile(File metaInfFolder, File projectBaseFolder) throws IOException {
    ZipArchiver archiver = getZipArchiver();
    String output = metaInfFolder.getAbsolutePath() + File.separator + projectBaseFolder.getName() + ".zip";
    try {
      archiver.toZip(projectBaseFolder, output);
    } catch (IOException e) {
      throw new IOException("Could not create exportable zip file", e);
    }
  }

  /**
   * Generates the mule_export.properties file in META-INF
   *
   * @param metaInfFolder
   * @param projectName
   * @throws MojoExecutionException
   */
  protected void generateMuleExportProperties(File metaInfFolder, String projectName) throws IOException {
    MuleExportProperties exportProperties = new MuleExportProperties(projectName);
    try {
      exportProperties.store(metaInfFolder);
    } catch (IOException e) {
      throw new IOException("Could not create " + MULE_EXPORT_PROPERTIES_FILE, e);
    }
  }

  public ZipArchiver getZipArchiver() {
    return new ZipArchiver(BLACK_LIST);
  }
}

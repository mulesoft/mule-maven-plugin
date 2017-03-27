/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

import static org.mule.tools.artifact.archiver.api.PackagerFiles.*;
import static org.mule.tools.artifact.archiver.api.PackagerFolders.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.mule.tools.maven.mojo.model.PackagingType;
import org.mule.tools.maven.util.CopyFileVisitor;
import org.mule.tools.maven.util.ProjectBaseFolderFileCloner;

/**
 * Copy resource to the proper places
 */
@Mojo(name = "generate-sources",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GenerateSourcesMojo extends AbstractMuleMojo {

  protected ProjectBaseFolderFileCloner projectBaseFolderFileCloner;

  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().debug("Creating target content with Mule source code...");
    projectBaseFolderFileCloner = new ProjectBaseFolderFileCloner(project);
    try {
      createSrcFolderContent();
      createMetaInfMuleSourceFolderContent();
      createDescriptors();
    } catch (IOException e) {
      throw new MojoFailureException("Fail to generate sources", e);
    }
  }

  protected void createSrcFolderContent() throws IOException, MojoExecutionException {
    String srcFolderName = PackagingType.MULE_POLICY.equals(project.getPackaging()) ? POLICY : MULE;
    File targetFolder = Paths.get(project.getBuild().getDirectory(), srcFolderName).toFile();
    Files.walkFileTree(getSourceFolder().toPath(), new CopyFileVisitor(getSourceFolder(), targetFolder));
  }

  protected void createMetaInfMuleSourceFolderContent() throws IOException {
    File targetFolder = Paths.get(project.getBuild().getDirectory(), META_INF, MULE_SRC, project.getArtifactId()).toFile();
    CopyFileVisitor visitor = new CopyFileVisitor(projectBaseFolder, targetFolder);
    List<Path> exclusions = new ArrayList<>();
    exclusions.add(Paths.get(projectBaseFolder.toPath().toString(), TARGET));
    visitor.setExclusions(exclusions);
    Files.walkFileTree(projectBaseFolder.toPath(), visitor);
  }

  private void createDescriptors() throws IOException, MojoExecutionException {
    createPomProperties();
    createDescriptorFilesContent();
  }

  protected void createDescriptorFilesContent() throws IOException {
    projectBaseFolderFileCloner
        .clone(POM_XML).toPath(META_INF, MAVEN, project.getGroupId(), project.getArtifactId());

    String jsonDescriptorFileName =
        PackagingType.MULE_POLICY.equals(project.getPackaging()) ? MULE_POLICY_JSON : MULE_APPLICATION_JSON;
    projectBaseFolderFileCloner
        .clone(jsonDescriptorFileName).toPath(META_INF, MULE_ARTIFACT);
  }

  protected void createPomProperties() throws IOException, MojoExecutionException {
    Path pomPropertiesFilePath =
        Paths.get(project.getBuild().getDirectory(), META_INF, MAVEN, project.getGroupId(), project.getArtifactId(),
                  POM_PROPERTIES);
    try {
      PrintWriter writer = new PrintWriter(pomPropertiesFilePath.toString(), "UTF-8");
      writer.println("version=" + this.project.getVersion());
      writer.println("groupId=" + this.project.getGroupId());
      writer.println("artifactId=" + this.project.getArtifactId());
      writer.close();
    } catch (IOException e) {
      throw new MojoExecutionException("Could not create pom.properties", e);
    }
  }


}

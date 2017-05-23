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

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.eclipse.aether.RepositorySystemSession;
import org.mule.tools.maven.mojo.model.PackagingType;
import org.mule.tools.maven.mojo.model.SharedLibraryDependency;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mule.tools.artifact.archiver.api.PackagerFolders.MULE;
import static org.mule.tools.artifact.archiver.api.PackagerFolders.POLICY;


/**
 * Base Mojo
 */
public abstract class AbstractMuleMojo extends AbstractMojo {

  @Component
  protected ProjectBuilder projectBuilder;

  @Component
  protected RepositorySystem repositorySystem;

  @Parameter(readonly = true, required = true, defaultValue = "${session}")
  protected MavenSession session;

  @Parameter(readonly = true, required = true, defaultValue = "${localRepository}")
  protected ArtifactRepository localRepository;

  @Parameter(readonly = true, required = true, defaultValue = "${project.remoteArtifactRepositories}")
  protected List<ArtifactRepository> remoteArtifactRepositories;

  @Parameter(defaultValue = "${project.build.finalName}", required = true)
  protected String finalName;

  @Parameter(property = "project", required = true)
  protected MavenProject project;

  @Parameter(property = "project.build.directory", required = true)
  protected File outputDirectory;

  @Parameter(defaultValue = "${project.basedir}")
  protected File projectBaseFolder;

  @Parameter(defaultValue = "${project.basedir}/src/main/")
  protected File mainFolder;

  @Parameter(defaultValue = "${project.basedir}/src/test/munit/")
  protected File munitSourceFolder;

  @Parameter(defaultValue = "${lightweightPackage}")
  protected boolean lightweightPackage = false;

  @Parameter(defaultValue = "${skipValidation}")
  protected boolean skipValidation = false;

  @Parameter(property = "shared.libraries", required = false)
  protected List<SharedLibraryDependency> sharedLibraries;

  @Parameter
  protected String classifier;


  protected File getSourceFolder() throws MojoExecutionException {
    String packagingType = project.getPackaging();
    File sourceFolder = null;
    try {
      if (PackagingType.MULE_APPLICATION.equals(packagingType) || PackagingType.MULE_DOMAIN.equals(packagingType)) {
        sourceFolder = new File(mainFolder, MULE);
      }
      if (PackagingType.MULE_POLICY.equals(packagingType)) {
        sourceFolder = new File(mainFolder, POLICY);
      }
    } catch (IllegalArgumentException e) {
      List<String> packagingTypeNames =
          Arrays.stream(PackagingType.values()).map(type -> type.toString()).collect(Collectors.toList());
      String possibleValues = String.join(", ", packagingTypeNames);
      throw new MojoExecutionException("Unknown packaging type " + packagingType
          + ". Please specify a valid mule packaging type: " + possibleValues);
    }
    return sourceFolder;
  }

  protected File getMuleAppZipFile() {
    return new File(this.outputDirectory, this.finalName + ".jar");
  }

  protected void createFileIfNecessary(String... filePath) throws IOException {

    String path = StringUtils.join(filePath, File.separator);
    File file = new File(path);
    if (!file.exists()) {
      file.createNewFile();
    }
  }

  protected void createFolderIfNecessary(String... folderPath) {
    String path = StringUtils.join(folderPath, File.separator);
    File folder = new File(path);
    if (!folder.exists()) {
      folder.mkdir();
    }
  }
}

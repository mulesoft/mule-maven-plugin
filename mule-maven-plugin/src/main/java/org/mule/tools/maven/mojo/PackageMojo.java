/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.ArchiverException;
import org.mule.tools.api.packager.builder.MulePackageBuilder;
import org.mule.tools.api.packager.builder.PackageBuilder;

/**
 * Build a Mule application archive.
 */
@Mojo(name = "package",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class PackageMojo extends AbstractMuleMojo {

  private static final String ZIP_EXTENSION = "zip";

  @Component
  protected MavenProjectHelper helper;

  @Override
  public void doExecute() throws MojoExecutionException, MojoFailureException {
    getLog().debug("Packaging...");

    String targetFolder = project.getBuild().getDirectory();
    File destinationFile = getDestinationFile(targetFolder);
    try {
      getPackageBuilder().createPackage(Paths.get(targetFolder), destinationFile.toPath());
    } catch (ArchiverException | IOException e) {
      throw new MojoExecutionException("Exception creating the Mule App", e);
    }
    helper.attachArtifact(this.project, ZIP_EXTENSION, getClassifier(), destinationFile);
  }

  /**
   * Given a {@code targetFolder}, it returns a new {@link File} to the new compressed file where the complete Mule app will be
   * stored. If the file already exists, it will delete it and create a new one.
   *
   * @param targetFolder starting path in which the destination file will be stored
   * @return the destination file to store the Mule app
   * @throws MojoExecutionException if it can't delete the previous file
   */
  protected File getDestinationFile(String targetFolder) throws MojoExecutionException {
    checkArgument(targetFolder != null, "The target folder must not be null");
    Path destinationPath = Paths.get(targetFolder, getFileName());
    try {
      Files.deleteIfExists(destinationPath);
    } catch (IOException e) {
      throw new MojoExecutionException(String.format("Exception deleting the file [%s]", destinationPath), e);
    }
    return destinationPath.toFile();
  }

  protected String getFileName() {
    return finalName + "-" + getClassifier() + "." + ZIP_EXTENSION;
  }

  protected PackageBuilder getPackageBuilder() {
    return new MulePackageBuilder();
  }


  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_PACKAGE_PREVIOUS_RUN_PLACEHOLDER";
  }

}

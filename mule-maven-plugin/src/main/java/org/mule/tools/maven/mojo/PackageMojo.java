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

import static org.mule.tools.artifact.archiver.api.PackagerFolders.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.artifact.AttachedArtifact;
import org.codehaus.plexus.archiver.ArchiverException;
import org.mule.tools.artifact.archiver.internal.PackageBuilder;
import org.mule.tools.maven.mojo.model.Classifier;
import org.mule.tools.maven.mojo.model.PackagingType;

/**
 * Build a Mule application archive.
 */
@Mojo(name = "package",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class PackageMojo extends AbstractMuleMojo {

  private static final String TYPE = "zip";

  @Component
  protected ArtifactHandlerManager handlerManager;

  @Parameter(defaultValue = "${finalName}")
  protected String finalName;

  @Parameter(defaultValue = "${onlyMuleSources}")
  protected boolean onlyMuleSources = false;

  @Parameter(defaultValue = "${attachMuleSources}")
  protected boolean attachMuleSources = false;

  protected PackageBuilder packageBuilder;

  protected PackagingType packagingType;

  public void execute() throws MojoExecutionException, MojoFailureException {
    packagingType = PackagingType.fromString(project.getPackaging());
    String targetFolder = project.getBuild().getDirectory();
    File destinationFile = getDestinationFile(targetFolder);
    try {
      createMuleApp(destinationFile, targetFolder);
    } catch (ArchiverException e) {
      throw new MojoExecutionException("Exception creating the Mule App", e);
    }
    setProjectArtifactTypeToZip(destinationFile);
  }

  /**
   * Given a {@code targetFolder}, it returns a new {@link File} to the new compressed file where the complete Mule app will be
   * stored. If the file already exists, it will delete it and create a new one.
   *
   * @param targetFolder starting path in which the destination file will be stored
   * @return the destination file to store the Mule app
   * @throws MojoExecutionException if it can't delete the previous file
   */
  private File getDestinationFile(String targetFolder) throws MojoExecutionException {
    final Path destinationPath = Paths.get(targetFolder, getFinalName() + "." + TYPE);
    try {
      Files.deleteIfExists(destinationPath);
    } catch (IOException e) {
      throw new MojoExecutionException(String.format("Exception deleting the file [%s]", destinationPath), e);
    }
    return destinationPath.toFile();
  }

  protected String getFinalName() {
    if (finalName == null) {
      finalName = project.getArtifactId() + "-" + project.getVersion() + "-" + packagingType.resolveClassifier(classifier);
    }
    getLog().debug("Using final name: " + finalName);
    return finalName;
  }

  protected void setProjectArtifactTypeToZip(File destinationFile) {
    ArtifactHandler handler = handlerManager.getArtifactHandler(TYPE);
    Artifact artifact =
        new AttachedArtifact(this.project.getArtifact(), TYPE, packagingType.resolveClassifier(classifier).toString(), handler);
    artifact.setFile(destinationFile);
    artifact.setResolved(true);
    this.project.setArtifact(artifact);
  }

  protected void createMuleApp(File destinationFile, String targetFolder) throws MojoExecutionException, ArchiverException {
    initializePackageBuilder();
    try {
      PackageBuilder builder = packageBuilder.withDestinationFile(destinationFile);
      if (!onlyMuleSources) {
        builder
            .withClasses(new File(targetFolder + File.separator + CLASSES))
            .withMaven(new File(targetFolder + File.separator + META_INF + File.separator + MAVEN))
            .withMuleArtifact(new File(targetFolder + File.separator + META_INF + File.separator + MULE_ARTIFACT));
        if (PackagingType.MULE_POLICY.equals(project.getPackaging())) {
          builder.withPolicy(new File(targetFolder + File.separator + POLICY));

        } else {
          builder.withMule(new File(targetFolder + File.separator + MULE));
        }
        if (!lightwayPackage) {
          builder.withRepository(new File(targetFolder + File.separator + REPOSITORY));
        }

        if (attachMuleSources) {
          builder.withMuleSrc(new File(targetFolder + File.separator + META_INF + File.separator + MULE_SRC));
        }
      } else {
        builder.withMuleSrc(new File(targetFolder + File.separator + META_INF + File.separator + MULE_SRC));
      }

      builder.createDeployableFile();
    } catch (IOException e) {
      throw new MojoExecutionException("Cannot create archive");
    }
  }

  protected void initializePackageBuilder() {
    packageBuilder = new PackageBuilder();
  }
}

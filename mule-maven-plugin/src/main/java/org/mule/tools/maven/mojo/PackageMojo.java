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

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION_EXAMPLE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION_TEMPLATE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_PLUGIN;
import static org.mule.tools.api.packager.packaging.PackagingType.MULE_DOMAIN_BUNDLE;
import org.mule.tools.api.packager.builder.PackageBuilder;
import org.mule.tools.api.packager.builder.PackageBuilderFactory;
import org.mule.tools.api.packager.packaging.PackagingOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.ArchiverException;

/**
 * Build a Mule application archive.
 */
@Mojo(name = "package",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class PackageMojo extends AbstractMuleMojo {

  private static final String JAR_EXTENSION = "jar";
  private static final String ZIP_EXTENSION = "zip";

  @Component
  protected MavenProjectHelper helper;

  @Parameter(defaultValue = "${onlyMuleSources}")
  protected boolean onlyMuleSources = false;

  @Parameter(defaultValue = "${attachMuleSources}")
  protected boolean attachMuleSources = false;
  protected PackagingOptions options;

  @Override
  public void doExecute() throws MojoExecutionException {
    getLog().debug("Packaging...");

    String targetFolder = project.getBuild().getDirectory();
    File destinationFile = getDestinationFile(targetFolder);
    try {
      getPackageBuilder().createPackage(Paths.get(targetFolder), destinationFile.toPath());
    } catch (ArchiverException | IOException e) {
      throw new MojoExecutionException("Exception creating the Mule App", e);
    }
    helper.attachArtifact(this.project, getType(),
                          getPackagingType().resolveClassifier(classifier, lightweightPackage, testJar),
                          destinationFile);
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
    return project.getBuild().getFinalName() + "-" + getClassifier() + "." + getType();
  }

  protected PackageBuilder getPackageBuilder() {
    options = buildPackagingOptions();
    return PackageBuilderFactory.create(getPackagingType(), options);
  }

  protected PackagingOptions buildPackagingOptions() {
    boolean isMuleApplicationTemplate = MULE_APPLICATION_TEMPLATE.equals(classifier);
    boolean isMuleApplicationExample = MULE_APPLICATION_EXAMPLE.equals(classifier);
    boolean isMuleReusableApp = MULE_PLUGIN.equals(classifier);
    return new PackagingOptions(isMuleApplicationTemplate || onlyMuleSources,
                                isMuleReusableApp || lightweightPackage, isMuleApplicationExample || attachMuleSources,
                                testJar, useLocalRepository);
  }

  private String getType() {
    return getPackagingType().equals(MULE_DOMAIN_BUNDLE) ? ZIP_EXTENSION : JAR_EXTENSION;
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_PACKAGE_PREVIOUS_RUN_PLACEHOLDER";
  }

}

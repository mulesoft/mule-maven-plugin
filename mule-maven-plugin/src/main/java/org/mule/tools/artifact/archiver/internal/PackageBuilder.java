/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.artifact.archiver.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.tools.artifact.archiver.internal.packaging.PackagingType;
import org.mule.tools.artifact.archiver.internal.packaging.PackagingTypeFactory;

/**
 * Builder for Mule Application archives.
 */
public class PackageBuilder {

  private PackagingType packagingType;

  private transient Log log = LogFactory.getLog(this.getClass());

  private File muleFolder = null;
  private File policyFolder = null;
  private File classesFolder = null;
  private File repositoryFolder = null;

  private File mavenFolder = null;
  private File muleSrcFolder = null;
  private File muleArtifactFolder = null;

  private List<File> rootResources = new ArrayList<>();

  private File destinationFile;
  private MuleArchiver archiver = null;

  public PackageBuilder(PackagingType packagingType) {
    this.packagingType = packagingType;
  }

  public PackageBuilder() {
    this(PackagingTypeFactory.getDefaultPackaging());
  }

  public MuleArchiver getMuleArchiver() {
    if (archiver == null) {
      archiver = new MuleArchiver();
    }
    return archiver;
  }

  /**
   * @param archiver
   * @return builder
   */
  public PackageBuilder withArchiver(MuleArchiver archiver) {
    checkNotNull(archiver, "The org.mule.tools.artifact.org.mule.tools.artifact.archiver must not be null");
    this.archiver = archiver;
    return this;
  }

  /**
   * @param folder folder with all the configuration files of the application
   * @return builder
   */
  public PackageBuilder withClasses(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    classesFolder = folder;
    return this;
  }

  public PackageBuilder withMule(File folder) {
    checkArgument(folder != null);
    muleFolder = folder;
    return this;
  }

  public PackageBuilder withPolicy(File folder) {
    checkArgument(folder != null);
    policyFolder = folder;
    return this;
  }

  public PackageBuilder withMaven(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    mavenFolder = folder;
    return this;
  }

  public PackageBuilder withMuleSrc(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    muleSrcFolder = folder;
    return this;
  }

  public PackageBuilder withMuleArtifact(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    muleArtifactFolder = folder;
    return this;
  }

  public PackageBuilder withRepository(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    repositoryFolder = folder;
    return this;
  }

  public PackageBuilder withRootResource(File resource) {
    checkArgument(resource != null, "The resource must not be null");
    rootResources.add(resource);
    return this;
  }

  /**
   * @param file file to be created with the content of the app
   * @return
   */
  public PackageBuilder withDestinationFile(File file) {
    checkArgument(file != null, "The file must not be null");
    checkArgument(!file.exists(), "The file must not be duplicated");
    this.destinationFile = file;
    return this;
  }

  /**
   * Creates the application package.
   *
   * @throws IOException
   */
  public void createDeployableFile() throws IOException {
    runPrePackageValidations();

    MuleArchiver archiver = getMuleArchiver();
    if (null != muleFolder && muleFolder.exists() && muleFolder.isDirectory()) {
      archiver.addMule(muleFolder, null, null);
    }

    if (null != policyFolder && policyFolder.exists() && policyFolder.isDirectory()) {
      archiver.addPolicy(policyFolder, null, null);
    }

    if (null != classesFolder && classesFolder.exists() && classesFolder.isDirectory()) {
      archiver.addClasses(classesFolder, null, null);
      // Warning
    }

    if (null != mavenFolder && mavenFolder.exists() && mavenFolder.isDirectory()) {
      archiver.addMaven(mavenFolder, null, null);
      // Warning
    }

    if (null != muleArtifactFolder && muleArtifactFolder.exists() && muleArtifactFolder.isDirectory()) {
      archiver.addMuleArtifact(muleArtifactFolder, null, null);
      // Warning
    }

    if (null != muleSrcFolder && muleSrcFolder.exists() && muleSrcFolder.isDirectory()) {
      archiver.addMuleSrc(muleSrcFolder, null, null);
      // Warning
    }

    if (null != repositoryFolder && repositoryFolder.exists() && repositoryFolder.isDirectory()) {
      archiver.addRepository(repositoryFolder, null, null);
      // Warning
    }

    if (null != muleSrcFolder && muleSrcFolder.exists() && muleSrcFolder.isDirectory()) {
      archiver.addMuleSrc(muleSrcFolder, null, null);
    }

    archiver.setDestFile(destinationFile);
    archiver.createArchive();
  }

  private void runPrePackageValidations() {
    checkArgument(destinationFile != null, "The destination file has not been set");
  }

  private void checkMandatoryFolder(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    checkArgument(folder.exists(), "The folder must exists");
    checkArgument(folder.isDirectory(), "The folder must be a valid directory");
  }


  /**
   * Resource go under: app-folder/
   *
   * @param file file to be included in the root folder of the app
   * @return builder
   */
  public PackageBuilder addRootResourcesFile(File file) {
    // this.rootResourceFolder.addFile(file);
    return this;
  }

  public void generateArtifact(File targetFolder, File destinationFile) throws IOException {
    checkMandatoryFolder(targetFolder);
    checkArgument(destinationFile != null && !destinationFile.exists(), "Destination file must not be null or already exist");
    File[] files = targetFolder.listFiles();
    if (files == null || files.length == 0) {
      log.warn("The provided target folder is empty, no file will be generated");
      return;
    }
    Map<String, File> fileMap = Arrays.stream(files).collect(Collectors.toMap(File::getName, Function.identity()));
    try {
      this.packagingType.applyPackaging(this, fileMap).withDestinationFile(destinationFile);
    } catch (IllegalArgumentException e) {
      log.warn("The provided target folder does not have the expected structure");
      return;
    }
    this.createDeployableFile();
    log.info("File " + destinationFile.getName() + " has been successfully created");
  }
}

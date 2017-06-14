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
import static org.mule.tools.artifact.archiver.api.PackagerFolders.CLASSES;
import static org.mule.tools.artifact.archiver.api.PackagerFolders.MAVEN;
import static org.mule.tools.artifact.archiver.api.PackagerFolders.META_INF;
import static org.mule.tools.artifact.archiver.api.PackagerFolders.MULE;
import static org.mule.tools.artifact.archiver.api.PackagerFolders.MULE_ARTIFACT;
import static org.mule.tools.artifact.archiver.api.PackagerFolders.MULE_SRC;
import static org.mule.tools.artifact.archiver.api.PackagerFolders.POLICY;
import static org.mule.tools.artifact.archiver.api.PackagerFolders.REPOSITORY;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.mule.tools.artifact.archiver.internal.packaging.PackagingMode;
import org.mule.tools.artifact.archiver.internal.packaging.PackagingModeFactory;

/**
 * Builder for Mule Application archives.
 */
public class PackageBuilder {

  private PackagingMode packagingMode;

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

  public PackageBuilder(PackagingMode packagingMode) {
    this.packagingMode = packagingMode;
  }

  public PackageBuilder() {
    this(PackagingModeFactory.getDefaultPackaging());
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
   * It wires all the possible folders that should be properly name from the workingDirectory to the package.
   * 
   * @param workingDirectory a directory containing all the folders properly named
   * @return a PackageBuilder
   */
  public PackageBuilder fromWorkingDirectory(Path workingDirectory) {
    return this
        .withClasses(workingDirectory.resolve(CLASSES).toFile())
        // TODO check this
        .withMule(workingDirectory.resolve(MULE).toFile())
        .withPolicy(workingDirectory.resolve(POLICY).toFile())

        .withMaven(workingDirectory.resolve(META_INF).resolve(MAVEN).toFile())
        .withMuleArtifact(workingDirectory.resolve(META_INF).resolve(MULE_ARTIFACT).toFile())

        .withMuleSrc(workingDirectory.resolve(META_INF).resolve(MULE_SRC).toFile())
        .withRepository(workingDirectory.resolve(REPOSITORY).toFile());
  }

  /**
   * Creates the application package.
   *
   * It does so using the provided directories. If a directory does not exits or a directory path is not an actual directory then
   * such element will not be added to the final package.
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

    archiver.setDestFile(destinationFile);
    archiver.createArchive();
  }

  private void runPrePackageValidations() {
    checkArgument(destinationFile != null, "The destination file has not been set");
  }

}

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.builder;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.mule.tools.api.packager.structure.FolderNames.APPLICATIONS;
import static org.mule.tools.api.packager.structure.FolderNames.DOMAIN;
import static org.mule.tools.api.packager.structure.FolderNames.MAVEN;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.codehaus.plexus.archiver.ArchiverException;

import org.mule.tools.api.packager.archiver.AbstractArchiver;
import org.mule.tools.api.packager.archiver.DomainBundleArchiver;

/**
 * Builder for Mule Domain Bundle packages.
 */
public class DomainBundlePackageBuilder implements PackageBuilder {

  private File domainFolder = null;
  private File applicationsFolder = null;
  private File mavenFolder = null;

  private DomainBundleArchiver archiver;

  public DomainBundlePackageBuilder withMaven(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    checkArgument(folder.exists(), "The folder must exist");
    mavenFolder = folder;
    return this;
  }

  public DomainBundlePackageBuilder withDomain(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    checkArgument(folder.exists(), "The folder must exist");
    domainFolder = folder;
    return this;
  }

  public DomainBundlePackageBuilder withApplications(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    checkArgument(folder.exists(), "The folder must exist");
    applicationsFolder = folder;
    return this;
  }

  public DomainBundlePackageBuilder withArchiver(AbstractArchiver archiver) {
    checkArgument(archiver != null, "Archiver must not be null");
    this.archiver = (DomainBundleArchiver) archiver;
    return this;
  }

  /**
   * Creates a mule domain bundle package based on the contents of the origin folder, writing them to the destination jar file.
   * The target file is supposed to have more or less the structure of the example below:
   *
   * <pre>
   *
   * ├── applications
   * │   ├── mule-app-a-1.0.0-mule-application.jar
   * │   ├── mule-app-b-1.0.0-mule-application.jar
   * │   └── mule-app-c-1.0.0-mule-application.jar
   * ├── domain
   * │   └── mule-domain-a-1.0.0-mule-domain.jar
   * └── META-INF
   *     └── maven
   *         ├── group-id
   *         └── artifact-id
   *             ├── pom.xml
   *             └── pom.properties
   * </pre>
   *
   * @param originFolderPath folder containing the source files.
   * @param destinationPath location where to leave the final package.
   * @throws ArchiverException
   * @throws IOException
   */
  @Override
  public void createPackage(Path originFolderPath, Path destinationPath) throws ArchiverException, IOException {
    checkArgument(originFolderPath != null, "The origin path must not be null");
    checkArgument(originFolderPath.toFile().exists(), "The origin path must exist");

    Path metaInfPath = originFolderPath.resolve(META_INF.value());
    this.withDomain(originFolderPath.resolve(DOMAIN.value()).toFile())
        .withApplications(originFolderPath.resolve(APPLICATIONS.value()).toFile())
        .withMaven(metaInfPath.resolve(MAVEN.value()).toFile());

    this.createArchive(destinationPath);
  }

  @Override
  public void createPackage(Path destinationPath) throws ArchiverException, IOException {
    this.createArchive(destinationPath);
  }

  private void createArchive(Path destinationPath) throws IOException {
    checkArgument(destinationPath != null, "The destination path must not be null");
    checkArgument(!destinationPath.toFile().exists(), "The destination file must not be duplicated");

    validateState();
    DomainBundleArchiver archiver = getArchiver();

    archiver.addDomain(domainFolder, null, null);
    archiver.addApplications(applicationsFolder, null, null);
    archiver.addMaven(mavenFolder, null, null);

    archiver.setDestFile(destinationPath.toFile());
    archiver.createArchive();
  }

  /**
   * Ensures that all the required folders have been provided
   *
   */
  private void validateState() {
    isValidFolder(domainFolder, "The domain folders has not been properly defined");
    isValidFolder(applicationsFolder, "The applications folders has not been properly defined");
    isValidFolder(mavenFolder, "The maven folders has not been properly defined");
  }

  /**
   * Ensures the provided file is a valid folder.
   *
   * @param file the file to validate
   * @param message the error message to show
   */
  private void isValidFolder(File file, String message) {
    checkState(file != null && file.exists() && file.isDirectory(), message);
  }

  protected DomainBundleArchiver getArchiver() {
    if (archiver == null) {
      archiver = new DomainBundleArchiver();
    }
    return archiver;
  }
}

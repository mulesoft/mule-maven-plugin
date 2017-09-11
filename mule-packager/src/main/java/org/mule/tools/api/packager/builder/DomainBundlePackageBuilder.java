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

import org.codehaus.plexus.archiver.ArchiverException;
import org.mule.tools.api.packager.archiver.AbstractArchiver;
import org.mule.tools.api.packager.archiver.DomainBundleArchiver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.mule.tools.api.packager.structure.FolderNames.*;

/**
 * Builder for Mule Domain Bundle packages.
 */
public class DomainBundlePackageBuilder implements PackageBuilder {

  private File destinationFile;
  private DomainBundleArchiver archiver;
  private File domainFolder = null;
  private File applicationsFolder = null;
  private File mavenFolder = null;


  public DomainBundleArchiver getDomainBundleArchiver() {
    if (archiver == null) {
      archiver = new DomainBundleArchiver();
    }
    return archiver;
  }

  /**
   * @param file file to be created with the content of the domain bundle
   * @return
   */
  public DomainBundlePackageBuilder withDestinationFile(File file) {
    checkArgument(file != null, "The file must not be null");
    checkArgument(!file.exists(), "The file must not be duplicated");
    this.destinationFile = file;
    return this;
  }

  public DomainBundlePackageBuilder withArchiver(AbstractArchiver archiver) {
    checkNotNull(archiver, "AbstractArchiver must not be null");
    this.archiver = (DomainBundleArchiver) archiver;
    return this;
  }

  public DomainBundlePackageBuilder withMaven(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    mavenFolder = folder;
    return this;
  }

  public DomainBundlePackageBuilder withDomain(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    domainFolder = folder;
    return this;
  }

  public DomainBundlePackageBuilder withApplications(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    applicationsFolder = folder;
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
   * @param destinationFile file that represents the resource that is going to represent the final package.
   * @param originFolder folder containing the source files.
   * @throws ArchiverException
   * @throws IOException
   */
  @Override
  public void createPackage(File destinationFile, String originFolder) throws ArchiverException, IOException {
    Path originFolderPath = Paths.get(originFolder);
    Path metaInfPath = originFolderPath.resolve(META_INF.value());
    DomainBundlePackageBuilder builder =
        this.withDestinationFile(destinationFile)
            .withDomain(originFolderPath.resolve(DOMAIN.value()).toFile())
            .withApplications(originFolderPath.resolve(APPLICATIONS.value()).toFile())
            .withMaven(metaInfPath.resolve(MAVEN.value()).toFile());
    builder.createDeployableFile();
  }

  @Override
  public void createDeployableFile() throws IOException {
    checkState(destinationFile != null, "The destination file has not been set");

    DomainBundleArchiver archiver = getDomainBundleArchiver();

    if (null != domainFolder && domainFolder.exists() && domainFolder.isDirectory()) {
      archiver.addDomain(domainFolder, null, null);
    }

    if (null != applicationsFolder && applicationsFolder.exists() && applicationsFolder.isDirectory()) {
      archiver.addApplications(applicationsFolder, null, null);
    }

    if (null != mavenFolder && mavenFolder.exists() && mavenFolder.isDirectory()) {
      archiver.addMaven(mavenFolder, null, null);
    }

    archiver.setDestFile(destinationFile);
    archiver.createArchive();
  }
}

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
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.mule.tools.api.packager.structure.FolderNames.CLASSES;
import static org.mule.tools.api.packager.structure.FolderNames.MAVEN;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_SRC;
import static org.mule.tools.api.packager.structure.FolderNames.REPOSITORY;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.archiver.ArchiverException;

import org.mule.tools.api.packager.archiver.AbstractArchiver;
import org.mule.tools.api.packager.archiver.MuleArchiver;
import org.mule.tools.api.packager.packaging.PackagingOptions;

/**
 * Builder for Mule Application packages.
 */
public class MulePackageBuilder implements PackageBuilder {

  private File classesFolder = null;
  private File repositoryFolder = null;

  private File mavenFolder = null;
  private File muleSrcFolder = null;
  private File muleArtifactFolder = null;

  private List<File> rootResources = new ArrayList<>();

  private File destinationFile;
  private MuleArchiver archiver = null;
  private PackagingOptions packagingOptions;

  public MuleArchiver getMuleArchiver() {
    if (archiver == null) {
      archiver = new MuleArchiver();
    }
    return archiver;
  }

  public MulePackageBuilder withPackagingOptions(PackagingOptions packagingOptions) {
    checkNotNull(packagingOptions, "The PackagingOptions must not be null");
    this.packagingOptions = packagingOptions;
    return this;
  }

  public MulePackageBuilder withArchiver(AbstractArchiver archiver) {
    checkNotNull(archiver, "AbstractArchiver must not be null");
    this.archiver = (MuleArchiver) archiver;
    return this;
  }

  public MulePackageBuilder withClasses(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    classesFolder = folder;
    return this;
  }

  public MulePackageBuilder withMaven(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    mavenFolder = folder;
    return this;
  }

  public MulePackageBuilder withMuleSrc(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    muleSrcFolder = folder;
    return this;
  }

  public MulePackageBuilder withMuleArtifact(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    muleArtifactFolder = folder;
    return this;
  }

  public MulePackageBuilder withRepository(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    repositoryFolder = folder;
    return this;
  }

  public MulePackageBuilder withRootResource(File resource) {
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
    // TODO, ensure the paths exits or use the validator
    return this
        .withClasses(workingDirectory.resolve(CLASSES.value()).toFile())
        .withMaven(workingDirectory.resolve(META_INF.value()).resolve(MAVEN.value()).toFile())
        .withMuleArtifact(workingDirectory.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile())
        .withMuleSrc(workingDirectory.resolve(META_INF.value()).resolve(MULE_SRC.value()).toFile())
        .withRepository(workingDirectory.resolve(REPOSITORY.value()).toFile());
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
    checkState(destinationFile != null, "The destination file has not been set");

    MuleArchiver archiver = getMuleArchiver();

    if (null != classesFolder && classesFolder.exists() && classesFolder.isDirectory()) {
      archiver.addToRoot(classesFolder, null, null);
    }

    if (null != mavenFolder && mavenFolder.exists() && mavenFolder.isDirectory()) {
      archiver.addMaven(mavenFolder, null, null);
    }

    if (null != muleArtifactFolder && muleArtifactFolder.exists() && muleArtifactFolder.isDirectory()) {
      archiver.addMuleArtifact(muleArtifactFolder, null, null);
    }

    if (null != muleSrcFolder && muleSrcFolder.exists() && muleSrcFolder.isDirectory()) {
      archiver.addMuleSrc(muleSrcFolder, null, null);
    }

    if (null != repositoryFolder && repositoryFolder.exists() && repositoryFolder.isDirectory()) {
      archiver.addRepository(repositoryFolder, null, null);
    }

    archiver.setDestFile(destinationFile);
    archiver.createArchive();
  }

  /**
   * Creates a mule app package based on the contents of the origin folder, writing them to the destination jar file. The target
   * file is supposed to have more or less the structure of the example below:
   * 
   * <pre>
   *
   *
   * ├── log4j2.xml
   * ├── org
   * │   └── MyClass.class
   * ├── mule
   * │   ├── package
   * │   └── mule-configuration(s).xml
   * ├── api
   * │   └── *.raml
   * └── wsdl
   * │   └── *.wsdl
   * ├── META-INF
   * │   ├── mule-src
   * │   │   └── application-name
   * │   ├── maven
   * │   │   ├── group-id
   * │   │   └── artifact-id
   * │   │       ├── pom.xml
   * │   │       └── pom.properties
   * │   └── mule-artifact
   * │       ├── classloader-model.json
   * │       └── mule-artifact.json
   * └── repository
   *     ├── org
   *     └── mule
   *         ├── munit
   *         └── munit-common
   *             ├── 1.1.0
   *             │   ├── *.jar
   *             │   └── pom.xml
   *             └── 2.0.0
   *                 ├── *.jar
   *                 └── pom.xml
   * </pre>
   *
   * @param destinationFile file that represents the resource that is going to represent the final package.
   * @param originFolder folder containing the source files.
   * @throws ArchiverException
   * @throws IOException
   */
  public void createPackage(File destinationFile, String originFolder)
      throws ArchiverException, IOException {
    checkState(packagingOptions != null, "Packaging options should not be null when creating a mule package");

    Path originFolderPath = Paths.get(originFolder);
    Path metaInfPath = originFolderPath.resolve(META_INF.value());

    MulePackageBuilder builder = (MulePackageBuilder) this.withDestinationFile(destinationFile);
    if (!packagingOptions.isOnlyMuleSources()) {
      builder
          .withClasses(originFolderPath.resolve(CLASSES.value()).toFile())
          .withMaven(metaInfPath.resolve(MAVEN.value()).toFile())
          .withMuleArtifact(metaInfPath.resolve(MULE_ARTIFACT.value()).toFile());

      if (!packagingOptions.isLightweightPackage()) {
        builder.withRepository(originFolderPath.resolve(REPOSITORY.value()).toFile());
      }

      if (packagingOptions.isAttachMuleSources()) {
        builder.withMuleSrc(metaInfPath.resolve(MULE_SRC.value()).toFile());
      }
    } else {
      builder.withMuleSrc(metaInfPath.resolve(MULE_SRC.value()).toFile());
    }

    builder.createDeployableFile();
  }

}

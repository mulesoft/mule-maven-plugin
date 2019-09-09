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
import static org.mule.tools.api.packager.structure.FolderNames.CLASSES;
import static org.mule.tools.api.packager.structure.FolderNames.MAVEN;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_SRC;
import static org.mule.tools.api.packager.structure.FolderNames.MUNIT;
import static org.mule.tools.api.packager.structure.FolderNames.REPOSITORY;
import static org.mule.tools.api.packager.structure.FolderNames.TEST_CLASSES;
import static org.mule.tools.api.packager.structure.FolderNames.TEST_MULE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.archiver.ArchiverException;

import org.mule.tools.api.packager.archiver.MuleArchiver;
import org.mule.tools.api.packager.packaging.PackagingOptions;
import org.mule.tools.api.packager.structure.FolderNames;

/**
 * Builder for Mule Application packages.
 */
public class MulePackageBuilder implements PackageBuilder {

  public static final String CLASSLOADER_MODEL_JSON = "classloader-model.json";
  private PackagingOptions packagingOptions;

  private File classesFolder = null;
  private File testClassesFolder = null;
  private File testMule = null;

  private File repositoryFolder = null;

  // All this is META-INF
  private File mavenFolder = null;
  private File muleSrcFolder = null;
  private File muleArtifactFolder = null;

  protected List<File> rootResources = new ArrayList<>();

  private MuleArchiver archiver = null;

  public MulePackageBuilder withClasses(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    checkArgument(folder.exists(), "The folder must exists");
    classesFolder = folder;
    return this;
  }

  public MulePackageBuilder withTestClasses(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    checkArgument(folder.exists(), "The folder must exists");
    testClassesFolder = folder;
    return this;
  }

  public MulePackageBuilder withTestMule(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    checkArgument(folder.exists(), "The folder must exists");
    testMule = folder;
    return this;
  }

  public MulePackageBuilder withMaven(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    checkArgument(folder.exists(), "The folder must exists");
    mavenFolder = folder;
    return this;
  }

  public MulePackageBuilder withMuleSrc(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    checkArgument(folder.exists(), "The folder must exists");
    muleSrcFolder = folder;
    return this;
  }

  public MulePackageBuilder withMuleArtifact(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    checkArgument(folder.exists(), "The folder must exists");
    muleArtifactFolder = folder;
    return this;
  }

  public MulePackageBuilder withRepository(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    checkArgument(folder.exists(), "The folder must exists");
    repositoryFolder = folder;
    return this;
  }

  public MulePackageBuilder withRootResource(File resource) {
    checkArgument(resource != null, "The resource must not be null");
    checkArgument(resource.exists(), "The resource must exists");
    rootResources.add(resource);
    return this;
  }

  public MulePackageBuilder withPackagingOptions(PackagingOptions packagingOptions) {
    checkArgument(packagingOptions != null, "The PackagingOptions must not be null");
    this.packagingOptions = packagingOptions;
    return this;
  }

  public MulePackageBuilder withArchiver(MuleArchiver archiver) {
    checkArgument(archiver != null, "The archiver must not be null");
    this.archiver = archiver;
    return this;
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
   * @param originFolderPath folder containing the source files.
   * @param destinationPath location where to leave the final package.
   * @throws ArchiverException
   * @throws IOException
   */
  @Override
  public void createPackage(Path originFolderPath, Path destinationPath) throws ArchiverException, IOException {
    checkArgument(originFolderPath != null, "The origin path must not be null");
    checkArgument(originFolderPath.toFile().exists(), "The origin path must exists");

    Path metaInfPath = originFolderPath.resolve(META_INF.value());
    this
        .withClasses(originFolderPath.resolve(CLASSES.value()).toFile())
        .withMaven(metaInfPath.resolve(MAVEN.value()).toFile())
        .withMuleArtifact(metaInfPath.resolve(MULE_ARTIFACT.value()).toFile())
        .withTestClasses(originFolderPath.resolve(TEST_CLASSES.value()).toFile())
        .withTestMule(originFolderPath.resolve(TEST_MULE.value()).resolve(MUNIT.value()).toFile())
        .withRepository(originFolderPath.resolve(REPOSITORY.value()).toFile())
        .withMuleSrc(metaInfPath.resolve(MULE_SRC.value()).toFile());

    this.createArchive(destinationPath);
  }

  @Override
  public void createPackage(Path destinationPath) throws ArchiverException, IOException {
    createArchive(destinationPath);
  }

  /**
   * Creates the application package.
   *
   * It does so using the provided directories. If a directory does not exist or a directory path is not an actual directory then
   * such element will not be added to the final package.
   *
   * @throws IOException
   */
  private void createArchive(Path destinationPath) throws IOException {
    checkArgument(destinationPath != null, "The destination path must not be null");
    checkArgument(!destinationPath.toFile().exists(), "The destination file must not be duplicated");

    validateState(packagingOptions);

    MuleArchiver archiver = getArchiver();
    if (!packagingOptions.isOnlyMuleSources()) {
      archiver.addToRoot(classesFolder, null, null);
      archiver.addMaven(mavenFolder, null, null);

      if (packagingOptions.isLightweightPackage() && !packagingOptions.isUseLocalRepository()) {
        archiver.addMuleArtifact(muleArtifactFolder, null, new String[] {CLASSLOADER_MODEL_JSON});
      } else {
        archiver.addMuleArtifact(muleArtifactFolder, null, null);
      }

      if (packagingOptions.isTestPackage()) {
        archiver.addToRoot(testClassesFolder, null, null);
        archiver.addToRoot(testMule, null, null);
      }

      if (!packagingOptions.isLightweightPackage()) {
        archiver.addRepository(repositoryFolder, null, null);
      }

      if (packagingOptions.isAttachMuleSources()) {
        archiver.addMuleSrc(muleSrcFolder, null, null);
      }
    } else {
      archiver.addMuleSrc(muleSrcFolder, null, null);
    }

    archiver.setDestFile(destinationPath.toFile());
    archiver.createArchive();
  }

  /**
   * Ensures that all the required folders have been provided based on the {@link PackagingOptions}
   * 
   * @param packagingOptions
   */
  private void validateState(PackagingOptions packagingOptions) {
    checkState(packagingOptions != null, "Packaging options should not be null when creating a mule package");

    if (!packagingOptions.isOnlyMuleSources()) {
      isValidFolder(classesFolder, "The classes folder has not been defined");
      isValidFolder(mavenFolder, "The maven folder has not been defined");
      isValidFolder(muleArtifactFolder, "The mule-artifact folder has not been defined");

      if (packagingOptions.isTestPackage()) {
        isValidFolder(testClassesFolder, "The test-classes folder has not been defined");
        isValidFolder(testMule, "The test-mule folder has not been defined");
      }

      if (!packagingOptions.isLightweightPackage()) {
        isValidFolder(repositoryFolder, "The repository folder has not been defined");
      }

      if (packagingOptions.isAttachMuleSources()) {
        isValidFolder(muleSrcFolder, "The mules-src folder has not been defined");
      }
    } else {
      isValidFolder(muleSrcFolder, "The mules-src folder has not been defined");
    }
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

  protected MuleArchiver getArchiver() {
    if (archiver == null) {
      archiver = new MuleArchiver();
    }
    return archiver;
  }

}

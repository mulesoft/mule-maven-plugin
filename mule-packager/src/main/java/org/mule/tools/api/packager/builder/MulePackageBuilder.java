/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.builder;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.mule.tools.api.packager.structure.FolderNames.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.archiver.ArchiverException;
import org.mule.tools.api.packager.archiver.MuleArchiver;

/**
 * Builder for Mule Application packages.
 */
public class MulePackageBuilder implements PackageBuilder {

  private File classesFolder = null;
  private File testClassesFolder = null;
  private File muleFolder = null;
  private File testMuleFolder = null;

  protected List<File> rootResources = new ArrayList<>();

  private MuleArchiver archiver = null;
  private File apiFolder;
  private File wsdlFolder;

  protected MulePackageBuilder withClasses(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    checkArgument(folder.exists(), "The folder must exists");
    classesFolder = folder;
    return this;
  }

  protected MulePackageBuilder withTestClasses(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    checkArgument(folder.exists(), "The folder must exist");
    testClassesFolder = folder;
    return this;
  }

  protected MulePackageBuilder withRootResource(File resource) {
    checkArgument(resource != null, "The resource must not be null");
    checkArgument(resource.exists(), "The resource must exist");
    rootResources.add(resource);
    return this;
  }

  protected MulePackageBuilder withArchiver(MuleArchiver archiver) {
    checkArgument(archiver != null, "The archiver must not be null");
    this.archiver = archiver;
    return this;
  }

  protected MulePackageBuilder withMuleFolder(File folder) {
    checkArgument(folder != null, "The folder must not be null");
    checkArgument(folder.exists(), "The folder must exist");
    this.muleFolder = folder;
    return this;
  }

  protected MulePackageBuilder withTestMuleFolder(File folder) {
    this.testMuleFolder = folder;
    return this;
  }

  protected MulePackageBuilder withApiFolder(File folder) {
    this.apiFolder = folder;
    return this;
  }

  protected MulePackageBuilder withWsdlFolder(File folder) {
    this.wsdlFolder = folder;
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

    this.withClasses(originFolderPath.resolve(CLASSES.value()).toFile())
        .withTestClasses(originFolderPath.resolve(TEST_CLASSES.value()).toFile())
        .withMuleFolder(originFolderPath.resolve(MULE.value()).toFile())
        .withTestMuleFolder(originFolderPath.resolve(TEST_MULE.value()).toFile())
        .withApiFolder(originFolderPath.resolve(API.value()).toFile())
        .withWsdlFolder(originFolderPath.resolve(WSDL.value()).toFile());

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

    MuleArchiver archiver = getArchiver();
    archiver.addToRoot(muleFolder, null, null);
    archiver.addClasses(classesFolder, null, null);
    archiver.addApi(apiFolder, null, null);
    archiver.addWsdl(wsdlFolder, null, null);


    archiver.setDestFile(destinationPath.toFile());
    archiver.createArchive();
  }

  /**
   * Ensures that all the required folders have been provided.
   * 
   */
  private void validateState() {
    isValidFolder(classesFolder, "The classes folder has not been defined");
    isValidFolder(testClassesFolder, "The test-classes folder has not been defined");
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

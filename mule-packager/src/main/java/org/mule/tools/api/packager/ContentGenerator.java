/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager;

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_ARTIFACT_JSON;
import static org.mule.tools.api.packager.structure.PackagerFiles.POM_PROPERTIES;
import static org.mule.tools.api.packager.structure.PackagerFiles.POM_XML;
import static org.mule.tools.api.packager.structure.PackagerFolders.CLASSES;
import static org.mule.tools.api.packager.structure.PackagerFolders.MAVEN;
import static org.mule.tools.api.packager.structure.PackagerFolders.META_INF;
import static org.mule.tools.api.packager.structure.PackagerFolders.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.PackagerFolders.MULE_SRC;
import static org.mule.tools.api.packager.structure.PackagerFolders.TARGET;
import static org.mule.tools.api.packager.structure.PackagerFolders.TEST_MULE;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import org.mule.tools.api.classloader.model.ClassLoaderModel;
import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.util.CopyFileVisitor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * It knows how to generate the required content for each of the mandatory folder of the package
 * 
 */
public class ContentGenerator {

  private static final String CLASSLOADER_MODEL_FILE_NAME = "classloader-model.json";
  private String groupId;
  private String artifactId;
  private String version;
  private PackagingType packagingType;

  private Path projectBaseFolder;
  private Path projectTargetFolder;

  public ContentGenerator(String groupId, String artifactId, String version, PackagingType packagingType,
                          Path projectBaseFolder, Path projectTargetFolder) {

    checkArgument(StringUtils.isNotEmpty(groupId), "The groupId must not be null nor empty");
    checkArgument(StringUtils.isNotEmpty(artifactId), "The artifactId must not be null nor empty");
    checkArgument(StringUtils.isNotEmpty(version), "The version must not be null nor empty");

    checkArgument(packagingType != null, "The packagingType must not be null");

    checkPathExist(projectBaseFolder);
    checkPathExist(projectTargetFolder);

    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;

    this.packagingType = packagingType;

    this.projectBaseFolder = projectBaseFolder;
    this.projectTargetFolder = projectTargetFolder;
  }

  /**
   * It create all the package content in the required folders
   * 
   * @throws IOException
   */
  public void createContent() throws IOException {
    createMuleSrcFolderContent();
    createMetaInfMuleSourceFolderContent();
    createDescriptors();
  }

  /**
   * It creates the content that contains the productive Mule source code. It leaves it in the classes folder
   *
   * @throws IOException
   */
  public void createMuleSrcFolderContent() throws IOException {
    Path originPath = packagingType.getSourceFolderLocation(projectBaseFolder);
    Path destinationPath = projectTargetFolder.resolve(CLASSES);

    copyContent(originPath, destinationPath, Optional.ofNullable(null), true, false);
  }

  /**
   * It creates the content that contains the test Mule source code. The name of the folder depends on the {@link PackagingType}
   * 
   * @throws IOException
   */
  public void createTestFolderContent() throws IOException {
    Path originPath = packagingType.getTestSourceFolderLocation(projectBaseFolder);
    Path destinationPath = projectTargetFolder.resolve(TEST_MULE).resolve(originPath.getFileName());

    copyContent(originPath, destinationPath, Optional.ofNullable(null), false, true);
  }

  /**
   * It creates the {@link org.mule.tools.api.packager.structure.PackagerFolders#MULE_SRC} folder used by IDEs to import the
   * project source code
   * 
   * @throws IOException
   */
  public void createMetaInfMuleSourceFolderContent() throws IOException {
    Path originPath = projectBaseFolder;
    Path destinationPath = projectTargetFolder.resolve(META_INF).resolve(MULE_SRC).resolve(artifactId);

    List<Path> exclusions = new ArrayList<>();
    exclusions.add(projectBaseFolder.resolve(TARGET));

    copyContent(originPath, destinationPath, Optional.of(exclusions));
  }

  /**
   * It creates the descriptors files, pom.xml, pom.properties, and the mule-*.json file. The name of the the last one depends on
   * the {@link PackagingType}
   * 
   * @throws IOException
   */
  public void createDescriptors() throws IOException {
    copyPomFile();
    createPomProperties();
    copyDescriptorFile();
  }

  /**
   * It creates classloader-model.json in META-INF/mule-artifact
   *
   * @param classLoaderModel the classloader model of the application being packaged
   */
  public void createApplicationClassLoaderModelJsonFile(ClassLoaderModel classLoaderModel) {
    File destinationFolder =
        projectTargetFolder.resolve(META_INF).resolve(MULE_ARTIFACT).toFile();
    createClassLoaderModelJsonFile(classLoaderModel, destinationFolder);
  }

  /**
   * Creates a {@link ClassLoaderModel} from the JSON representation
   *
   * @param classLoaderModelDescriptor file containing the classloader model in JSON format
   * @return a non null {@link ClassLoaderModel} matching the provided JSON content
   */
  public static ClassLoaderModel createClassLoaderModelFromJson(File classLoaderModelDescriptor) {
    try {
      Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();

      Reader reader = new FileReader(classLoaderModelDescriptor);
      ClassLoaderModel classLoaderModel = gson.fromJson(reader, ClassLoaderModel.class);
      reader.close();

      return classLoaderModel;
    } catch (IOException e) {
      throw new RuntimeException("Could not create classloadermodel.json", e);
    }
  }

  private void copyPomFile() throws IOException {
    Path originPath = projectBaseFolder.resolve(POM_XML);
    Path destinationPath = projectTargetFolder.resolve(META_INF).resolve(MAVEN).resolve(groupId).resolve(artifactId);
    String destinationFileName = originPath.getFileName().toString();

    copyFile(originPath, destinationPath, destinationFileName);
  }

  private void copyDescriptorFile() throws IOException {
    Path originPath = projectBaseFolder.resolve(MULE_ARTIFACT_JSON);
    Path destinationPath = projectTargetFolder.resolve(META_INF).resolve(MULE_ARTIFACT);
    String destinationFileName = originPath.getFileName().toString();

    copyFile(originPath, destinationPath, destinationFileName);
  }

  protected void createPomProperties() {
    Path pomPropertiesDestinationPath = projectTargetFolder.resolve(META_INF).resolve(MAVEN).resolve(groupId).resolve(artifactId);
    checkPathExist(pomPropertiesDestinationPath);

    Path pomPropertiesFilePath = pomPropertiesDestinationPath.resolve(POM_PROPERTIES);
    try {
      PrintWriter writer = new PrintWriter(pomPropertiesFilePath.toString(), "UTF-8");
      writer.println("version=" + version);
      writer.println("groupId=" + groupId);
      writer.println("artifactId=" + artifactId);
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException("Could not create pom.properties", e);
    }
  }

  private void copyContent(Path originPath, Path destinationPath, Optional<List<Path>> exclusions) throws IOException {
    copyContent(originPath, destinationPath, exclusions, true, true);
  }

  private void copyContent(Path originPath, Path destinationPath, Optional<List<Path>> exclusions, Boolean validateOrigin,
                           Boolean validateDestination)
      throws IOException {
    if (validateOrigin) {
      checkPathExist(originPath);
    }
    if (validateDestination) {
      checkPathExist(destinationPath);
    }

    CopyFileVisitor visitor = new CopyFileVisitor(originPath.toFile(), destinationPath.toFile());
    exclusions.ifPresent(e -> visitor.setExclusions(e));

    Files.walkFileTree(originPath, visitor);
  }

  private void copyFile(Path originPath, Path destinationPath, String destinationFileName) throws IOException {
    checkPathExist(originPath);
    checkPathExist(destinationPath);
    Files.copy(originPath, destinationPath.resolve(destinationFileName), StandardCopyOption.REPLACE_EXISTING);
  }

  private void checkPathExist(Path path) {
    checkArgument(path.toFile().exists(), "The path: " + path.toString() + " should exits");
  }

  /**
   * It creates classloader-model.json in META-INF/mule-artifact
   *
   * @param classLoaderModel the classloader model of the application being packaged
   * @return the created File containing the classloader model's JSON representation
   */
  public static File createClassLoaderModelJsonFile(ClassLoaderModel classLoaderModel, File destinationFolder) {
    File destinationFile = new File(destinationFolder, CLASSLOADER_MODEL_FILE_NAME);
    try {
      destinationFile.createNewFile();
      Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
      Writer writer = new FileWriter(destinationFile.getAbsolutePath());
      ClassLoaderModel parameterizedClassloaderModel = classLoaderModel.getParametrizedUriModel();
      gson.toJson(parameterizedClassloaderModel, writer);
      writer.close();
      return destinationFile;
    } catch (IOException e) {
      throw new RuntimeException("Could not create classloadermodel.json", e);
    }
  }
}

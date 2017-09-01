/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.sources;

import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_ARTIFACT_JSON;
import static org.mule.tools.api.packager.structure.FolderNames.CLASSES;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_SRC;
import static org.mule.tools.api.packager.structure.FolderNames.TARGET;
import static org.mule.tools.api.packager.structure.FolderNames.TEST_MULE;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mule.tools.api.classloader.model.ClassLoaderModel;
import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.util.CopyFileVisitor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * It knows how to generate the required content for each of the mandatory folder of the package
 * 
 */
public class MuleContentGenerator extends ContentGenerator {

  private static final String CLASSLOADER_MODEL_FILE_NAME = "classloader-model.json";

  public MuleContentGenerator(String groupId, String artifactId, String version, PackagingType packagingType,
                              Path projectBaseFolder, Path projectTargetFolder) {

    super(groupId, artifactId, version, packagingType, projectBaseFolder, projectTargetFolder);
  }

  /**
   * It create all the package content in the required folders
   * 
   * @throws IOException
   */
  @Override
  public void createContent() throws IOException {
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
    Path destinationPath = projectTargetFolder.resolve(CLASSES.value());

    copyContent(originPath, destinationPath, Optional.ofNullable(null), true, false);
  }

  /**
   * It creates the content that contains the test Mule source code. The name of the folder depends on the {@link PackagingType}
   * 
   * @throws IOException
   */
  public void createTestFolderContent() throws IOException {
    Path originPath = packagingType.getTestSourceFolderLocation(projectBaseFolder);
    Path destinationPath = projectTargetFolder.resolve(TEST_MULE.value()).resolve(originPath.getFileName());

    copyContent(originPath, destinationPath, Optional.ofNullable(null), false, true);
  }

  /**
   * It creates the {@link org.mule.tools.api.packager.structure.FolderNames#MULE_SRC} folder used by IDEs to import the project
   * source code
   * 
   * @throws IOException
   */
  public void createMetaInfMuleSourceFolderContent() throws IOException {
    Path originPath = projectBaseFolder;
    Path destinationPath = projectTargetFolder.resolve(META_INF.value()).resolve(MULE_SRC.value()).resolve(artifactId);

    List<Path> exclusions = new ArrayList<>();
    exclusions.add(projectBaseFolder.resolve(TARGET.value()));

    copyContent(originPath, destinationPath, Optional.of(exclusions));
  }

  /**
   * It creates classloader-model.json in META-INF/mule-artifact
   *
   * @param classLoaderModel the classloader model of the application being packaged
   */
  public void createApplicationClassLoaderModelJsonFile(ClassLoaderModel classLoaderModel) {
    File destinationFolder =
        projectTargetFolder.resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile();
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

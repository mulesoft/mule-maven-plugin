/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.sources;

import static java.lang.Boolean.FALSE;
import static org.mule.tools.api.packager.structure.FolderNames.*;
import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_DEPLOY_PROPERTIES;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.util.CopyFileVisitor;
import org.mule.tools.api.util.exclude.MuleExclusionMatcher;

/**
 * Generates the required content for each of the mandatory folders of a mule application package
 */
public class MuleContentGenerator extends ContentGenerator {

  public MuleContentGenerator(ProjectInformation projectInformation) {
    super(projectInformation);
  }

  /**
   * It creates all the package content in the required folders
   * 
   * @throws IOException
   */
  @Override
  public void createContent() throws IOException {
    createDescriptors();
  }

  /**
   * It creates the content that contains the productive Mule source code. It leaves it in the classes folder
   *
   * @throws IOException
   */
  public void createMuleSrcFolderContent() throws IOException {
    Path originPath = PackagingType.fromString(projectInformation.getPackaging())
        .getSourceFolderLocation(projectInformation.getProjectBaseFolder());
    Path destinationPath = projectInformation.getBuildDirectory().resolve(MULE.value());

    copyContent(originPath, destinationPath, null, true, false);
  }

  /**
   * It creates the content that contains the test Mule source code. The name of the folder depends on the {@link PackagingType}
   * 
   * @throws IOException
   */
  public void createTestFolderContent() throws IOException {
    Path originPath = PackagingType.fromString(projectInformation.getPackaging())
        .getTestSourceFolderLocation(projectInformation.getProjectBaseFolder());
    Path destinationPath = projectInformation.getBuildDirectory().resolve(TEST_MULE.value()).resolve(originPath.getFileName());

    copyContent(originPath, destinationPath, null, false, true);
  }

  public void createApiContent() throws IOException {
    Path originPath = projectInformation.getProjectStructure().getApiFolder();
    Path destinationPath = projectInformation.getBuildDirectory().resolve(API.value());
    copyContent(originPath, destinationPath, null, false, true);
  }

  public void createWsdlContent() throws IOException {
    Path originPath = projectInformation.getProjectStructure().getWsdlFolder();
    Path destinationPath = projectInformation.getBuildDirectory().resolve(WSDL.value());
    copyContent(originPath, destinationPath, null, false, true);
  }


  private void copyContent(Path originPath, Path destinationPath, List<Path> exclusions) throws IOException {
    copyContent(originPath, destinationPath, exclusions, true, true);
  }

  private void copyContent(Path originPath, Path destinationPath, List<Path> exclusions, Boolean validateOrigin,
                           Boolean validateDestination)
      throws IOException {
    copyContent(originPath, destinationPath, exclusions, validateOrigin, validateDestination, FALSE, FALSE);
  }

  private void copyContent(Path originPath, Path destinationPath, List<Path> exclusions, Boolean validateOrigin,
                           Boolean validateDestination, Boolean ignoreHiddenFiles, Boolean ignoreHiddenFolders)
      throws IOException {
    if (validateOrigin) {
      checkPathExist(originPath);
    }
    if (validateDestination) {
      checkPathExist(destinationPath);
    }


    CopyFileVisitor visitor =
        new CopyFileVisitor(originPath.toFile(), destinationPath.toFile(), ignoreHiddenFiles, ignoreHiddenFolders,
                            new MuleExclusionMatcher(projectInformation.getProjectBaseFolder()));
    if (exclusions != null) {
      visitor.setExclusions(exclusions);
    }

    Files.walkFileTree(originPath, visitor);
  }

  /**
   * It creates the descriptors files. The name of the the last one depends on the {@link PackagingType}
   *
   * @throws IOException
   */
  public void createDescriptors() throws IOException {
    copyDescriptorFile();
  }

  private void copyDescriptorFile() throws IOException {
    Path originPath = projectInformation.getProjectStructure().getMuleDeployPropertiesPath();
    Path destinationPath = projectInformation.getBuildDirectory().resolve(MULE.value());
    String destinationFileName = originPath.getFileName().toString();
    copyFile(originPath, destinationPath, destinationFileName);
  }
}

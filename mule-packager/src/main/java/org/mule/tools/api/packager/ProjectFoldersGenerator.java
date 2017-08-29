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
import org.mule.tools.api.packager.packaging.PackagingType;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.mule.tools.api.packager.structure.FolderNames;

/**
 * The goal of this class it ot generate the basic working folder structure to create a package.
 */
public class ProjectFoldersGenerator {

  private String groupId;
  private String artifactId;

  public ProjectFoldersGenerator(String groupId, String artifactId, PackagingType packagingType) {
    checkArgument(StringUtils.isNotEmpty(groupId), "The groupId must not be null nor empty");
    checkArgument(StringUtils.isNotEmpty(artifactId), "The artifactId must not be null nor empty");
    checkArgument(packagingType != null, "The packagingType must not be null");

    this.groupId = groupId;
    this.artifactId = artifactId;
  }

  /**
   * Generates all the folders required for a project to be properly build If any of the folders is already present on the
   * targetFolder they will not be overwritten
   *
   * @param targetFolder the target folder path
   */
  public void generate(Path targetFolder) {
    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), FolderNames.TEST_MULE.value(), FolderNames.MUNIT.value());

    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), FolderNames.META_INF.value(), FolderNames.MULE_SRC.value(),
                            artifactId);
    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), FolderNames.META_INF.value(), FolderNames.MAVEN.value(),
                            groupId, artifactId);
    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), FolderNames.META_INF.value(),
                            FolderNames.MULE_ARTIFACT.value());

    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), FolderNames.REPOSITORY.value());
  }

  private void createFolderIfNecessary(String... folderPath) {
    String path = StringUtils.join(folderPath, File.separator);
    File folder = new File(path);
    if (!folder.exists()) {
      folder.mkdirs();
    }
  }
}

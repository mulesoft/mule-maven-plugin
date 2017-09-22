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
 * Generates the basic working folder structure to create a package.
 */
public abstract class AbstractProjectFoldersGenerator {

  private String groupId;
  private String artifactId;
  private PackagingType packagingType;

  public AbstractProjectFoldersGenerator(String groupId, String artifactId, PackagingType packagingType) {
    checkArgument(StringUtils.isNotEmpty(groupId), "The groupId must not be null nor empty");
    checkArgument(StringUtils.isNotEmpty(artifactId), "The artifactId must not be null nor empty");
    checkArgument(packagingType != null, "The packagingType must not be null");

    this.groupId = groupId;
    this.artifactId = artifactId;
    this.packagingType = packagingType;
  }

  protected String getGroupId() {
    return groupId;
  }

  protected String getArtifactId() {
    return artifactId;
  }

  public PackagingType getPackagingType() {
    return packagingType;
  }

  /**
   * Generates all the folders required for a project to be properly build If any of the folders is already present on the
   * targetFolder they will not be overwritten
   *
   * @param targetFolder the target folder path
   */
  public abstract void generate(Path targetFolder);

  protected void createFolderIfNecessary(String... folderPath) {
    String path = StringUtils.join(folderPath, File.separator);
    File folder = new File(path);
    if (!folder.exists()) {
      folder.mkdirs();
    }
  }
}

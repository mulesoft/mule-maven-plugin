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

import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.packager.structure.FolderNames;

import java.nio.file.Path;

/**
 * Generates the basic working folder structure to create a mule application package.
 */
public class MuleProjectFoldersGenerator extends AbstractProjectFoldersGenerator {

  public MuleProjectFoldersGenerator(String groupId, String artifactId, PackagingType packagingType) {
    super(groupId, artifactId, packagingType);
  }

  @Override
  public void generate(Path targetFolder) {
    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), FolderNames.TEST_MULE.value(), FolderNames.MUNIT.value());

    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), FolderNames.META_INF.value(), FolderNames.MULE_SRC.value(),
                            getArtifactId());
    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), FolderNames.META_INF.value(), FolderNames.MAVEN.value(),
                            getGroupId(), getArtifactId());
    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), FolderNames.META_INF.value(),
                            FolderNames.MULE_ARTIFACT.value());
    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), FolderNames.REPOSITORY.value());

    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), FolderNames.CLASSES.value());

  }
}

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

import static org.mule.tools.api.packager.structure.FolderNames.APPLICATIONS;
import static org.mule.tools.api.packager.structure.FolderNames.DOMAIN;
import static org.mule.tools.api.packager.structure.FolderNames.MAVEN;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;

import java.nio.file.Path;

import org.mule.tools.api.packager.packaging.PackagingType;

/**
 * Generates the basic working folder structure to create a domain bundle package.
 */
public class DomainBundleProjectFoldersGenerator extends AbstractProjectFoldersGenerator {

  public DomainBundleProjectFoldersGenerator(String groupId, String artifactId, PackagingType packagingType) {
    super(groupId, artifactId, packagingType);
  }

  @Override
  public void generate(Path targetFolder) {
    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), DOMAIN.value());
    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), APPLICATIONS.value());
    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), META_INF.value(), MAVEN.value(), getGroupId(),
                            getArtifactId());
  }
}

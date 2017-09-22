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

/**
 * Factory of project folder generators. The corresponding generator type is based on the packaging type defined in the project
 * information.
 */
public class ProjectFoldersGeneratorFactory {

  public static AbstractProjectFoldersGenerator create(ProjectInformation projectInformation) {
    String groupId = projectInformation.getGroupId();
    String artifactId = projectInformation.getArtifactId();
    PackagingType packagingType = PackagingType.fromString(projectInformation.getPackaging());

    if (packagingType.equals(PackagingType.MULE_DOMAIN_BUNDLE)) {
      return new DomainBundleProjectFoldersGenerator(groupId, artifactId, packagingType);
    }
    return new MuleProjectFoldersGenerator(groupId, artifactId, packagingType);
  }
}

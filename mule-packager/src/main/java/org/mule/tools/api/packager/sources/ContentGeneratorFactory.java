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

import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.packaging.PackagingType;

/**
 * Factory of content generators. The corresponding generator type is based on the packaging type defined in the project
 * information.
 */
public class ContentGeneratorFactory {

  public static ContentGenerator create(ProjectInformation projectInformation) {
    PackagingType packaging = PackagingType.fromString(projectInformation.getPackaging());
    return packaging == PackagingType.MULE_DOMAIN_BUNDLE ? new DomainBundleContentGenerator(projectInformation)
        : new MuleContentGenerator(projectInformation);
  }
}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.packager.sources;

import org.apache.maven.model.Parent;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.packaging.PackagingType;

/**
 * Factory of content generators. The corresponding generator type is based on the packaging type defined in the project
 * information.
 */
public class ContentGeneratorFactory {

  public static ContentGenerator create(ProjectInformation projectInformation) {
    return create(projectInformation, null);
  }

  public static ContentGenerator create(ProjectInformation projectInformation, Parent parent) {
    PackagingType packaging = PackagingType.fromString(projectInformation.getPackaging());
    return packaging == PackagingType.MULE_DOMAIN_BUNDLE ? new DomainBundleContentGenerator(projectInformation)
        : new MuleContentGenerator(projectInformation, parent);
  }
}

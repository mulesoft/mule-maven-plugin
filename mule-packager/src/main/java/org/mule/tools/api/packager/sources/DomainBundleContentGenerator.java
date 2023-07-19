/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.packager.sources;

import org.mule.tools.api.packager.ProjectInformation;

import java.io.IOException;

/**
 * Generates the required content for each of the mandatory folders of a mule domain bundle package
 */
public class DomainBundleContentGenerator extends ContentGenerator {

  public DomainBundleContentGenerator(ProjectInformation projectInformation) {
    super(projectInformation, null);
  }

  @Override
  public void createContent() throws IOException {
    createMavenDescriptors();
  }
}

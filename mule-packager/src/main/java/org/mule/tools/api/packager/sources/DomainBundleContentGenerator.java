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

import java.io.IOException;

/**
 * Generates the required content for each of the mandatory folders of a mule domain bundle package
 */
public class DomainBundleContentGenerator extends ContentGenerator {

  public DomainBundleContentGenerator(ProjectInformation projectInformation) {
    super(projectInformation);
  }

  @Override
  public void createContent() throws IOException {
    createMavenDescriptors();
  }
}

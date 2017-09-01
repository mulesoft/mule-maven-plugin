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

import org.mule.tools.api.packager.packaging.PackagingType;

import java.io.IOException;
import java.nio.file.Path;

public class DomainBundleContentGenerator extends ContentGenerator {

  public DomainBundleContentGenerator(String groupId, String artifactId, String version, PackagingType packagingType,
                                      Path projectBaseFolder, Path projectTargetFolder) {
    super(groupId, artifactId, version, packagingType, projectBaseFolder, projectTargetFolder);
  }

  @Override
  public void createContent() throws IOException {
    createMavenDescriptors();
  }
}

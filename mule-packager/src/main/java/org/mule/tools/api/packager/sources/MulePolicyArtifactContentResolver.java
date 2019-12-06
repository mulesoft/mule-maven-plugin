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

import org.mule.maven.client.api.model.BundleDependency;
import org.mule.tools.api.packager.Pom;
import org.mule.tools.api.packager.structure.ProjectStructure;

import java.nio.file.Path;
import java.util.List;

/**
 * Resolves the content of resources defined in mule-artifact.json based on the project base folder.
 */
public class MulePolicyArtifactContentResolver extends MuleArtifactContentResolver {

  private static final String TEMPLATE_FILE = "template.xml";

  public MulePolicyArtifactContentResolver(ProjectStructure projectStructure, Pom pom,
                                           List<BundleDependency> bundleDependencies) {
    super(projectStructure, pom, bundleDependencies);
  }

  @Override
  protected boolean hasMuleAsRootElement(Path path) {
    return path.getFileName().toString().equals(TEMPLATE_FILE) || super.hasMuleAsRootElement(path);
  }

}

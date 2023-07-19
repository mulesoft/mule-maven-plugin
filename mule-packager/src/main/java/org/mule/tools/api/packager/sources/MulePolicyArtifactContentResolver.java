/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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

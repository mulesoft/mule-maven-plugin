/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.util;

import org.mule.maven.client.api.model.BundleDependency;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;

import java.util.List;

public interface Project {

  List<ArtifactCoordinates> getDirectDependencies();

  default List<ArtifactCoordinates> getDependencies() {
    return getDirectDependencies();
  }

  List<BundleDependency> getBundleDependencies();
}

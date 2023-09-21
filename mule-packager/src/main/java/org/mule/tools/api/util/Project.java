/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.util;

import org.mule.maven.pom.parser.api.model.BundleDependency;
import org.mule.tools.api.muleclassloader.model.ArtifactCoordinates;

import java.util.List;

public interface Project {

  List<ArtifactCoordinates> getDirectDependencies();

  default List<ArtifactCoordinates> getDependencies() {
    return getDirectDependencies();
  }

  List<BundleDependency> getBundleDependencies();
}

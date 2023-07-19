/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.packager;

import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface Pom {

  void persist(Path pom) throws IOException;

  String getGroupId();

  String getArtifactId();

  String getVersion();

  List<ArtifactCoordinates> getDependencies();

  List<Path> getResourcesLocation();
}

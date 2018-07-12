/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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

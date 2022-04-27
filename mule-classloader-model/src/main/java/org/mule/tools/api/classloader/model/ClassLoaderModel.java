/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.classloader.model;

import java.util.List;
import java.util.Set;

public interface ClassLoaderModel<T extends ClassLoaderModel<T>> {

  String getVersion();

  T setVersion(String version);

  ArtifactCoordinates getArtifactCoordinates();

  T setArtifactCoordinates(ArtifactCoordinates artifactCoordinates);

  List<Artifact> getDependencies();

  T setDependencies(List<Artifact> dependencies);

  String[] getPackages();

  T setPackages(String[] packages);

  String[] getResources();

  T setResources(String[] resources);

  List<Plugin> getAdditionalPluginDependencies();

  T setAdditionalPluginDependencies(List<Plugin> additionalPluginDependencies);

  ClassLoaderModel<T> getParametrizedUriModel();

  Set<Artifact> getArtifacts();
}

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

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AppClassLoaderModel extends ClassLoaderModel {

  private List<Plugin> additionalPluginDependencies = new ArrayList<>();

  public AppClassLoaderModel(String version, ArtifactCoordinates artifactCoordinates) {
    super(version, artifactCoordinates);
  }

  @Override
  protected ClassLoaderModel doGetParameterizedUriModel() {
    AppClassLoaderModel copy = new AppClassLoaderModel(getVersion(), getArtifactCoordinates());
    List<Plugin> pluginsCopy =
        additionalPluginDependencies.stream().map(Plugin::copyWithParameterizedDependenciesUri).collect(toList());
    copy.setAdditionalPluginDependencies(pluginsCopy);
    return copy;
  }

  @Override
  public Set<Artifact> getArtifacts() {
    Set<Artifact> artifacts = super.getArtifacts();
    additionalPluginDependencies.forEach(
                                         plugin -> artifacts.addAll(plugin.getAdditionalDependencies()));
    return artifacts;
  }

  public Optional<List<Plugin>> getAdditionalPluginDependencies() {
    return ofNullable(additionalPluginDependencies);
  }

  public void setAdditionalPluginDependencies(List<Plugin> additionalPluginDependencies) {
    this.additionalPluginDependencies = additionalPluginDependencies;
  }
}

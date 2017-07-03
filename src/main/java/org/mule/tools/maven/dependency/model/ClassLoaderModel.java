/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.dependency.model;

import org.mule.tools.maven.dependency.util.DependencyUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.tools.maven.dependency.util.DependencyUtils.isValidMulePlugin;

public class ClassLoaderModel {

  private String version;
  private ArtifactCoordinates artifactCoordinates;
  private SortedSet<Dependency> dependencies;
  private SortedMap<Dependency, SortedSet<Dependency>> mulePlugins;

  public ClassLoaderModel(String version, ArtifactCoordinates artifactCoordinates) {
    checkArgument(version != null, "Version cannot be null");
    checkArgument(artifactCoordinates != null, "Artifact coordinates cannot be null");
    this.artifactCoordinates = artifactCoordinates;
    this.version = version;
    this.dependencies = new TreeSet<>();
    this.mulePlugins = new TreeMap<>();
  }

  public String getVersion() {
    return version;
  }

  public ArtifactCoordinates getArtifactCoordinates() {
    return artifactCoordinates;
  }

  public SortedSet<Dependency> getDependencies() {
    return this.dependencies;
  }

  public void setDependencies(SortedSet<Dependency> dependencies) {
    this.dependencies = dependencies;
  }

  public SortedMap<Dependency, SortedSet<Dependency>> getMulePlugins() {
    return this.mulePlugins;
  }

  public void setMulePlugins(SortedMap<Dependency, SortedSet<Dependency>> mulePlugins) {
    validatePlugins(mulePlugins.keySet());
    this.mulePlugins = mulePlugins;
  }


  protected void validatePlugins(Set<Dependency> dependencies) {
    SortedSet<Dependency> notMulePlugins =
        dependencies.stream().filter(dependency -> !DependencyUtils.isValidMulePlugin(dependency))
            .collect(Collectors.toCollection(TreeSet::new));
    if (!notMulePlugins.isEmpty()) {
      throw new IllegalArgumentException("The following dependencies are not mule plugins but are trying to be added as such: "
          + notMulePlugins.stream().map(Dependency::toString).collect(Collectors.toList()));
    }
  }

  public void addMulePlugin(Dependency dependency, SortedSet<Dependency> pluginDependencies) {
    if (!isValidMulePlugin(dependency)) {
      throw new IllegalArgumentException("The dependency " + dependency + " is not a valid mule plugin dependency");
    }
    SortedSet<Dependency> newDependencies = this.mulePlugins.getOrDefault(dependency, pluginDependencies);
    newDependencies.addAll(pluginDependencies);
    this.mulePlugins.put(dependency, newDependencies);
  }
}

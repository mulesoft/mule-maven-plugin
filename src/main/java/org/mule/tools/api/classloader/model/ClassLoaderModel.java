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

import org.mule.tools.api.classloader.model.util.ArtifactUtils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.tools.api.classloader.model.util.DefaultMavenRepositoryLayoutUtils.getFormattedOutputDirectory;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.isValidMulePlugin;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toArtifact;

public class ClassLoaderModel {

  private String version;
  private ArtifactCoordinates artifactCoordinates;
  private List<Artifact> dependencies = new ArrayList<>();
  private Map<Artifact, List<Artifact>> mulePlugins = new TreeMap<>();

  public ClassLoaderModel(String version, ArtifactCoordinates artifactCoordinates) {
    setArtifactCoordinates(artifactCoordinates);
    setVersion(version);
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    checkArgument(version != null, "Version cannot be null");
    this.version = version;
  }

  public ArtifactCoordinates getArtifactCoordinates() {
    return artifactCoordinates;
  }

  public void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates) {
    checkArgument(artifactCoordinates != null, "Artifact coordinates cannot be null");
    this.artifactCoordinates = artifactCoordinates;
  }

  public List<Artifact> getDependencies() {
    return this.dependencies;
  }

  public void setDependencies(List<Artifact> dependencies) {
    this.dependencies = dependencies;
  }

  public Map<Artifact, List<Artifact>> getMulePlugins() {
    return this.mulePlugins;
  }

  public void setMulePlugins(Map<Artifact, List<Artifact>> mulePlugins) {
    validatePlugins(mulePlugins.keySet());
    this.mulePlugins.putAll(mulePlugins);
  }


  protected void validatePlugins(Set<Artifact> artifacts) {
    SortedSet<Artifact> notMulePlugins =
        artifacts.stream().filter(artifact -> !ArtifactUtils.isValidMulePlugin(artifact))
            .collect(Collectors.toCollection(TreeSet::new));
    if (!notMulePlugins.isEmpty()) {
      throw new IllegalArgumentException("The following artifacts are not mule plugins but are trying to be added as such: "
          + notMulePlugins.stream().map(Artifact::toString).collect(Collectors.toList()));
    }
  }

  public void addMulePlugin(Artifact artifact, List<Artifact> pluginDependencies) {
    if (!isValidMulePlugin(artifact)) {
      throw new IllegalArgumentException("The artifact " + artifact + " is not a valid mule plugin artifact");
    }
    List<Artifact> newDependencies = this.mulePlugins.getOrDefault(artifact, pluginDependencies);
    newDependencies.addAll(pluginDependencies);
    this.mulePlugins.put(artifact, newDependencies);
  }

  public Set<org.apache.maven.artifact.Artifact> getArtifacts() {
    Set<Artifact> allDependencies = new TreeSet<>();

    allDependencies.addAll(dependencies);
    allDependencies.addAll(mulePlugins.keySet());
    mulePlugins.values().forEach(allDependencies::addAll);

    return allDependencies.stream().map(ArtifactUtils::toArtifact).collect(Collectors.toSet());
  }

  public void updateUriSuffixToLocalRepository() {
    dependencies.forEach(this::updateUriToLocalRepository);
    mulePlugins.keySet().forEach(this::updateUriToLocalRepository);
    mulePlugins.values().forEach(list -> list.forEach(this::updateUriToLocalRepository));
  }

  private void updateUriToLocalRepository(Artifact artifact) {
    String repositoryFolderName = "repository/";
    String newUriPath = getFormattedOutputDirectory(new File(repositoryFolderName), toArtifact(artifact)).getPath();
    try {
      URI newUri = new URI(newUriPath);
      artifact.setUri(newUri);
    } catch (URISyntaxException e) {
      // the artifact URI is not updated then the original one is kept
    }
  }
}

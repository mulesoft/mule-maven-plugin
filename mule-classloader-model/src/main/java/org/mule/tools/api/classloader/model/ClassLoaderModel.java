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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ClassLoaderModel {

  private String version;
  private ArtifactCoordinates artifactCoordinates;
  private List<Artifact> dependencies = new ArrayList<>();

  private String[] packages;
  private String[] resources;

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

  public Set<Artifact> getArtifacts() {
    Set<Artifact> allDependencies = new TreeSet<>();
    allDependencies.addAll(dependencies);
    return allDependencies.stream().collect(toSet());
  }

  public ClassLoaderModel getParametrizedUriModel() {
    ClassLoaderModel copy = doGetParameterizedUriModel();
    List<Artifact> dependenciesCopy = dependencies.stream().map(Artifact::copyWithParameterizedUri).collect(toList());
    copy.setDependencies(dependenciesCopy);
    copy.setPackages(getPackages());
    copy.setResources(getResources());
    return copy;
  }

  protected ClassLoaderModel doGetParameterizedUriModel() {
    ClassLoaderModel copy = new ClassLoaderModel(version, artifactCoordinates);
    return copy;
  }


  /**
   * Introspection of this artifact, it will contain the array of Java packages for this artifact.
   * @return {@link String[]} with the packages discovered for this artifact.
   */
  public String[] getPackages() {
    return packages;
  }

  public void setPackages(String[] packages) {
    this.packages = packages;
  }

  /**
   * Introspection of this artifact, it will contain the array of resources for this artifact.
   * @return {@link String[]} with the resources discovered for this artifact.
   */
  public String[] getResources() {
    return resources;
  }

  public void setResources(String[] resources) {
    this.resources = resources;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !(o instanceof ClassLoaderModel)) {
      return false;
    }

    ClassLoaderModel that = (ClassLoaderModel) o;

    return getArtifactCoordinates().equals(that.getArtifactCoordinates());
  }

  @Override
  public int hashCode() {
    return getArtifactCoordinates().hashCode();
  }
}

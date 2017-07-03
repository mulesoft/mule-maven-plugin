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

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

public class Dependency implements Comparable {

  private ArtifactCoordinates artifactCoordinates;
  private URI path;

  public Dependency(ArtifactCoordinates artifactCoordinates, URI path) {
    checkNotNull(artifactCoordinates, "Artifact coordinates cannot be null");
    checkNotNull(path, "Path cannot be null");
    this.artifactCoordinates = artifactCoordinates;
    this.path = path;
  }

  public ArtifactCoordinates getArtifactCoordinates() {
    return artifactCoordinates;
  }

  public URI getPath() {
    return path;
  }

  @Override
  public String toString() {
    return "Dependency{" +
        "artifactCoordinates=[" + artifactCoordinates.toString() +
        "], path=" + path.getPath().toString() +
        '}';
  }

  @Override
  public int compareTo(Object that) {
    return getMavenCoordinates(this).compareTo(getMavenCoordinates((Dependency) that));
  }

  private String getMavenCoordinates(Dependency dependency) {
    ArtifactCoordinates coordinates = dependency.getArtifactCoordinates();
    return coordinates.getGroupId() + ":" + coordinates.getArtifactId() + ":" + coordinates.getVersion();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Dependency that = (Dependency) o;

    return getArtifactCoordinates().equals(that.getArtifactCoordinates());
  }

  @Override
  public int hashCode() {
    return getArtifactCoordinates().hashCode();
  }
}

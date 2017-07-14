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

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

public class Artifact implements Comparable {

  private ArtifactCoordinates artifactCoordinates;
  private URI uri;

  public Artifact(ArtifactCoordinates artifactCoordinates, URI uri) {
    setArtifactCoordinates(artifactCoordinates);
    setUri(uri);
  }

  public ArtifactCoordinates getArtifactCoordinates() {
    return artifactCoordinates;
  }

  public URI getUri() {
    return uri;
  }

  public void setUri(URI uri) {
    checkNotNull(uri, "Uri cannot be null");
    this.uri = uri;
  }

  private void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates) {
    checkNotNull(artifactCoordinates, "Artifact coordinates cannot be null");
    this.artifactCoordinates = artifactCoordinates;
  }

  @Override
  public String toString() {
    return artifactCoordinates.toString();
  }

  @Override
  public int compareTo(Object that) {
    return getMavenCoordinates(this).compareTo(getMavenCoordinates((Artifact) that));
  }

  private String getMavenCoordinates(Artifact artifact) {
    ArtifactCoordinates coordinates = artifact.getArtifactCoordinates();
    return coordinates.getGroupId() + ":" + coordinates.getArtifactId() + ":" + coordinates.getVersion();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Artifact that = (Artifact) o;

    return getArtifactCoordinates().equals(that.getArtifactCoordinates());
  }

  @Override
  public int hashCode() {
    return getArtifactCoordinates().hashCode();
  }
}

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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toArtifact;
import static org.mule.tools.api.classloader.model.util.DefaultMavenRepositoryLayoutUtils.getFormattedOutputDirectory;

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
    return this.getSimplifiedMavenCoordinates().compareTo(((Artifact) that).getSimplifiedMavenCoordinates());
  }

  protected String getSimplifiedMavenCoordinates() {
    ArtifactCoordinates coordinates = this.getArtifactCoordinates();
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

  public Artifact copyWithParameterizedUri() {
    Artifact newArtifact = new Artifact(artifactCoordinates, uri);
    File repositoryFolder = new File("repository");
    String newUriPath = getFormattedOutputDirectory(repositoryFolder, toArtifact(this)).getPath();
    try {
      newArtifact.setUri(new URI(newUriPath));
    } catch (URISyntaxException e) {
      throw new RuntimeException("Could not generate URI for resource, the given path is invalid: " + newUriPath, e);
    }
    return newArtifact;
  }
}

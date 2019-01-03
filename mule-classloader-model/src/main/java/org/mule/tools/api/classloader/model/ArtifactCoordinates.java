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

import org.apache.commons.lang3.StringUtils;

public class ArtifactCoordinates {

  public static final String DEFAULT_ARTIFACT_TYPE = "jar";

  private String groupId;
  private String artifactId;
  private String version;
  private String type;
  private String classifier;
  private String scope;

  /**
   * Constructor added so that child classes can be instantiated by reflection.
   */
  protected ArtifactCoordinates() {
    setType(DEFAULT_ARTIFACT_TYPE);
  }

  public ArtifactCoordinates(String groupId, String artifactId, String version) {
    this(groupId, artifactId, version, DEFAULT_ARTIFACT_TYPE, null);
  }

  public ArtifactCoordinates(String groupId, String artifactId, String version, String type,
                             String classifier) {
    setGroupId(groupId);
    setArtifactId(artifactId);
    setVersion(version);
    setType(type);
    setClassifier(classifier);
  }

  public ArtifactCoordinates(String groupId, String artifactId, String version, String type,
                             String classifier, String scope) {
    setGroupId(groupId);
    setArtifactId(artifactId);
    setVersion(version);
    setType(type);
    setClassifier(classifier);
    setScope(scope);
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    checkArgument(StringUtils.isNotBlank(groupId), "Group id cannot be null nor blank");
    this.groupId = groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    checkArgument(StringUtils.isNotBlank(artifactId), "Artifact id can not be null nor blank");
    this.artifactId = artifactId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    checkArgument(StringUtils.isNotBlank(version), "Version can not be null nor blank");
    this.version = version;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    checkArgument(StringUtils.isNotBlank(type), "Type can not be null nor blank");
    this.type = type;
  }

  public String getClassifier() {
    return classifier;
  }

  public void setClassifier(String classifier) {
    this.classifier = classifier;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  @Override
  public String toString() {
    return groupId + ':' + artifactId + ':' + version + ':' + type + (StringUtils.isNotBlank(classifier) ? ':' + classifier : "");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ArtifactCoordinates that = (ArtifactCoordinates) o;

    if (!getArtifactId().equals(that.getArtifactId())) {
      return false;
    }
    if (!getGroupId().equals(that.getGroupId())) {
      return false;
    }
    if (!StringUtils.equals(getClassifier(), that.getClassifier())) {
      return false;
    }
    return getVersion().equals(that.getVersion());
  }

  @Override
  public int hashCode() {
    int result = getArtifactId().hashCode();
    result = 31 * result + getGroupId().hashCode();
    result = 31 * result + getVersion().hashCode();
    if (getClassifier() != null) {
      result = 31 * result + getClassifier().hashCode();
    }
    return result;
  }
}

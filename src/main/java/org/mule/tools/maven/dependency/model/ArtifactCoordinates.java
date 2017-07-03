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

import org.apache.commons.lang.StringUtils;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

public class ArtifactCoordinates {

  private String artifactId;
  private String groupId;
  private String version;
  private Optional<String> type;
  private Optional<String> classifier;

  public ArtifactCoordinates(String groupId, String artifactId, String version) {
    this(groupId, artifactId, version, Optional.of("jar"), Optional.empty());
  }

  public ArtifactCoordinates(String groupId, String artifactId, String version, Optional<String> type,
                             Optional<String> classifier) {
    checkArgument(StringUtils.isNotBlank(groupId), "Group id cannot be null nor blank");
    checkArgument(StringUtils.isNotBlank(artifactId), "Artifact id cannot be null nor blank");
    checkArgument(StringUtils.isNotBlank(version), "Version cannot be null nor blank");
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.type = type;
    this.classifier = classifier;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getVersion() {
    return version;
  }

  public Optional<String> getType() {
    return type;
  }

  public Optional<String> getClassifier() {
    return classifier;
  }

  @Override
  public String toString() {
    return "ArtifactCoordinates{" +
        "artifactId='" + artifactId + '\'' +
        ", groupId='" + groupId + '\'' +
        ", version='" + version + '\'' +
        ", type=" + type +
        ", classifier=" + classifier +
        '}';
  }
}

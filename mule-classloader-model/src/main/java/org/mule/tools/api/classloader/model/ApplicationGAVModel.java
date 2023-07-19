/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.classloader.model;

import static com.google.common.base.Preconditions.checkNotNull;

public class ApplicationGAVModel {

  private final String groupId;
  private final String artifactId;
  private final String version;

  public ApplicationGAVModel(String groupId, String artifactId, String version) {
    checkNotNull(groupId, "groupId cannot be null");
    checkNotNull(artifactId, "artifactId cannot be null");
    checkNotNull(version, "version cannot be null");
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getVersion() {
    return version;
  }

}

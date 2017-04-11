/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.mule;

import static java.lang.String.*;

public class ArtifactDescription {

  private String groupId;
  private String artifactId;
  private String version;
  private String type;

  public ArtifactDescription() {

  }

  public ArtifactDescription(String groupId) {
    String[] elements = groupId.split(":");
    groupId = elements[0];
    artifactId = elements[1];
    version = elements[2];
    type = elements[3];
  }

  public ArtifactDescription(String groupId, String artifactId, String version, String type) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.type = type;
  }

  public String toString() {
    return format("%s:%s:%s:%s", groupId, artifactId, version, type);
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getContentDirectory() {
    if ("mule-standalone".equals(artifactId)) {
      return "mule-standalone-" + version;
    } else {
      return "mule-enterprise-standalone-" + version;
    }
  }
}

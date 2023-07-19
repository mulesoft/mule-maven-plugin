/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.fabric.model;

public class AssetReference {

  public String groupId;
  public String artifactId;
  public String version;
  public String packaging;


  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setPackaging(String packaging) {
    this.packaging = packaging;
  }
}

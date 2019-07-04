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

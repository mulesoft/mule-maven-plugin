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

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SharedLibraryDependency {

  private final String groupId;
  private final String artifactId;

  public SharedLibraryDependency setGroupId(String groupId) {
    return getCopyBuilder()
        .groupId(groupId)
        .build();
  }

  public SharedLibraryDependency setArtifactId(String artifactId) {
    return getCopyBuilder()
        .artifactId(artifactId)
        .build();
  }

  private SharedLibraryDependency.SharedLibraryDependencyBuilder getCopyBuilder() {
    return SharedLibraryDependency.builder()
        .groupId(groupId)
        .artifactId(artifactId);
  }
}

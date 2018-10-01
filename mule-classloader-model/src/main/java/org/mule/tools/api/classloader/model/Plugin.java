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

import java.util.List;
import java.util.stream.Collectors;

/**
 * POJO to modelate a plugin that will declare additional dependencies.
 *
 * @since 3.2.0
 */
public class Plugin {

  private String groupId;

  private String artifactId;

  private List<Artifact> additionalDependencies;

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

  public List<Artifact> getAdditionalDependencies() {
    return additionalDependencies;
  }

  public void setAdditionalDependencies(List<Artifact> dependencies) {
    this.additionalDependencies = dependencies;
  }

  public Plugin copyWithParameterizedDependenciesUri() {
    Plugin copy = new Plugin();
    copy.setGroupId(this.groupId);
    copy.setArtifactId(this.artifactId);
    List<Artifact> dependenciesCopy =
        additionalDependencies.stream().map(Artifact::copyWithParameterizedUri).collect(Collectors.toList());
    copy.setAdditionalDependencies(dependenciesCopy);
    return copy;
  }
}

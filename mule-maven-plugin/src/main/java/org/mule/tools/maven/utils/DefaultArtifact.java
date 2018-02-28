/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.apache.maven.artifact.Artifact.SCOPE_COMPILE;
import static org.apache.maven.artifact.Artifact.SCOPE_RUNTIME;

public class DefaultArtifact implements org.mule.tools.api.util.Artifact {

  org.apache.maven.artifact.Artifact artifact;

  public DefaultArtifact(org.apache.maven.artifact.Artifact artifact) {
    this.artifact = artifact;
  }

  @Override
  public List<String> getDependencyTrail() {
    return artifact.getDependencyTrail();
  }

  @Override
  public boolean isOptional() {
    return artifact.isOptional();
  }

  @Override
  public boolean isCompileScope() {
    return StringUtils.equals(artifact.getScope(), SCOPE_COMPILE);
  }

  @Override
  public boolean isRuntimeScope() {
    return StringUtils.equals(artifact.getScope(), SCOPE_RUNTIME);
  }

  /**
   * The first element on dependency tail is the project that is compiled. The project's groupId can be anything, we don't filter
   * it.
   */
  @Override
  public List<String> getOnlyDependenciesTrail() {
    List<String> trail = artifact.getDependencyTrail();
    return trail.size() > 1 ? trail.subList(1, trail.size()) : new ArrayList<String>();
  }

  @Override
  public File getFile() {
    return artifact.getFile();
  }

  @Override
  public String getGroupId() {
    return artifact.getGroupId();
  }
}

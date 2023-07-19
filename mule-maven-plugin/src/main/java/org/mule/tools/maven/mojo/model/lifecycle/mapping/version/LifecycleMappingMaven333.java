/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.maven.mojo.model.lifecycle.mapping.version;

import org.apache.maven.lifecycle.mapping.Lifecycle;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.project.ProjectLifecycleMapping;

public class LifecycleMappingMaven333 extends LifecycleMappingMavenVersionless {

  private final ProjectLifecycleMapping mapping;

  public LifecycleMappingMaven333(ProjectLifecycleMapping mapping) {
    this.mapping = mapping;
  }

  @Override
  public String buildGoals(String goals) {
    return goals;
  }

  @Override
  public void setLifecyclePhases(Lifecycle lifecycle) {
    lifecycle.setPhases(mapping.getLifecyclePhases(this));
  }
}

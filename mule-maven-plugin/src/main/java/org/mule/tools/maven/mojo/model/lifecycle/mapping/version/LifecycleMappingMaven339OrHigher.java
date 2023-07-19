/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.maven.mojo.model.lifecycle.mapping.version;

import org.apache.maven.lifecycle.mapping.Lifecycle;
import org.apache.maven.lifecycle.mapping.LifecyclePhase;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.project.ProjectLifecycleMapping;

public class LifecycleMappingMaven339OrHigher extends LifecycleMappingMavenVersionless {

  private final ProjectLifecycleMapping mapping;

  public LifecycleMappingMaven339OrHigher(ProjectLifecycleMapping mapping) {
    this.mapping = mapping;
  }

  @Override
  public LifecyclePhase buildGoals(String goals) {
    return new LifecyclePhase(goals);
  }

  @Override
  public void setLifecyclePhases(Lifecycle lifecycle) {
    lifecycle.setLifecyclePhases(mapping.getLifecyclePhases(this));
  }
}

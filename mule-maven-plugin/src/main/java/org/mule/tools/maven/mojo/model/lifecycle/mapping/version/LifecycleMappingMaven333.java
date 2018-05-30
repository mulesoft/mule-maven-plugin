/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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

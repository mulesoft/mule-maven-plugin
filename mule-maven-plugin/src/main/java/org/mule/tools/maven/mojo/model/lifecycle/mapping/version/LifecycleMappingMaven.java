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

import com.google.common.collect.ImmutableMap;
import org.apache.maven.lifecycle.mapping.Lifecycle;
import org.apache.maven.lifecycle.mapping.LifecyclePhase;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.project.ProjectLifecycleMapping;

import java.util.Map;

public class LifecycleMappingMaven {

  private static final String DEFAULT_LIFECYCLE_ID = "default";

  private final ProjectLifecycleMapping mapping;

  public LifecycleMappingMaven(ProjectLifecycleMapping mapping) {
    this.mapping = mapping;
  }

  public Map<String, Lifecycle> getLifecycles() {
    Lifecycle lifecycle = getDefaultLifecycle();
    return ImmutableMap.of(lifecycle.getId(), lifecycle);
  }

  protected Lifecycle getDefaultLifecycle() {
    Lifecycle lifecycle = new Lifecycle();
    lifecycle.setId(DEFAULT_LIFECYCLE_ID);
    setLifecyclePhases(lifecycle);
    return lifecycle;
  }

  public LifecyclePhase buildGoals(String goals) {
    return new LifecyclePhase(goals);
  }

  public void setLifecyclePhases(Lifecycle lifecycle) {
    lifecycle.setLifecyclePhases(mapping.getLifecyclePhases(this));
  }
}

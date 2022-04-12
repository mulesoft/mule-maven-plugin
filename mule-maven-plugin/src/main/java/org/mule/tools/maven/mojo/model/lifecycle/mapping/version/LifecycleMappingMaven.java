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
import java.util.function.Consumer;
import java.util.function.Function;

public class LifecycleMappingMaven {

  private static class LifecyclePhaseGoals<T> {

    private final Function<String, Object> goalsBuilder;
    private final Function<Lifecycle, Consumer<Map<String, T>>> lifecyclePhasesSetter;

    public LifecyclePhaseGoals(Function<String, Object> goalsBuilder,
                               Function<Lifecycle, Consumer<Map<String, T>>> lifecyclePhasesSetter) {
      this.goalsBuilder = goalsBuilder;
      this.lifecyclePhasesSetter = lifecyclePhasesSetter;
    }

    public Function<String, Object> getGoalsBuilder() {
      return goalsBuilder;
    }

    public Consumer<Map<String, T>> getLifecyclePhasesSetter(Lifecycle lifecycle) {
      return lifecyclePhasesSetter.apply(lifecycle);
    }
  }

  private static final String DEFAULT_LIFECYCLE_ID = "default";
  private static final LifecyclePhaseGoals<?> LIFECYCLE_PHASE_GOALS;

  static {
    LifecyclePhaseGoals<?> lifecyclePhaseGoals;
    try {
      Class.forName("org.apache.maven.lifecycle.mapping.LifecyclePhase");
      lifecyclePhaseGoals =
          new LifecyclePhaseGoals<LifecyclePhase>(LifecyclePhase::new, lifecycle -> lifecycle::setLifecyclePhases);
    } catch (ClassNotFoundException e) {
      lifecyclePhaseGoals = new LifecyclePhaseGoals<String>(goals -> goals, lifecycle -> lifecycle::setPhases);
    }
    LIFECYCLE_PHASE_GOALS = lifecyclePhaseGoals;
  }

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

  @SuppressWarnings("unchecked")
  public <V> V buildGoals(String goals) {
    return (V) LIFECYCLE_PHASE_GOALS.getGoalsBuilder().apply(goals);
  }

  public void setLifecyclePhases(Lifecycle lifecycle) {
    LIFECYCLE_PHASE_GOALS.getLifecyclePhasesSetter(lifecycle)
        .accept(mapping.getLifecyclePhases(this));
  }
}

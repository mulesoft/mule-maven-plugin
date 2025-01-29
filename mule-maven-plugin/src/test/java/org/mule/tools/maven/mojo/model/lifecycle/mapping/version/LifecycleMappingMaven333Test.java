/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo.model.lifecycle.mapping.version;

import org.apache.maven.lifecycle.mapping.Lifecycle;
import org.junit.jupiter.api.Test;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.project.ProjectLifecycleMapping;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LifecycleMappingMaven333Test {

  @SuppressWarnings("unchecked")
  @Test
  void setLifecyclePhasesTest() {
    ProjectLifecycleMapping mapping = mock(ProjectLifecycleMapping.class);
    Lifecycle lifecycle = mock(Lifecycle.class);

    LifecycleMappingMaven333 maven333 = new LifecycleMappingMaven333(mapping);
    maven333.setLifecyclePhases(lifecycle);

    verify(mapping).getLifecyclePhases(any(LifecycleMappingMaven333.class));
    verify(lifecycle).setPhases(any(Map.class));
  }
}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo.model.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.CLEAN;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.COMPILE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.DEPLOY;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.GENERATE_SOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.GENERATE_TEST_SOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.INITIALIZE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.INSTALL;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PACKAGE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PROCESS_CLASSES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PROCESS_RESOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PROCESS_SOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PROCESS_TEST_RESOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.SITE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.TEST;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.TEST_COMPILE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.VALIDATE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.VERIFY;

import org.apache.maven.lifecycle.mapping.LifecyclePhase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.project.MuleLifecycleMapping;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenVersionless;

import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

import org.apache.maven.lifecycle.DefaultLifecycles;
import org.apache.maven.lifecycle.mapping.Lifecycle;

class MuleLifecycleMappingMavenVersionlessTest {

  private final String DEFAULT_LIFECYCLE = DefaultLifecycles.STANDARD_LIFECYCLES[0];

  @Test
  void getLifecyclePhases() {
    Set<MavenLifecyclePhase> expectedLifecyclePhases =
        Sets.newHashSet(CLEAN, VALIDATE, INITIALIZE, GENERATE_SOURCES, PROCESS_SOURCES, PROCESS_RESOURCES, COMPILE,
                        PROCESS_CLASSES,
                        GENERATE_TEST_SOURCES, PROCESS_TEST_RESOURCES, TEST_COMPILE, TEST, PACKAGE, VERIFY, INSTALL,
                        DEPLOY, SITE);

    LifecycleMappingMavenVersionless lifecycleMappingMavenVersionlessMock = mock(LifecycleMappingMavenVersionless.class);
    when(lifecycleMappingMavenVersionlessMock.buildGoals(any())).thenReturn("");
    Map<String, Object> lifecyclePhases = (new MuleLifecycleMapping()).getLifecyclePhases(lifecycleMappingMavenVersionlessMock);


    assertThat(lifecyclePhases.keySet())
        .as("The number of lifecycle phases is wrong")
        .hasSize(expectedLifecyclePhases.size());

    expectedLifecyclePhases
        .forEach(expectedPhase -> assertThat(lifecyclePhases.containsKey(expectedPhase.id()))
            .as("Missing lifecycle phase: " + expectedPhase.id()).isTrue());
  }

  @Disabled
  @Test
  void getMuleDefaultLifecycleTest() {
    MuleLifecycleMapping muleLifecycleMappingMaven = new MuleLifecycleMapping();

    Map<String, Lifecycle> lifecycles = muleLifecycleMappingMaven.getLifecycles();
    Lifecycle defaultLifecycle = lifecycles.get(DEFAULT_LIFECYCLE);
    Map<String, LifecyclePhase> phases = defaultLifecycle.getLifecyclePhases();

    LifecycleMappingMavenVersionless lifecycleMappingMavenVersionlessMock = mock(LifecycleMappingMavenVersionless.class);
    when(lifecycleMappingMavenVersionlessMock.buildGoals(any())).thenReturn("");
    Map<String, Object> expectedPhases = (new MuleLifecycleMapping()).getLifecyclePhases(lifecycleMappingMavenVersionlessMock);

    for (String phase : phases.keySet()) {
      assertThat(expectedPhases.containsKey(phase)).as("Current phase is not defined in the expected lifecycle map").isTrue();
    }

    for (String phase : expectedPhases.keySet()) {
      assertThat(phases.containsKey(phase)).as("Current phase is not defined in the actual lifecycle map").isTrue();
    }
  }

}

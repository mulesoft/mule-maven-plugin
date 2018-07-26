/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo.model.lifecycle;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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

import org.mule.tools.maven.mojo.model.lifecycle.mapping.project.MuleLifecycleMapping;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenVersionless;

import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

import org.apache.maven.lifecycle.DefaultLifecycles;
import org.apache.maven.lifecycle.mapping.Lifecycle;
import org.junit.Test;

public class MuleLifecycleMappingMavenVersionlessTest {

  private final String DEFAULT_LIFECYCLE = DefaultLifecycles.STANDARD_LIFECYCLES[0];

  @Test
  public void getLifecyclePhases() {

    Set<MavenLifecyclePhase> expectedLifecyclePhases =
        Sets.newHashSet(CLEAN, VALIDATE, INITIALIZE, GENERATE_SOURCES, PROCESS_SOURCES, PROCESS_RESOURCES, COMPILE,
                        PROCESS_CLASSES,
                        GENERATE_TEST_SOURCES, PROCESS_TEST_RESOURCES, TEST_COMPILE, TEST, PACKAGE, VERIFY, INSTALL,
                        DEPLOY, SITE);

    LifecycleMappingMavenVersionless lifecycleMappingMavenVersionlessMock = mock(LifecycleMappingMavenVersionless.class);
    when(lifecycleMappingMavenVersionlessMock.buildGoals(any())).thenReturn("");
    Map lifecyclePhases = (new MuleLifecycleMapping()).getLifecyclePhases(lifecycleMappingMavenVersionlessMock);


    assertThat("The number of lifecycle phases is wrong", lifecyclePhases.keySet().size(),
               is(expectedLifecyclePhases.size()));

    expectedLifecyclePhases.forEach(expectedPhase -> assertThat("Missing lifecycle phase: " + expectedPhase.id(),
                                                                lifecyclePhases.containsKey(expectedPhase.id()), is(true)));

  }

  @Test
  public void getMuleDefaultLifecycleTest() {
    MuleLifecycleMapping muleLifecycleMappingMaven = new MuleLifecycleMapping();

    Map lifecycles = muleLifecycleMappingMaven.getLifecycles();
    Lifecycle defaultLifecycle = (Lifecycle) lifecycles.get(DEFAULT_LIFECYCLE);
    Map phases = defaultLifecycle.getLifecyclePhases();

    LifecycleMappingMavenVersionless lifecycleMappingMavenVersionlessMock = mock(LifecycleMappingMavenVersionless.class);
    when(lifecycleMappingMavenVersionlessMock.buildGoals(any())).thenReturn("");
    Map expectedPhases = (new MuleLifecycleMapping()).getLifecyclePhases(lifecycleMappingMavenVersionlessMock);
    for (Object phase : phases.keySet()) {
      assertThat("Current phase is not defined in the expected lifecycle map", expectedPhases.containsKey(phase), is(true));
    }

    for (Object phase : expectedPhases.keySet()) {
      assertThat("Current phase is not defined in the actual lifecycle map", phases.containsKey(phase), is(true));
    }
  }

}

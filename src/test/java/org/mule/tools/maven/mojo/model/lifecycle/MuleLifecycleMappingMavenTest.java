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

import org.apache.maven.lifecycle.DefaultLifecycles;
import org.apache.maven.lifecycle.mapping.Lifecycle;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MuleLifecycleMappingMavenTest {

  private final String DEFAULT_LIFECYCLE = DefaultLifecycles.STANDARD_LIFECYCLES[0];

  @Test
  public void getMuleDefaultLifecycleTest() {
    MuleLifecycleMappingMaven muleLifecycleMappingMaven = new MuleLifecycleMapping().getMuleLifecycleMappingMaven();

    Map lifecycles = muleLifecycleMappingMaven.getLifecycles();
    Lifecycle defaultLifecycle = (Lifecycle) lifecycles.get(DEFAULT_LIFECYCLE);
    Map phases = defaultLifecycle.getLifecyclePhases();

    Map expectedPhases = muleLifecycleMappingMaven.getLifecyclePhases();

    for (Object phase : phases.keySet()) {
      assertThat("Current phase is not defined in the expected lifecycle map", expectedPhases.containsKey(phase), is(true));
      assertThat("Current phase is not attached to the same goals in the expected lifecycle map",
                 expectedPhases.get(phase).toString(), equalTo(phases.get(phase).toString()));
    }

    for (Object phase : expectedPhases.keySet()) {
      assertThat("Current phase is not defined in the actual lifecycle map", phases.containsKey(phase), is(true));
    }
  }

}

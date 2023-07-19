/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.maven.mojo.model.lifecycle.mapping.project;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.lifecycle.mapping.LifecycleMapping;
import org.junit.Test;
import org.mule.tools.maven.mojo.model.lifecycle.MuleLifecycleMappingMavenVersionlessTest;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMaven333;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenFactory;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenVersionless;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenFactory.buildLifecycleMappingMaven;

public class MuleLifecycleMappingTest {

  @Test
  public void getPhases() {
    MuleLifecycleMapping mapping = new MuleLifecycleMapping();
    LifecycleMappingMavenVersionless lifecycleMapping = buildLifecycleMappingMaven(mapping);
    assertThat("Phases should be the same", mapping.getPhases(StringUtils.EMPTY).keySet(),
               equalTo(mapping.getLifecyclePhases(lifecycleMapping).keySet()));
  }
}

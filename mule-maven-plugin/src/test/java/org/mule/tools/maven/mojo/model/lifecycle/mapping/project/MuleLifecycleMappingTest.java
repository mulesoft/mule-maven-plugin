/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo.model.lifecycle.mapping.project;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenVersionless;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenFactory.buildLifecycleMappingMaven;

public class MuleLifecycleMappingTest {

  private static final String MULE_DEPLOY = "muleDeploy";


  private final MuleLifecycleMapping mapping = new MuleLifecycleMapping();

  @ParameterizedTest
  @ValueSource(strings = {"XXX", "false", "true"})
  public void getPhases(String isMuleDeploy) {
    if ("XXX".equals(isMuleDeploy)) {
      System.clearProperty(MULE_DEPLOY);
    } else {
      System.setProperty(MULE_DEPLOY, isMuleDeploy);
    }

    MuleLifecycleMapping mapping = new MuleLifecycleMapping();
    LifecycleMappingMavenVersionless lifecycleMapping = buildLifecycleMappingMaven(mapping);
    assertThat(mapping.getPhases(StringUtils.EMPTY).keySet())
        .as("Phases should be the same")
        .isEqualTo(mapping.getLifecyclePhases(lifecycleMapping).keySet());
  }

  @Test
  void getOptionalMojosTest() {
    assertThat(mapping.getOptionalMojos(StringUtils.EMPTY)).isNull();
  }
}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller.probing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mule.tools.client.standalone.controller.probing.deployment.ApplicationDeploymentProbe;
import org.mule.tools.client.standalone.controller.probing.deployment.DeploymentProbe;
import org.mule.tools.client.standalone.controller.probing.deployment.DomainDeploymentProbe;
import org.mule.tools.client.standalone.controller.probing.deployment.DeploymentProbeFactory;
import org.mule.tools.client.core.exception.DeploymentException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

public class DeploymentProbeFactoryTest {

  public static final String MULE_APPLICATION = "mule-application";
  public static final String MULE_DOMAIN = "mule-domain";
  Set<String> supportedPackaging = newHashSet(MULE_APPLICATION, MULE_DOMAIN);

  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {MULE_APPLICATION, ApplicationDeploymentProbe.class}, {MULE_DOMAIN, DomainDeploymentProbe.class},
        {"unsupported-packaging", DeploymentProbe.class}
    });
  }

  private String packaging;
  private Class clazz;

  @BeforeEach
  void initializeService() {
    this.packaging = new String();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void createProbeTest() {
    try {
      assertThat(DeploymentProbeFactory.createProbe(packaging)).describedAs("ProbeDeployment is not the expected")
          .isInstanceOfAny(clazz);
    } catch (DeploymentException e) {
      assertThat(!supportedPackaging.contains(packaging))
          .describedAs("The packaging " + packaging + " should have support for probing").isTrue();
    }
  }

}

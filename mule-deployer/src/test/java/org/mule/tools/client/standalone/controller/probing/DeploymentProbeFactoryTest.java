/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller.probing;

import org.junit.jupiter.api.Test;
import org.mule.tools.client.standalone.controller.probing.deployment.ApplicationDeploymentProbe;
import org.mule.tools.client.standalone.controller.probing.deployment.DeploymentProbe;
import org.mule.tools.client.standalone.controller.probing.deployment.DomainDeploymentProbe;
import org.mule.tools.client.standalone.controller.probing.deployment.DeploymentProbeFactory;
import org.mule.tools.client.core.exception.DeploymentException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeploymentProbeFactoryTest {

  private static final String MULE_APPLICATION = "mule-application";
  private static final String MULE_DOMAIN = "mule-domain";
  private static final String UNSUPPORTED_PACKAGING = "unsupported-packaging";

  @Test
  void testCreateProbeForMuleApplication0() throws DeploymentException {
    DeploymentProbe probe = DeploymentProbeFactory.createProbe(MULE_APPLICATION);
    assertThat(probe).isNotNull();
    assertThat(probe).isInstanceOf(ApplicationDeploymentProbe.class);
  }

  @Test
  void testCreateProbeForMuleApplication1() throws DeploymentException {
    DeploymentProbe probe = DeploymentProbeFactory.createProbe(MULE_DOMAIN);
    assertThat(probe).isNotNull();
    assertThat(probe).isInstanceOf(DomainDeploymentProbe.class);
  }

  @Test
  void testCreateProbeForMuleApplication2() {
    assertThatThrownBy(() -> {
      DeploymentProbe probe = DeploymentProbeFactory.createProbe(UNSUPPORTED_PACKAGING);
    }).isInstanceOf(DeploymentException.class);
  }
}

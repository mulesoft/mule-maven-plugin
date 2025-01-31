/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller.probing.deployment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mule.tools.client.standalone.controller.MuleProcessController;
import org.mule.tools.client.standalone.controller.probing.Probe;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DomainDeploymentProbeTest {

  private MuleProcessController muleMock;
  private Probe probeDeployed;
  private Probe probeNotDeployed;

  @BeforeEach
  void setUp() {
    muleMock = mock(MuleProcessController.class);

    probeDeployed = new DomainDeploymentProbe().isDeployed(muleMock, "testDomain");
    probeNotDeployed = new DomainDeploymentProbe().notDeployed(muleMock, "testDomain");
  }

  @Test
  void isDeployedTest() {
    assertThat(probeDeployed).isNotNull().isInstanceOf(DomainDeploymentProbe.class);
  }

  @Test
  void notDeployedTest() {
    assertThat(probeNotDeployed).isNotNull().isInstanceOf(DomainDeploymentProbe.class);
  }

  @Test
  void isSatisfiedTest() {
    when(muleMock.isDomainDeployed("testDomain")).thenReturn(true);
    assertThat(probeDeployed.isSatisfied()).isTrue();
  }

  @Test
  void describeFailureTest() throws Exception {
    MuleProcessController muleMock = Mockito.mock(MuleProcessController.class);
    when(muleMock.isDomainDeployed("testDomain")).thenReturn(false);

    Constructor<DomainDeploymentProbe> constructor =
        DomainDeploymentProbe.class.getDeclaredConstructor(MuleProcessController.class, String.class, Boolean.class);
    constructor.setAccessible(true);

    DomainDeploymentProbe probeNotDeployed = constructor.newInstance(muleMock, "testDomain", false);

    String failureMessage = probeNotDeployed.describeFailure();
    assertThat(failureMessage).matches("Domain \\[?testDomain\\]?.*");
  }
}

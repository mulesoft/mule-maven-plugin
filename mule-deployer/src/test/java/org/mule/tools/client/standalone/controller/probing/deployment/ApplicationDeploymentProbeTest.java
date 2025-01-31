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

class ApplicationDeploymentProbeTest {

  private MuleProcessController muleMock;
  private Probe probeDeployed;
  private Probe probeNotDeployed;

  @BeforeEach
  void setUp() {
    muleMock = mock(MuleProcessController.class);
    probeDeployed = new ApplicationDeploymentProbe().isDeployed(muleMock, "testDomain");
    probeNotDeployed = new ApplicationDeploymentProbe().notDeployed(muleMock, "testDomain");
  }

  @Test
  void isDeployedTest() {
    assertThat(new ApplicationDeploymentProbe().isDeployed(muleMock, "testDomain")).isNotNull()
        .isInstanceOf(ApplicationDeploymentProbe.class);
  }

  @Test
  void notDeployedTest() {
    assertThat(new ApplicationDeploymentProbe().notDeployed(muleMock, "testDomain")).isNotNull()
        .isInstanceOf(ApplicationDeploymentProbe.class);
  }

  //TODO this test should fail
  @Test
  void isSatisfiedTest() {
    when(muleMock.isDomainDeployed("testDomain")).thenReturn(true);
    assertThat(probeDeployed.isSatisfied()).isFalse();
  }

  @Test
  void describeFailureTest() throws Exception {
    MuleProcessController muleMock = Mockito.mock(MuleProcessController.class);
    when(muleMock.isDomainDeployed("testDomain")).thenReturn(false);

    Constructor<ApplicationDeploymentProbe> constructor =
        ApplicationDeploymentProbe.class.getDeclaredConstructor(MuleProcessController.class, String.class, Boolean.class);
    constructor.setAccessible(true);

    ApplicationDeploymentProbe probeNotDeployed = constructor.newInstance(muleMock, "testDomain", false);

    String failureMessage = probeNotDeployed.describeFailure();
    assertThat(failureMessage).matches("Application \\[?testDomain\\]?.*");
  }
}

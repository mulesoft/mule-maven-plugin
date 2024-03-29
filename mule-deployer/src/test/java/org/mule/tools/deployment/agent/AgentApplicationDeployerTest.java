/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.deployment.artifact.ApplicationDeployer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AgentApplicationDeployerTest {

  private AgentApplicationDeployer agentApplicationDeployer;
  private ApplicationDeployer applicationDeployerMock;

  @BeforeEach
  public void setUp() {
    applicationDeployerMock = mock(ApplicationDeployer.class);
    agentApplicationDeployer = new AgentApplicationDeployer(applicationDeployerMock);
  }

  @Test
  public void deployTest() throws DeploymentException {
    agentApplicationDeployer.deploy();

    verify(applicationDeployerMock, times(1)).deployApplication();
    verify(applicationDeployerMock, times(0)).undeployApplication();
  }

  @Test
  public void undeployTest() throws DeploymentException {
    agentApplicationDeployer.undeploy();

    verify(applicationDeployerMock, times(1)).undeployApplication();
    verify(applicationDeployerMock, times(0)).deployApplication();
  }
}

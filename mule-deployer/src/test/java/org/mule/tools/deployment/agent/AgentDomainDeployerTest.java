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
import org.mule.tools.deployment.artifact.DomainDeployer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AgentDomainDeployerTest {

  private AgentDomainDeployer agentDomainDeployer;
  private DomainDeployer domainDeployerMock;

  @BeforeEach
  public void setUp() {
    domainDeployerMock = mock(DomainDeployer.class);
    agentDomainDeployer = new AgentDomainDeployer(domainDeployerMock);
  }

  @Test
  public void deployTest() throws DeploymentException {
    agentDomainDeployer.deploy();

    verify(domainDeployerMock, times(1)).deployDomain();
    verify(domainDeployerMock, times(0)).undeployDomain();
  }

  @Test
  public void undeployTest() throws DeploymentException {
    agentDomainDeployer.undeploy();

    verify(domainDeployerMock, times(1)).undeployDomain();
    verify(domainDeployerMock, times(0)).deployDomain();
  }
}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.deployment.agent;

import org.junit.Before;
import org.junit.Test;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.deployment.artifact.DomainDeployer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AgentDomainDeployerTest {

  private AgentDomainDeployer agentDomainDeployer;
  private DomainDeployer domainDeployerMock;

  @Before
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

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.deployment.arm;

import org.junit.Before;
import org.junit.Test;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.deployment.artifact.DomainDeployer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ArmDomainDeployerTest {

  private ArmDomainDeployer armDomainDeployer;
  private DomainDeployer domainDeployerMock;

  @Before
  public void setUp() {
    domainDeployerMock = mock(DomainDeployer.class);
    armDomainDeployer = new ArmDomainDeployer(domainDeployerMock);
  }

  @Test
  public void deployTest() throws DeploymentException {
    armDomainDeployer.deploy();

    verify(domainDeployerMock, times(1)).deployDomain();
    verify(domainDeployerMock, times(0)).undeployDomain();
  }

  @Test
  public void undeployTest() throws DeploymentException {
    armDomainDeployer.undeploy();

    verify(domainDeployerMock, times(1)).undeployDomain();
    verify(domainDeployerMock, times(0)).deployDomain();
  }

}

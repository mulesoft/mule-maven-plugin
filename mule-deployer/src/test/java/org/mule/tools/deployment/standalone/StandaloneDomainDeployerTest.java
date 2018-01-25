/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.standalone;

import org.junit.Before;
import org.junit.Test;
import org.mule.tools.client.core.exception.DeploymentException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class StandaloneDomainDeployerTest {

  private StandaloneDomainDeployer domainDeployer;
  private StandaloneArtifactDeployer deployerMock;

  @Before
  public void setUp() throws DeploymentException {
    deployerMock = mock(StandaloneArtifactDeployer.class);
    domainDeployer = new StandaloneDomainDeployer(deployerMock);
  }

  @Test
  public void deployTest() throws DeploymentException {
    domainDeployer.deploy();

    verify(deployerMock, times(1)).verifyMuleIsStarted();
    verify(deployerMock, times(1)).deployDomain();
    verify(deployerMock, times(1)).waitForDeployments();

    verify(deployerMock, times(0)).undeployDomain();
    verify(deployerMock, times(0)).deployApplication();
    verify(deployerMock, times(0)).addDomainFromstandaloneDeployment(any());
    verify(deployerMock, times(0)).undeployApplication();
  }

  @Test
  public void undeployTest() throws DeploymentException {
    domainDeployer.undeploy();

    verify(deployerMock, times(1)).verifyMuleIsStarted();
    verify(deployerMock, times(1)).undeployDomain();

    verify(deployerMock, times(0)).waitForDeployments();
    verify(deployerMock, times(0)).deployDomain();
    verify(deployerMock, times(0)).deployApplication();
    verify(deployerMock, times(0)).addDomainFromstandaloneDeployment(any());
    verify(deployerMock, times(0)).undeployApplication();
  }
}

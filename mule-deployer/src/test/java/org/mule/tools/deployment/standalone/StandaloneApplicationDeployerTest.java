/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.deployment.standalone;

import org.junit.Before;
import org.junit.Test;
import org.mule.tools.client.core.exception.DeploymentException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class StandaloneApplicationDeployerTest {

  private StandaloneApplicationDeployer applicationDeployer;
  private StandaloneArtifactDeployer deployerMock;

  @Before
  public void setUp() throws DeploymentException {
    deployerMock = mock(StandaloneArtifactDeployer.class);
    applicationDeployer = new StandaloneApplicationDeployer(deployerMock);
  }

  @Test
  public void deployTest() throws DeploymentException {
    applicationDeployer.deploy();

    verify(deployerMock, times(1)).verifyMuleIsStarted();
    verify(deployerMock, times(1)).deployApplication();
    verify(deployerMock, times(1)).waitForDeployments();

    verify(deployerMock, times(0)).undeployDomain();
    verify(deployerMock, times(0)).deployDomain();
    verify(deployerMock, times(0)).addDomainFromstandaloneDeployment(any());
    verify(deployerMock, times(0)).undeployApplication();
  }

  @Test
  public void undeployTest() throws DeploymentException {
    applicationDeployer.undeploy();

    verify(deployerMock, times(1)).verifyMuleIsStarted();
    verify(deployerMock, times(1)).undeployApplication();

    verify(deployerMock, times(0)).waitForDeployments();
    verify(deployerMock, times(0)).deployDomain();
    verify(deployerMock, times(0)).undeployDomain();
    verify(deployerMock, times(0)).addDomainFromstandaloneDeployment(any());
    verify(deployerMock, times(0)).deployApplication();
  }
}

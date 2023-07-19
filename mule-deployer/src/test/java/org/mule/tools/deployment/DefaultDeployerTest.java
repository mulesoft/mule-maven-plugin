/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.deployment;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.utils.DeployerLog;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DefaultDeployerTest {

  private DefaultDeployer defaultDeployer;
  private Deployer deployerSpy;
  private DeployerLog logMock;

  @Before
  public void setUp() {
    deployerSpy = spy(Deployer.class);
    logMock = mock(DeployerLog.class);
    doNothing().when(logMock).info(anyString());
    defaultDeployer = new DefaultDeployer(deployerSpy, StringUtils.EMPTY, logMock);
  }

  @Test
  public void deployTest() throws DeploymentException {
    doNothing().when(deployerSpy).deploy();

    defaultDeployer.deploy();

    verify(deployerSpy, times(1)).deploy();
    verify(deployerSpy, times(0)).undeploy();
  }

  @Test
  public void undeployTest() throws DeploymentException {
    doNothing().when(deployerSpy).undeploy();

    defaultDeployer.undeploy();

    verify(deployerSpy, times(1)).undeploy();
    verify(deployerSpy, times(0)).deploy();
  }
}

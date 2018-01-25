/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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

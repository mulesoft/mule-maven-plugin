/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.arm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.utils.DeployerLog;

import javax.ws.rs.NotFoundException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class ArmApplicationDeployerTest {

  private static final Integer FAKE_APPLICATION_NUMBER = new Integer(0);
  private ArmApplicationDeployer applicationDeployer;
  private ArmArtifactDeployer artifactDeployerMock;
  private DeployerLog logMock;

  @BeforeEach
  public void setUp() {
    artifactDeployerMock = mock(ArmArtifactDeployer.class);
    logMock = mock(DeployerLog.class);
    applicationDeployer = new ArmApplicationDeployer(artifactDeployerMock, logMock);
  }

  @Test
  public void deployNonExistentApplication() throws DeploymentException {
    when(artifactDeployerMock.getApplicationId()).thenReturn(null);

    applicationDeployer.deploy();

    verify(artifactDeployerMock, times(1)).deployApplication();
    verify(artifactDeployerMock, times(0)).redeployApplication();
  }

  @Test
  public void deployExistentApplication() throws DeploymentException {
    when(artifactDeployerMock.getApplicationId()).thenReturn(FAKE_APPLICATION_NUMBER);

    applicationDeployer.deploy();

    verify(artifactDeployerMock, times(0)).deployApplication();
    verify(artifactDeployerMock, times(1)).redeployApplication();
  }

  @Test
  public void undeployExistentApplication() throws DeploymentException {
    applicationDeployer.undeploy();

    verify(artifactDeployerMock, times(1)).undeployApplication();
    verify(artifactDeployerMock, times(0)).isFailIfNotExists();
    verify(logMock, times(0)).error(anyString());
  }

  @Test
  public void undeployNonExistentDoNotFailApplication() throws DeploymentException {
    doReturn(false).when(artifactDeployerMock).isFailIfNotExists();
    doThrow(new NotFoundException()).when(artifactDeployerMock).undeployApplication();

    applicationDeployer.undeploy();

    verify(artifactDeployerMock, times(1)).undeployApplication();
    verify(artifactDeployerMock, times(1)).isFailIfNotExists();
    verify(logMock, times(1)).error(anyString());
  }

  @Test
  public void undeployNonExistentDoFailApplication() {
    assertThatThrownBy(() -> {
      doReturn(true).when(artifactDeployerMock).isFailIfNotExists();
      doThrow(new NotFoundException()).when(artifactDeployerMock).undeployApplication();

      applicationDeployer.undeploy();

      verify(artifactDeployerMock, times(1)).undeployApplication();
      verify(artifactDeployerMock, times(1)).isFailIfNotExists();
      verify(logMock, times(0)).error(anyString());
    }).isExactlyInstanceOf(NotFoundException.class);
  }
}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.cloudhub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.OperationRetrier;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;

import static org.mockito.Mockito.*;

public class CloudHubApplicationDeployerTest {

  private CloudHubApplicationDeployer applicationDeployer;
  private CloudHubArtifactDeployer artifactDeployerMock;
  private OperationRetrier retrierMock;
  private Deployment deploymentMock;

  @BeforeEach
  public void setUp() {
    artifactDeployerMock = mock(CloudHubArtifactDeployer.class);
    retrierMock = mock(OperationRetrier.class);
    deploymentMock = mock(Deployment.class);
    applicationDeployer = new CloudHubApplicationDeployer(artifactDeployerMock);
  }

  @Test
  public void deployTest() throws DeploymentException {
    applicationDeployer.deploy();

    verify(artifactDeployerMock).deployApplication();
    verify(artifactDeployerMock, never()).undeployApplication();
    verify(artifactDeployerMock, never()).deployDomain();
    verify(artifactDeployerMock, never()).undeployDomain();
  }

  @Test
  public void undeployTest() throws DeploymentException {
    applicationDeployer.undeploy();

    verify(artifactDeployerMock).undeployApplication();
    verify(artifactDeployerMock, never()).deployApplication();
    verify(artifactDeployerMock, never()).undeployDomain();
    verify(artifactDeployerMock, never()).deployDomain();
  }
}

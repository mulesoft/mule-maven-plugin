/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.cloudhub;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.core.exception.DeploymentException;

import static org.mockito.Mockito.*;

public class CloudHubDomainDeployerTest {

  private CloudHubDomainDeployer domainDeployer;
  private CloudHubArtifactDeployer artifactDeployerMock;

  @BeforeEach
  public void setUp() {
    artifactDeployerMock = mock(CloudHubArtifactDeployer.class);
    domainDeployer = new CloudHubDomainDeployer(artifactDeployerMock);
  }

  @Test
  public void deployTest() throws DeploymentException {
    domainDeployer.deploy();

    verify(artifactDeployerMock).deployDomain();
    verify(artifactDeployerMock, never()).undeployDomain();
    verify(artifactDeployerMock, never()).deployApplication();
    verify(artifactDeployerMock, never()).undeployApplication();
  }

  @Test
  public void undeployTest() throws DeploymentException {
    domainDeployer.undeploy();

    verify(artifactDeployerMock).undeployDomain();
    verify(artifactDeployerMock, never()).deployDomain();
    verify(artifactDeployerMock, never()).deployApplication();
    verify(artifactDeployerMock, never()).undeployApplication();
  }
}

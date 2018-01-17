/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.cloudhub;

import org.junit.Before;
import org.junit.Test;
import org.mule.tools.client.standalone.exception.DeploymentException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CloudHubDomainDeployerTest {

  private CloudHubDomainDeployer domainDeployer;
  private CloudHubArtifactDeployer artifactDeployerMock;

  @Before
  public void setUp() {
    artifactDeployerMock = mock(CloudHubArtifactDeployer.class);
    domainDeployer = new CloudHubDomainDeployer(artifactDeployerMock);
  }

  @Test
  public void deployTest() throws DeploymentException {
    domainDeployer.deploy();

    verify(artifactDeployerMock, times(1)).deployDomain();
    verify(artifactDeployerMock, times(0)).undeployDomain();
    verify(artifactDeployerMock, times(0)).deployApplication();
    verify(artifactDeployerMock, times(0)).undeployApplication();
  }

  @Test
  public void undeployTest() throws DeploymentException {
    domainDeployer.undeploy();

    verify(artifactDeployerMock, times(1)).undeployDomain();
    verify(artifactDeployerMock, times(0)).deployDomain();
    verify(artifactDeployerMock, times(0)).deployApplication();
    verify(artifactDeployerMock, times(0)).undeployApplication();
  }
}

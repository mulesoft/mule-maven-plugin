/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.fabric;

import org.junit.jupiter.api.Test;
import org.mule.tools.client.core.exception.DeploymentException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RuntimeFabricDomainDeployerTest {

  private final RuntimeFabricArtifactDeployer artifactDeployer = mock(RuntimeFabricArtifactDeployer.class);
  private final RuntimeFabricDomainDeployer deployer = new RuntimeFabricDomainDeployer(artifactDeployer);

  @Test
  void callTest() throws DeploymentException {
    verify(artifactDeployer, times(0)).deployDomain();
    verify(artifactDeployer, times(0)).undeployDomain();

    deployer.deploy();
    deployer.undeploy();

    verify(artifactDeployer, times(1)).deployDomain();
    verify(artifactDeployer, times(1)).undeployDomain();
  }
}

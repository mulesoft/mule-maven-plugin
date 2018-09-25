/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.fabric;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.deployment.Deployer;
import org.mule.tools.model.Deployment;
import org.mule.tools.utils.DeployerLog;

public class RuntimeFabricDomainDeployer implements Deployer {

  /**
   * The artifact deployer.
   */
  private final RuntimeFabricArtifactDeployer domainDeployer;

  public RuntimeFabricDomainDeployer(Deployment deployment, DeployerLog log) {
    this(new RuntimeFabricArtifactDeployer(deployment, log));
  }

  protected RuntimeFabricDomainDeployer(RuntimeFabricArtifactDeployer deployer) {
    domainDeployer = deployer;
  }

  /**
   * Deploys an artifact.
   *
   * @throws DeploymentException
   */
  @Override
  public void deploy() throws DeploymentException {
    domainDeployer.deployDomain();
  }

  /**
   * Undeploys an artifact.
   *
   * @throws DeploymentException
   */
  @Override
  public void undeploy() throws DeploymentException {
    domainDeployer.undeployDomain();

  }
}

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

public class RuntimeFabricApplicationDeployer implements Deployer {

  /**
   * The application deployer.
   */
  private final RuntimeFabricArtifactDeployer applicationDeployer;

  public RuntimeFabricApplicationDeployer(Deployment deployment, DeployerLog log) {
    this(new RuntimeFabricArtifactDeployer(deployment, log));
  }

  protected RuntimeFabricApplicationDeployer(RuntimeFabricArtifactDeployer deployer) {
    applicationDeployer = deployer;
  }

  /**
   * Deploys an artifact.
   *
   * @throws DeploymentException
   */
  @Override
  public void deploy() throws DeploymentException {
    applicationDeployer.deployApplication();
  }

  /**
   * Undeploys an artifact.
   *
   * @throws DeploymentException
   */
  @Override
  public void undeploy() throws DeploymentException {
    applicationDeployer.undeployApplication();
  }
}

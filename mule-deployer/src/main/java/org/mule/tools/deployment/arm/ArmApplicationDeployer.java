/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.arm;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.deployment.Deployer;
import org.mule.tools.utils.DeployerLog;

import javax.ws.rs.NotFoundException;

/**
 * Deploys mule applications to ARM.
 */
public class ArmApplicationDeployer implements Deployer {

  private final ArmArtifactDeployer armArtifactDeployer;
  private final DeployerLog log;

  public ArmApplicationDeployer(Deployment deployment, DeployerLog log) throws DeploymentException {
    this(new ArmArtifactDeployer(deployment, log), log);
  }

  protected ArmApplicationDeployer(ArmArtifactDeployer deployer, DeployerLog log) {
    this.armArtifactDeployer = deployer;
    this.log = log;
  }

  /**
   * Deploys a mule application to ARM. If the application already exists in ARM, it is redeployed.
   * 
   * @throws DeploymentException
   */
  @Override
  public void deploy() throws DeploymentException {
    Integer applicationId = armArtifactDeployer.getApplicationId();
    if (applicationId == null) {
      armArtifactDeployer.deployApplication();
    } else {
      armArtifactDeployer.redeployApplication();
    }
  }

  /**
   * Undeploys a mule application to ARM. If the application does not exist in ARM, the deployment fails just if the
   * failIfNotExists property is set to true in the deployment configuration.
   *
   * @throws DeploymentException
   */
  @Override
  public void undeploy() throws DeploymentException {
    try {
      armArtifactDeployer.undeployApplication();
    } catch (NotFoundException e) {
      if (armArtifactDeployer.isFailIfNotExists()) {
        throw e;
      } else {
        log.error("Application not found: " + armArtifactDeployer.getApplicationName());
      }
    }
  }
}

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

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.utils.DeployerLog;

import static org.mule.tools.deployment.AbstractDeployerFactory.getDeployerFactory;

public class DefaultDeployer implements Deployer {

  /**
   * It is supposed to know how to deploy a specific artifact to a specific mule environment.
   */
  private final Deployer deployer;

  /**
   * Logs information and errors. It logs the main steps of the deployment process.
   */
  private final DeployerLog log;

  /**
   * The application name defined in the deployment configuration.
   */
  private final String applicationName;

  public DefaultDeployer(Deployment deployment, DeployerLog log) throws DeploymentException {
    this(getDeployerFactory(deployment).createArtifactDeployer(deployment, log), deployment.getApplicationName(), log);
  }

  public DefaultDeployer(Deployer deployer, String applicationName, DeployerLog log) {
    this.deployer = deployer;
    this.applicationName = applicationName;
    this.log = log;
  }

  /**
   * Invokes the {@link Deployer } to deploy the artifact to a specific mule environment defined in the deployment configuration.
   * 
   * @throws DeploymentException
   */
  @Override
  public void deploy() throws DeploymentException {
    log.info(String.format("Deploying artifact %s", applicationName));
    deployer.deploy();
    log.info(String.format("Artifact %s deployed", applicationName));
  }

  /**
   * Invokes the {@link Deployer } to undeploy the artifact from a specific mule environment defined in the deployment
   * configuration.
   *
   * @throws DeploymentException
   */
  @Override
  public void undeploy() throws DeploymentException {
    log.info(String.format("Undeploying artifact %s", applicationName));
    deployer.undeploy();
    log.info(String.format("Artifact %s undeployed", applicationName));
  }
}

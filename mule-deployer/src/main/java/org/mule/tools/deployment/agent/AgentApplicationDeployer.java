/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.agent;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.deployment.artifact.ApplicationDeployer;
import org.mule.tools.model.Deployment;
import org.mule.tools.deployment.Deployer;
import org.mule.tools.utils.DeployerLog;

/**
 * Deploys mule applications to the Agent.
 */
public class AgentApplicationDeployer implements Deployer {

  /**
   * The application deployer.
   */
  private final ApplicationDeployer applicationDeployer;

  /**
   * Constructor for the {@link AgentApplicationDeployer}.
   * 
   * @param deployment A {@link Deployment} with the deployment information.
   * @param log A {@link DeployerLog} to inform about the deployment process.
   */
  public AgentApplicationDeployer(Deployment deployment, DeployerLog log) {
    this(new AgentArtifactDeployer(deployment, log));
  }

  protected AgentApplicationDeployer(ApplicationDeployer deployer) {
    applicationDeployer = deployer;
  }

  /**
   * Deploys a mule application to the Agent.
   * 
   * @throws DeploymentException
   */
  @Override
  public void deploy() throws DeploymentException {
    applicationDeployer.deployApplication();
  }

  /**
   * Undeploys a mule application from the Agent.
   * 
   * @throws DeploymentException
   */
  @Override
  public void undeploy() throws DeploymentException {
    applicationDeployer.undeployApplication();
  }

}

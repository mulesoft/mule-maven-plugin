/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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

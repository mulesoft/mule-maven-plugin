/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.deployment.agent;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.deployment.Deployer;
import org.mule.tools.model.Deployment;
import org.mule.tools.deployment.artifact.DomainDeployer;
import org.mule.tools.utils.DeployerLog;

/**
 * Deploys mule applications to the Agent.
 */
public class AgentDomainDeployer implements Deployer {

  /**
   * The domain deployer.
   */
  private final DomainDeployer domainDeployer;

  /**
   * Constructor for the {@link AgentDomainDeployer}.
   *
   * @param deployment A {@link Deployment} with the deployment information.
   * @param log A {@link DeployerLog} to inform about the deployment process.
   */
  public AgentDomainDeployer(Deployment deployment, DeployerLog log) {
    this(new AgentArtifactDeployer(deployment, log));
  }

  protected AgentDomainDeployer(DomainDeployer deployer) {
    domainDeployer = deployer;
  }

  /**
   * Deploys a mule domain to the Agent.
   *
   * @throws DeploymentException
   */
  @Override
  public void deploy() throws DeploymentException {
    domainDeployer.deployDomain();
  }

  /**
   * Undeploys a mule domain from the Agent.
   *
   * @throws DeploymentException
   */
  @Override
  public void undeploy() throws DeploymentException {
    domainDeployer.undeployDomain();
  }
}

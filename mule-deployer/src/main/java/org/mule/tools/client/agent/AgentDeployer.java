/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.agent;

import org.apache.maven.project.MavenProject;

import org.mule.tools.client.AbstractDeployer;
import org.mule.tools.client.exception.ClientException;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.utils.DeployerLog;

public class AgentDeployer extends AbstractDeployer {

  private AgentClient agentClient;
  private AgentDeployment agentDeployment;

  public AgentDeployer(AgentDeployment agentDeployment, DeployerLog log) throws DeploymentException {
    super(agentDeployment, log);
  }

  @Override
  public void deploy() throws DeploymentException {
    try {
      info("Deploying application " + getApplicationName() + " to Mule Agent");
      agentClient.deployApplication(getApplicationName(), getApplicationFile());
    } catch (ClientException e) {
      error("Failure: " + e.getMessage());
      throw new DeploymentException("Failed to deploy application " + getApplicationName(), e);
    }
  }

  @Override
  public void undeploy(MavenProject mavenProject) throws DeploymentException {
    AgentClient agentClient = new AgentClient(log, agentDeployment.getUri());
    log.info("Undeploying application " + agentDeployment.getApplicationName());
    agentClient.undeployApplication(agentDeployment.getApplicationName());
  }

  @Override
  protected void initialize() {
    agentDeployment = (AgentDeployment) deploymentConfiguration;
    this.agentClient = new AgentClient(log, agentDeployment.getUri());
  }

}

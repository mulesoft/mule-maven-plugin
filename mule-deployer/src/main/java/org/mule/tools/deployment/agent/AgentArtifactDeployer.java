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

import org.mule.tools.client.agent.AgentClient;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.deployment.artifact.ArtifactDeployer;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.verification.agent.AgentDeploymentVerification;

/**
 * Deploys mule artifacts to the Agent using the {@link AgentClient}.
 */
public class AgentArtifactDeployer implements ArtifactDeployer {

  private static final Long DEFAULT_AGENT_TIMEOUT = 60000L;

  private final AgentDeployment deployment;

  /**
   * The agent client. It should know how to call the agent API.
   */
  private final AgentClient client;

  public AgentArtifactDeployer(Deployment deployment, DeployerLog log) {
    this(deployment, new AgentClient(log, deployment));
  }

  protected AgentArtifactDeployer(Deployment deployment, AgentClient client) {
    this.deployment = (AgentDeployment) deployment;
    if (!this.deployment.getDeploymentTimeout().isPresent()) {
      this.deployment.setDeploymentTimeout(DEFAULT_AGENT_TIMEOUT);
    }
    this.client = client;
  }

  /**
   * Deploys the domain specified in the {@link Deployment} to the agent through the {@link AgentClient}.
   */
  @Override
  public void deployDomain() {
    client.deployDomain(deployment.getApplicationName(), deployment.getArtifact());
  }

  /**
   * Undeploys the domain specified in the {@link Deployment} from the agent through the {@link AgentClient}.
   */
  @Override
  public void undeployDomain() {
    client.undeployDomain(deployment.getApplicationName());
  }

  /**
   * Deploys the application specified in the {@link Deployment} to the agent through the {@link AgentClient}.
   *
   * @throws DeploymentException
   */
  @Override
  public void deployApplication() throws DeploymentException {
    client.deployApplication(deployment.getApplicationName(), deployment.getArtifact());
    AgentDeploymentVerification verification = getDeploymentVerification();
    verification.assertDeployment(deployment);
  }

  /**
   * Undeploys the application specified in the {@link Deployment} from the agent through the {@link AgentClient}.
   */
  @Override
  public void undeployApplication() {
    client.undeployApplication(deployment.getApplicationName());
  }


  public AgentDeploymentVerification getDeploymentVerification() {
    return new AgentDeploymentVerification(client);
  }
}

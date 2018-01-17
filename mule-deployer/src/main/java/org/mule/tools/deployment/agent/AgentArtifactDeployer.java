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

import org.apache.commons.lang3.StringUtils;
import org.mule.tools.client.OperationRetrier;
import org.mule.tools.client.agent.AgentClient;
import org.mule.tools.client.agent.model.Application;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.deployment.artifact.ArtifactDeployer;
import org.mule.tools.utils.DeployerLog;

import java.util.concurrent.TimeoutException;

/**
 * Deploys mule artifacts to the Agent using the {@link AgentClient}.
 */
public class AgentArtifactDeployer implements ArtifactDeployer {

  private final AgentDeployment deployment;
  /**
   * An operation retrier that verifies the deployment success during a time span.
   */
  private OperationRetrier retrier;

  /**
   * The agent client. It should know how to call the agent API.
   */
  private final AgentClient client;

  private static final String STARTED_STATUS = "STARTED";

  public AgentArtifactDeployer(Deployment deployment, DeployerLog log) {
    this(deployment, new AgentClient(log, deployment), new OperationRetrier());
  }

  protected AgentArtifactDeployer(Deployment deployment, AgentClient client, OperationRetrier retrier) {
    this.deployment = (AgentDeployment) deployment;
    this.client = client;
    this.retrier = retrier;
    this.retrier.setTimeout(deployment.getDeploymentTimeout());

  }

  /**
   * Deploys the domain specified in the {@link Deployment} to the agent through the {@link AgentClient}.
   * 
   * @throws DeploymentException
   */
  @Override
  public void deployDomain() throws DeploymentException {
    client.deployDomain(deployment.getApplicationName(), deployment.getArtifact());
  }

  /**
   * Undeploys the domain specified in the {@link Deployment} from the agent through the {@link AgentClient}.
   * 
   * @throws DeploymentException
   */
  @Override
  public void undeployDomain() throws DeploymentException {
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
    try {
      retrier.retry(() -> !isApplicationDeployed());
    } catch (InterruptedException | TimeoutException e) {
      throw new DeploymentException("Application deployment has timeouted", e);
    }
  }

  /**
   * Undeploys the application specified in the {@link Deployment} from the agent through the {@link AgentClient}.
   *
   * @throws DeploymentException
   */
  @Override
  public void undeployApplication() throws DeploymentException {
    client.undeployApplication(deployment.getApplicationName());
  }

  public boolean isApplicationDeployed() {
    Application application = client.getApplication(deployment.getApplicationName());
    return application != null && StringUtils.equals(application.state, STARTED_STATUS);
  }
}

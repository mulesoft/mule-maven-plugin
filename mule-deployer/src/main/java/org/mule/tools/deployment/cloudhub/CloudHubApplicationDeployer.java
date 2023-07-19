/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.deployment.cloudhub;

import org.mule.tools.client.core.exception.ClientException;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.deployment.Deployer;
import org.mule.tools.utils.DeployerLog;

/**
 * Deploys mule applications to CloudHub.
 */
public class CloudHubApplicationDeployer implements Deployer {

  /**
   * The application deployer.
   */
  private final CloudHubArtifactDeployer applicationDeployer;

  public CloudHubApplicationDeployer(Deployment deployment, DeployerLog log) {
    this(new CloudHubArtifactDeployer(deployment, log));
  }

  protected CloudHubApplicationDeployer(CloudHubArtifactDeployer deployer) {
    applicationDeployer = deployer;
  }

  /**
   * Deploys a mule application to CloudHub.
   * 
   * @throws DeploymentException
   */
  @Override
  public void deploy() throws DeploymentException {
    try {
      applicationDeployer.deployApplication();
    } catch (ClientException e) {
      throw new DeploymentException("Failed to deploy application " + applicationDeployer.getApplicationName(), e);
    }
  }

  /**
   * Undeploys a mule application from CloudHub.
   * 
   * @throws DeploymentException
   */
  @Override
  public void undeploy() throws DeploymentException {
    applicationDeployer.undeployApplication();
  }
}

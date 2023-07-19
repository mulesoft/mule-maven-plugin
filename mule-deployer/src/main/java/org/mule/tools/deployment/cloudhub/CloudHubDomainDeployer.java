/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.deployment.cloudhub;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.deployment.Deployer;
import org.mule.tools.model.Deployment;
import org.mule.tools.utils.DeployerLog;

/**
 * Deploys mule domains to CloudHub.
 */
public class CloudHubDomainDeployer implements Deployer {

  /**
   * The application deployer.
   */
  private final CloudHubArtifactDeployer domainDeployer;

  public CloudHubDomainDeployer(Deployment deployment, DeployerLog log) {
    this(new CloudHubArtifactDeployer(deployment, log));
  }

  protected CloudHubDomainDeployer(CloudHubArtifactDeployer deployer) {
    domainDeployer = deployer;
  }

  @Override
  public void deploy() throws DeploymentException {
    domainDeployer.deployDomain();
  }

  @Override
  public void undeploy() throws DeploymentException {
    domainDeployer.undeployDomain();
  }
}

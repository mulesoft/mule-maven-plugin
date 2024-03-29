/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.standalone;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.deployment.Deployer;
import org.mule.tools.utils.DeployerLog;

/**
 * Deploys mule domains to Standalone.
 */
public class StandaloneDomainDeployer implements Deployer {

  private final StandaloneArtifactDeployer standaloneArtifactDeployer;

  public StandaloneDomainDeployer(Deployment deployment, DeployerLog log) throws DeploymentException {
    this(new StandaloneArtifactDeployer(deployment, log));
  }

  protected StandaloneDomainDeployer(StandaloneArtifactDeployer deployer) throws DeploymentException {
    this.standaloneArtifactDeployer = deployer;
  }

  /**
   * Deploys a mule domain to Standalone. First, it verifies if the mule instance is running in the location specified in the
   * deployment configuration. If it is running, the domain is deployed, and the deployer waits for the deployment to be
   * sucessful.
   *
   * @throws DeploymentException
   */
  @Override
  public void deploy() throws DeploymentException {
    standaloneArtifactDeployer.verifyMuleIsStarted();
    standaloneArtifactDeployer.deployDomain();
    standaloneArtifactDeployer.waitForDeployments();
  }

  @Override
  public void undeploy() throws DeploymentException {
    standaloneArtifactDeployer.verifyMuleIsStarted();
    standaloneArtifactDeployer.undeployDomain();
  }
}

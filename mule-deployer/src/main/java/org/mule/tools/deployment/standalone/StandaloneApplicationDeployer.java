/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.deployment.standalone;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.deployment.Deployer;
import org.mule.tools.utils.DeployerLog;

/**
 * Deploys mule applications to Standalone.
 */
public class StandaloneApplicationDeployer implements Deployer {

  private final StandaloneArtifactDeployer standaloneArtifactDeployer;

  public StandaloneApplicationDeployer(Deployment deployment, DeployerLog log) throws DeploymentException {
    this(new StandaloneArtifactDeployer(deployment, log));
  }

  protected StandaloneApplicationDeployer(StandaloneArtifactDeployer deployer) throws DeploymentException {
    this.standaloneArtifactDeployer = deployer;
  }

  /**
   * Deploys a mule application to Standalone. First, it verifies if the mule instance is running in the location specified in the
   * deployment configuration. If it is running, the application is deployed, and the deployer waits for the deployment to be
   * sucessful.
   * 
   * @throws DeploymentException
   */
  @Override
  public void deploy() throws DeploymentException {
    standaloneArtifactDeployer.verifyMuleIsStarted();
    standaloneArtifactDeployer.deployApplication();
    standaloneArtifactDeployer.waitForDeployments();
  }

  /**
   * Undeploys a mule application from Standalone.
   * 
   * @throws DeploymentException
   */
  @Override
  public void undeploy() throws DeploymentException {
    standaloneArtifactDeployer.verifyMuleIsStarted();
    standaloneArtifactDeployer.undeployApplication();
  }
}

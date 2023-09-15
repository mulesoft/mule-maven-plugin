/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.cloudhub2;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.deployment.Deployer;
import org.mule.tools.model.Deployment;
import org.mule.tools.utils.DeployerLog;

public class Cloudhub2ApplicationDeployer implements Deployer {

  /**
   * The application deployer.
   */
  private final Cloudhub2ArtifactDeployer applicationDeployer;

  public Cloudhub2ApplicationDeployer(Deployment deployment, DeployerLog log) {
    this(new Cloudhub2ArtifactDeployer(deployment, log));
  }

  protected Cloudhub2ApplicationDeployer(Cloudhub2ArtifactDeployer deployer) {
    applicationDeployer = deployer;
  }

  /**
   * Deploys an artifact.
   *
   * @throws DeploymentException
   */
  @Override
  public void deploy() throws DeploymentException {
    applicationDeployer.deployApplication();
  }

  /**
   * Undeploys an artifact.
   *
   * @throws DeploymentException
   */
  @Override
  public void undeploy() throws DeploymentException {
    applicationDeployer.undeployApplication();
  }
}

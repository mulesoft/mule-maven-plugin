/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.deployment.cloudhub2;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.deployment.Deployer;
import org.mule.tools.model.Deployment;
import org.mule.tools.utils.DeployerLog;

public class Cloudhub2DomainDeployer implements Deployer {

  /**
   * The artifact deployer.
   */
  private final Cloudhub2ArtifactDeployer domainDeployer;

  public Cloudhub2DomainDeployer(Deployment deployment, DeployerLog log) {
    this(new Cloudhub2ArtifactDeployer(deployment, log));
  }

  protected Cloudhub2DomainDeployer(Cloudhub2ArtifactDeployer deployer) {
    domainDeployer = deployer;
  }

  /**
   * Deploys an artifact.
   *
   * @throws DeploymentException
   */
  @Override
  public void deploy() throws DeploymentException {
    domainDeployer.deployDomain();
  }

  /**
   * Undeploys an artifact.
   *
   * @throws DeploymentException
   */
  @Override
  public void undeploy() throws DeploymentException {
    domainDeployer.undeployDomain();

  }
}

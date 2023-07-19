/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.deployment.arm;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.deployment.artifact.DomainDeployer;
import org.mule.tools.model.Deployment;
import org.mule.tools.deployment.Deployer;
import org.mule.tools.utils.DeployerLog;


/**
 * Deploys mule domains to ARM.
 */
public class ArmDomainDeployer implements Deployer {

  private final DomainDeployer armArtifactDeployer;

  public ArmDomainDeployer(Deployment deployment, DeployerLog log) {
    this(new ArmArtifactDeployer(deployment, log));
  }

  public ArmDomainDeployer(DomainDeployer deployer) {
    armArtifactDeployer = deployer;
  }

  @Override
  public void deploy() throws DeploymentException {
    armArtifactDeployer.deployDomain();
  }

  @Override
  public void undeploy() throws DeploymentException {
    armArtifactDeployer.undeployDomain();
  }
}

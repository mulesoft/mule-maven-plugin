/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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

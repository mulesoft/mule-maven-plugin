/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.cloudhub2;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.deployment.Deployer;
import org.mule.tools.deployment.fabric.RequestBuilder;
import org.mule.tools.deployment.fabric.RuntimeFabricArtifactDeployer;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.Cloudhub2Deployment;
import org.mule.tools.utils.DeployerLog;

public class Cloudhub2ApplicationDeployer extends RuntimeFabricArtifactDeployer implements Deployer {


  public Cloudhub2ApplicationDeployer(Deployment deployment, DeployerLog log) {
    super(deployment, log);
  }

  @Override
  public RequestBuilder createRequestBuilder() {
    return new RequestBuilderCh2((Cloudhub2Deployment) this.deployment, this.client);
  }

  /**
   * Deploys an artifact.
   *
   * @throws DeploymentException
   */
  @Override
  public void deploy() throws DeploymentException {
    super.deployApplication();
  }

  /**
   * Undeploys an artifact.
   *
   * @throws DeploymentException
   */
  @Override
  public void undeploy() throws DeploymentException {
    super.undeployApplication();
  }
}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.cloudhub2;

import org.apache.commons.lang3.StringUtils;
import org.mule.tools.client.core.exception.ClientException;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.deployment.fabric.RequestBuilder;
import org.mule.tools.deployment.fabric.RuntimeFabricArtifactDeployer;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.Cloudhub2Deployment;
import org.mule.tools.model.anypoint.RuntimeFabricDeployment;
import org.mule.tools.utils.DeployerLog;

public class Cloudhub2ArtifactDeployer extends RuntimeFabricArtifactDeployer {

  private static final String CH2_DEPLOY_ERROR_MESSAGE = "The name of the application '${name}' is already used in the target";

  public Cloudhub2ArtifactDeployer(Deployment deployment, DeployerLog log) {
    super(deployment, new Cloudhub2RuntimeFabricClient((RuntimeFabricDeployment) deployment, log), log);
  }

  @Override
  public RequestBuilder createRequestBuilder() {
    return new RequestBuilderCh2((Cloudhub2Deployment) this.deployment, this.client);
  }

  @Override
  protected boolean isAlreadyDeployed(ClientException e) {
    return e.getStatusCode() == BAD_REQUEST && StringUtils
        .containsIgnoreCase(e.getMessage(), CH2_DEPLOY_ERROR_MESSAGE.replace("${name}", deployment.getApplicationName()));
  }


  @Override
  public void deployDomain() throws DeploymentException {
    throw new DeploymentException("Deployment of domains to CloudHub 2.0 is not supported");
  }

  @Override
  public void undeployDomain() throws DeploymentException {
    throw new DeploymentException("Undeployment of domains from CloudHub 2.0 is not supported");
  }

}

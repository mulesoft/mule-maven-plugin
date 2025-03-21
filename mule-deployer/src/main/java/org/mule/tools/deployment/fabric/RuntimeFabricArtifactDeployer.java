/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.fabric;

import org.mule.tools.client.core.exception.ClientException;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.fabric.RuntimeFabricClient;
import org.mule.tools.client.fabric.model.DeploymentModify;
import org.mule.tools.client.fabric.model.Target;
import org.mule.tools.deployment.artifact.ArtifactDeployer;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.RuntimeFabricDeployment;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.verification.DeploymentVerification;
import org.mule.tools.verification.fabric.RuntimeFabricDeploymentVerification;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

public class RuntimeFabricArtifactDeployer implements ArtifactDeployer {

  private static final Long DEFAULT_RUNTIME_FABRIC_DEPLOYMENT_TIMEOUT = 1200000L;
  private DeploymentVerification deploymentVerification;
  private RequestBuilder requestBuilder;
  protected RuntimeFabricClient client;
  private final DeployerLog log;
  protected final RuntimeFabricDeployment deployment;

  public RuntimeFabricArtifactDeployer(Deployment deployment, DeployerLog log) {
    this(deployment, new RuntimeFabricClient((RuntimeFabricDeployment) deployment, log), log);
  }

  protected RuntimeFabricArtifactDeployer(Deployment deployment, RuntimeFabricClient client, DeployerLog log) {
    checkArgument(client != null, "The client must not be null.");

    this.log = log;
    this.client = client;
    this.deployment = (RuntimeFabricDeployment) deployment;
    this.deploymentVerification = new RuntimeFabricDeploymentVerification(client);
    this.requestBuilder = createRequestBuilder();
    if (!this.deployment.getDeploymentTimeout().isPresent()) {
      this.deployment.setDeploymentTimeout(DEFAULT_RUNTIME_FABRIC_DEPLOYMENT_TIMEOUT);
    }
  }

  public RequestBuilder createRequestBuilder() {
    return new RequestBuilder(this.deployment, this.client);
  }

  @Override
  public void deployApplication() throws DeploymentException {
    try {
      log.info("Starting deployment to " + deployment.getTarget());
      log.info("Checking app " + deployment.getApplicationName());
      DeploymentModify modify = requestBuilder.buildDeploymentModify();
      Optional<String> deploymentId = requestBuilder.getOptionalDeploymentId(modify.target);
      if (deploymentId.isPresent()) {
        client.redeploy(modify, deploymentId.get());
      } else {
        client.deploy(requestBuilder.buildDeploymentRequest());
      }
    } catch (ClientException e) {
      throw new DeploymentException("Could not deploy application.", e);
    }
    if (!deployment.getSkipDeploymentVerification()) {
      checkApplicationHasStarted();
    }
  }

  @Override
  public void undeployApplication() throws DeploymentException {
    try {
      Target target = requestBuilder.buildTarget();
      String deploymentId = requestBuilder.getDeploymentId(target);
      client.deleteDeployment(deploymentId);
    } catch (ClientException | IllegalStateException e) {
      throw new DeploymentException("Could not undeploy application.", e);
    }
  }

  @Override
  public void deployDomain() throws DeploymentException {
    throw new DeploymentException("Deployment of domains to Runtime Fabric is not supported");

  }

  @Override
  public void undeployDomain() throws DeploymentException {
    throw new DeploymentException("Undeployment of domains to Runtime Fabric is not supported");
  }


  public void setDeploymentVerification(DeploymentVerification deploymentVerification) {
    checkArgument(deploymentVerification != null, "The verificator must not be null.");
    this.deploymentVerification = deploymentVerification;
  }

  public void setRequestBuilder(RequestBuilder requestBuilder) {
    this.requestBuilder = requestBuilder;
  }

  /**
   * Checks if an application in CloudHub has the {@code STARTED_STATUS} status.
   *
   * @throws DeploymentException In case it timeouts while checking for the status
   */
  protected void checkApplicationHasStarted() throws DeploymentException {
    log.info("Checking if application: " + deployment.getApplicationName() + " has started");
    deploymentVerification.assertDeployment(deployment);
  }
}

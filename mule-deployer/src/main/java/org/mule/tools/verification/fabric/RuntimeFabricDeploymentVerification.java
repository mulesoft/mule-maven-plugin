/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.verification.fabric;

import org.apache.commons.lang3.StringUtils;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.fabric.RuntimeFabricClient;
import org.mule.tools.client.fabric.model.DeploymentDetailedResponse;
import org.mule.tools.model.Deployment;
import org.mule.tools.verification.DefaultDeploymentVerification;
import org.mule.tools.verification.DeploymentVerification;
import org.mule.tools.verification.DeploymentVerificationStrategy;


public class RuntimeFabricDeploymentVerification implements DeploymentVerification {

  private final RuntimeFabricClient client;
  private final String deploymentId;

  private static final String FAILED_STATUS = "FAILED";
  private static final String APPLIED_STATUS = "APPLIED";
  private static final String STARTED_STATUS = "STARTED";

  public RuntimeFabricDeploymentVerification(RuntimeFabricClient client, String deploymentId) {
    this.client = client;
    this.deploymentId = deploymentId;
  }

  @Override
  public void assertDeployment(Deployment deployment) throws DeploymentException {
    DefaultDeploymentVerification verification =
        new DefaultDeploymentVerification(new RuntimeFabricDeploymentVerificationStrategy(deployment, deploymentId));
    verification.assertDeployment(deployment);
  }

  private class RuntimeFabricDeploymentVerificationStrategy implements DeploymentVerificationStrategy {

    private final Deployment deployment;
    private String deploymentId;

    private RuntimeFabricDeploymentVerificationStrategy(Deployment deployment, String deploymentId) {
      this.deployment = deployment;
      this.deploymentId = deploymentId;
    }

    @Override
    public Boolean isDeployed(Deployment deployment) {
      if (deploymentId == null) {
        return false;
      }
      DeploymentDetailedResponse response = client.getDeployment(deploymentId);
      if (response != null) {
        if (StringUtils.equals(response.status, FAILED_STATUS)) {
          throw new IllegalStateException("Deployment failed");
        } else if (StringUtils.equals(response.status, APPLIED_STATUS)
            || StringUtils.equals(response.status, STARTED_STATUS)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public void onTimeout(Deployment deployment) {
      return;
    }

    @Override
    public Boolean run() {
      return !isDeployed(deployment);
    }

    @Override
    public String getRetryExhaustedMessage() {
      return "Runtime Fabric deployment has timed out";
    }
  }
}

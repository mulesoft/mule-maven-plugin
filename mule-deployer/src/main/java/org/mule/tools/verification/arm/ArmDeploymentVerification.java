/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.verification.arm;

import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.verification.DefaultDeploymentVerification;
import org.mule.tools.verification.DeploymentVerification;
import org.mule.tools.verification.DeploymentVerificationStrategy;

public class ArmDeploymentVerification implements DeploymentVerification {

  private final ArmClient client;
  private final Integer applicationId;

  public ArmDeploymentVerification(ArmClient client, Integer applicationId) {
    this.client = client;
    this.applicationId = applicationId;
  }

  @Override
  public void assertDeployment(Deployment deployment) throws DeploymentException {
    DeploymentVerification verification = new DefaultDeploymentVerification(new ArmDeploymentVerificationStrategy(deployment));
    verification.assertDeployment(deployment);
  }

  private class ArmDeploymentVerificationStrategy implements DeploymentVerificationStrategy {

    private final Deployment deployment;

    public ArmDeploymentVerificationStrategy(Deployment deployment) {
      this.deployment = deployment;
    }

    @Override
    public Boolean isDeployed(Deployment deployment) {
      return client.isStarted(applicationId);
    }

    @Override
    public void onTimeout(Deployment deployment) {
      client.undeployApplication(applicationId);
    }

    @Override
    public Boolean run() {
      return !isDeployed(deployment);
    }

    @Override
    public String getRetryExhaustedMessage() {
      return "ARM deployment has timed out";
    }
  }
}

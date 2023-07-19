/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.verification.arm;

import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.verification.DefaultDeploymentVerification;
import org.mule.tools.verification.DeploymentVerification;
import org.mule.tools.verification.DeploymentVerificationStrategy;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ArmDeploymentVerification implements DeploymentVerification {

  private final ArmClient client;
  private final Integer applicationId;
  private DeploymentVerification verification;

  public ArmDeploymentVerification(ArmClient client, Integer applicationId) {
    this.client = client;
    this.applicationId = applicationId;
    this.verification = new DefaultDeploymentVerification(new ArmDeploymentVerificationStrategy());
  }

  @Override
  public void assertDeployment(Deployment deployment) throws DeploymentException {
    verification.assertDeployment(deployment);
  }

  private class ArmDeploymentVerificationStrategy implements DeploymentVerificationStrategy {

    @Override
    public Predicate<Deployment> isDeployed() {
      return deployment -> client.isStarted(applicationId);
    }

    @Override
    public Consumer<Deployment> onTimeout() {
      return deployment -> client.getApplication(applicationId);
    }
  }
}

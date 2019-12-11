/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.verification;

import org.mule.tools.client.OperationRetrier;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;

import java.util.concurrent.TimeoutException;

public class DefaultDeploymentVerification implements DeploymentVerification {

  /**
   * An operation retrier that verifies the deployment success during a time span.
   */
  private final OperationRetrier retrier;

  private final DeploymentVerificationStrategy verificationStrategy;

  public DefaultDeploymentVerification(DeploymentVerificationStrategy handler) {
    this(new OperationRetrier(), handler);
  }

  protected DefaultDeploymentVerification(OperationRetrier retrier, DeploymentVerificationStrategy verificationStrategy) {
    this.retrier = retrier;
    this.verificationStrategy = verificationStrategy;
  }

  @Override
  public void assertDeployment(Deployment deployment) throws DeploymentException {
    Long deploymentTimeout = deployment.getDeploymentTimeout();
    if (deploymentTimeout != null) {
      retrier.setTimeout(deploymentTimeout);
    }
    try {
      retrier.retry(verificationStrategy);
    } catch (InterruptedException | TimeoutException e) {
      verificationStrategy.onTimeout(deployment);
      throw new DeploymentException("Validation timed out waiting for application to start. " +
          "Please consider increasing the deploymentTimeout property.", e);
    }
  }
}

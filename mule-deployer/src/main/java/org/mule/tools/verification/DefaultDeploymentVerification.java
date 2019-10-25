/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.verification;

import org.mule.tools.client.OperationRetrier;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;

import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DefaultDeploymentVerification implements DeploymentVerification {

  /**
   * An operation retrier that verifies the deployment success during a time span.
   */
  private final OperationRetrier retrier;

  private final Consumer<Deployment> onTimeout;

  private final Predicate<Deployment> isDeployed;

  public DefaultDeploymentVerification(DeploymentVerificationStrategy handler) {
    this(new OperationRetrier(), handler);
  }

  protected DefaultDeploymentVerification(OperationRetrier retrier, DeploymentVerificationStrategy verificationStrategy) {
    this.retrier = retrier;
    this.onTimeout = verificationStrategy.onTimeout();
    this.isDeployed = verificationStrategy.isDeployed();
  }

  @Override
  public void assertDeployment(Deployment deployment) throws DeploymentException {
    deployment.getDeploymentTimeout().ifPresent(retrier::setTimeout);
    try {
      retrier.retry(() -> !isDeployed.test(deployment));
    } catch (InterruptedException | TimeoutException e) {
      onTimeout.accept(deployment);
      throw new DeploymentException("Validation timed out waiting for application to start. " +
          "Please consider increasing the deploymentTimeout property.", e);
    } catch (IllegalStateException e) {
      onTimeout.accept(deployment);
      throw new DeploymentException("Deployment has failed", e);
    }
  }
}

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;

import java.util.function.Supplier;

/**
 * Validates if the mule runtime version is valid in a deployment scenario.
 */
public abstract class AbstractDeploymentValidator {

  /**
   * The deployment to be validated.
   */
  protected final Deployment deployment;

  /**
   * The supplier of the exception to be thrown in case the deployment is not supplied.
   */
  private final Supplier<DeploymentException> deploymentExceptionSupplier =
      () -> new DeploymentException("muleVersion is not present in deployment configuration");

  public AbstractDeploymentValidator(Deployment deployment) {
    this.deployment = deployment;
  }

  /**
   * Validates that the mule runtime version declared in the deployment configuration is supported by the deployment environment.
   * 
   * @throws DeploymentException if the mule runtime version cannot be resolved; if the mule runtime version is not declared; or
   *         if the declared mule runtime version is not supported by the environment.
   */
  public void validateMuleVersionAgainstEnvironment() throws DeploymentException {
    String deploymentMuleVersion = deployment.getMuleVersion().orElseThrow(deploymentExceptionSupplier);
    EnvironmentSupportedVersions environmentVersion = getEnvironmentSupportedVersions();
    environmentVersion.supports(deploymentMuleVersion);
  }

  /**
   * Retrieves the supported mule runtime version in a specific environment.
   * 
   * @return The mule runtime supported versions.
   * @throws DeploymentException if the mule runtime version cannot be resolved.
   */
  public abstract EnvironmentSupportedVersions getEnvironmentSupportedVersions() throws DeploymentException;

}

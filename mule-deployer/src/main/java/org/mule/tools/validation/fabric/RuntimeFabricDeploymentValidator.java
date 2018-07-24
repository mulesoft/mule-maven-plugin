/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation.fabric;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.validation.AbstractDeploymentValidator;
import org.mule.tools.validation.EnvironmentSupportedVersions;

import static com.google.common.collect.Lists.newArrayList;

public class RuntimeFabricDeploymentValidator extends AbstractDeploymentValidator {

  public RuntimeFabricDeploymentValidator(Deployment deployment) {
    super(deployment);
  }

  /**
   * Retrieves the supported mule runtime version in a specific environment.
   *
   * @return The mule runtime supported versions.
   * @throws DeploymentException if the mule runtime version cannot be resolved.
   */
  @Override
  public EnvironmentSupportedVersions getEnvironmentSupportedVersions() throws DeploymentException {
    // This is not supported by Runtime Fabric right now, so we just add the version that is set in the deployment configuration
    return new EnvironmentSupportedVersions(newArrayList(deployment.getMuleVersion().get()));
  }
}

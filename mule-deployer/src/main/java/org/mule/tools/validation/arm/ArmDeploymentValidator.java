/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation.arm;

import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.client.arm.model.Target;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.validation.AbstractDeploymentValidator;
import org.mule.tools.validation.EnvironmentSupportedVersions;

/**
 * Validates if the mule runtime version is valid in an ARM deployment scenario.
 */
public class ArmDeploymentValidator extends AbstractDeploymentValidator {

  public ArmDeploymentValidator(Deployment deployment) {
    super(deployment);
  }

  @Override
  public EnvironmentSupportedVersions getEnvironmentSupportedVersions() throws DeploymentException {
    ArmClient client = getArmClient();
    String muleRuntimeVersion = findRuntimeVersion(client);
    return new EnvironmentSupportedVersions(muleRuntimeVersion);
  }

  /**
   * Find the mule runtime version in the target server configured in the deployment configuration.
   * 
   * @param client The ARM client.
   * @return The mule runtime version running in the target.
   */
  private String findRuntimeVersion(ArmClient client) {
    Target target = client.findServerByName(((ArmDeployment) deployment).getTarget());
    Integer serverId = Integer.valueOf(target.id);
    return client.getServer(serverId).data[0].muleVersion;
  }

  /**
   * Creates an ARM client based on the deployment configuration.
   *
   * @return The generated ARM client.
   */
  private ArmClient getArmClient() {
    ArmClient client = new ArmClient(deployment, null);
    return client;
  }
}

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation.cloudhub;

import org.mule.tools.client.cloudhub.CloudhubClient;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.validation.AbstractDeploymentValidator;
import org.mule.tools.validation.EnvironmentSupportedVersions;

import java.util.Set;

/**
 * Validates if the mule runtime version is valid in a CloudHub deployment scenario.
 */
public class CloudHubDeploymentValidator extends AbstractDeploymentValidator {

  public CloudHubDeploymentValidator(Deployment deployment) {
    super(deployment);
  }

  @Override
  public EnvironmentSupportedVersions getEnvironmentSupportedVersions() throws DeploymentException {
    CloudhubClient client = getCloudHubClient();
    Set<String> supportedMuleVersions = client.getSupportedMuleVersions();
    return new EnvironmentSupportedVersions(supportedMuleVersions);
  }

  /**
   * Creates a CloudHub client based on the deployment configuration.
   * 
   * @return The generated CloudHub client.
   */
  private CloudhubClient getCloudHubClient() {
    CloudhubClient client = new CloudhubClient((CloudHubDeployment) deployment, null);
    client.init();
    return client;
  }
}

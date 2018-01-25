/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation.agent;

import org.mule.tools.client.agent.AgentClient;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.validation.AbstractDeploymentValidator;
import org.mule.tools.validation.EnvironmentSupportedVersions;

/**
 * Validates if the mule runtime version is valid in an Agent deployment scenario.
 */
public class AgentDeploymentValidator extends AbstractDeploymentValidator {

  public AgentDeploymentValidator(Deployment deployment) {
    super(deployment);
  }

  @Override
  public EnvironmentSupportedVersions getEnvironmentSupportedVersions() throws DeploymentException {
    AgentClient client = getAgentClient();
    String muleRuntimeVersion = client.getAgentInfo().getMuleVersion();
    return new EnvironmentSupportedVersions(muleRuntimeVersion);
  }

  /**
   * Creates an Agent client based on the deployment configuration.
   *
   * @return The generated Agent client.
   */
  private AgentClient getAgentClient() {
    return new AgentClient(null, deployment);
  }
}

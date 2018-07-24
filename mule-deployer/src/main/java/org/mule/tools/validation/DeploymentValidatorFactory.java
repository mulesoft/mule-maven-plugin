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
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.model.anypoint.RuntimeFabricDeployment;
import org.mule.tools.model.standalone.StandaloneDeployment;
import org.mule.tools.validation.agent.AgentDeploymentValidator;
import org.mule.tools.validation.arm.ArmDeploymentValidator;
import org.mule.tools.validation.cloudhub.CloudHubDeploymentValidator;
import org.mule.tools.validation.fabric.RuntimeFabricDeploymentValidator;
import org.mule.tools.validation.standalone.StandaloneDeploymentValidator;

/**
 * A factory of {@link AbstractDeploymentValidator}.
 */
public class DeploymentValidatorFactory {

  /**
   * Creates a {@link AbstractDeploymentValidator} for the given deployment.
   *
   * @param deployment The deployment object.
   * @return A {@link AbstractDeploymentValidator}.
   * @throws DeploymentException if the deployment type is not supported.
   */
  public static AbstractDeploymentValidator createDeploymentValidator(Deployment deployment) throws DeploymentException {
    if (deployment instanceof AgentDeployment) {
      return new AgentDeploymentValidator(deployment);
    } else if (deployment instanceof StandaloneDeployment) {
      return new StandaloneDeploymentValidator(deployment);
    } else if (deployment instanceof CloudHubDeployment) {
      return new CloudHubDeploymentValidator(deployment);
    } else if (deployment instanceof ArmDeployment) {
      return new ArmDeploymentValidator(deployment);
    } else if (deployment instanceof RuntimeFabricDeployment) {
      return new RuntimeFabricDeploymentValidator(deployment);
    } else {
      throw new DeploymentException("Cannot configuration is not supported");
    }
  }
}

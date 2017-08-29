/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.client;

import org.mule.tools.client.agent.AgentDeployer;
import org.mule.tools.client.arm.ArmDeployer;
import org.mule.tools.client.cloudhub.CloudhubDeployer;
import org.mule.tools.client.standalone.deployment.ClusterDeployer;
import org.mule.tools.client.standalone.deployment.StandaloneDeployer;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.model.DeployerLog;
import org.mule.tools.model.DeploymentConfiguration;


public class DeployerFactory {

  public AbstractDeployer createDeployer(DeploymentConfiguration deploymentConfiguration, DeployerLog log)
      throws DeploymentException {
    switch (deploymentConfiguration.getDeploymentType()) {
      case standalone:
        return new StandaloneDeployer(deploymentConfiguration, log);
      case cluster:
        return new ClusterDeployer(deploymentConfiguration, log);
      case arm:
        return new ArmDeployer(deploymentConfiguration, log);
      case cloudhub:
        return new CloudhubDeployer(deploymentConfiguration, log);
      case agent:
        return new AgentDeployer(deploymentConfiguration, log);
      default:
        throw new DeploymentException("Unsupported deploymentConfiguration type: "
            + deploymentConfiguration.getDeploymentType());
    }
  }
}

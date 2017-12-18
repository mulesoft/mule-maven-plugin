/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.utils;

import org.mule.tools.client.AbstractDeployer;
import org.mule.tools.client.agent.AgentDeployer;
import org.mule.tools.client.arm.ArmDeployer;
import org.mule.tools.client.cloudhub.CloudhubDeployer;
import org.mule.tools.client.standalone.deployment.StandaloneDeployer;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.model.standalone.ClusterDeployment;
import org.mule.tools.model.standalone.StandaloneDeployment;

public class DeployerFactory {

  public AbstractDeployer createDeployer(Deployment deploymentConfiguration, DeployerLog log)
      throws DeploymentException {
    AbstractDeployer deployer = null;
    if (deploymentConfiguration instanceof StandaloneDeployment) {
      deployer = new StandaloneDeployer((StandaloneDeployment) deploymentConfiguration, log);
    }
    if (deploymentConfiguration instanceof ArmDeployment) {
      deployer = new ArmDeployer((ArmDeployment) deploymentConfiguration, log);
    }
    if (deploymentConfiguration instanceof CloudHubDeployment) {
      deployer = new CloudhubDeployer((CloudHubDeployment) deploymentConfiguration, log);
    }
    if (deploymentConfiguration instanceof AgentDeployment) {
      deployer = new AgentDeployer((AgentDeployment) deploymentConfiguration, log);
    }
    if (deployer == null) {
      throw new DeploymentException("Unsupported deploymentConfiguration type: "
          + deploymentConfiguration);
    }
    initializeDeployer(deployer);
    return deployer;
  }

  protected void initializeDeployer(AbstractDeployer deployer) throws DeploymentException {
    deployer.initialize();
  }
}

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.arm;

import org.mule.tools.client.AbstractDeployer;
import org.mule.tools.client.exception.ClientException;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.client.model.TargetType;

import java.io.File;

import org.apache.maven.plugin.logging.Log;

public class ArmDeployer extends AbstractDeployer {

  private final TargetType targetType;
  private final String target;
  private final ArmClient armClient;

  public ArmDeployer(String uri, String username, String password, String environment, TargetType targetType, String target,
                     File application, String applicationName, Log log, String businessGroup, boolean armInsecure) {
    super(applicationName, application, log);
    this.targetType = targetType;
    this.target = target;
    armClient = new ArmClient(log, uri, username, password, environment, businessGroup, armInsecure);
  }

  @Override
  public void deploy() throws DeploymentException {
    try {
      armClient.init();
      Integer applicationId = armClient.findApplication(getApplicationName(), targetType, target);
      if (applicationId == null) {
        info("Deploying application " + getApplicationName());
        armClient.deployApplication(getApplicationFile(), getApplicationName(), targetType, target);
      } else {
        String alreadyExistsMessage = "Found application %s on %s %s. Redeploying application...";
        info(String.format(alreadyExistsMessage, getApplicationName(), targetType.toString(), target));
        armClient.redeployApplication(applicationId, getApplicationFile(), getApplicationName(), targetType, target);
      }
    } catch (ClientException e) {
      error("Failed: " + e.getMessage());
      throw new DeploymentException("Failed to deploy application " + getApplicationName(), e);
    }
  }

}

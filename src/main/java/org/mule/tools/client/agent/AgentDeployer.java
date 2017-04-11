/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.agent;

import org.mule.tools.client.AbstractDeployer;
import org.mule.tools.client.exception.ClientException;
import org.mule.tools.client.standalone.exception.DeploymentException;

import java.io.File;

import org.apache.maven.plugin.logging.Log;

public class AgentDeployer extends AbstractDeployer {

  private final AgentClient agentClient;

  public AgentDeployer(Log log, String applicationName, File application, String uri) {
    super(applicationName, application, log);
    this.agentClient = new AgentClient(log, uri);
  }

  @Override
  public void deploy() throws DeploymentException {
    try {
      info("Deploying application " + getApplicationName() + " to Mule Agent");
      agentClient.deployApplication(getApplicationName(), getApplicationFile());
    } catch (ClientException e) {
      error("Failure: " + e.getMessage());
      throw new DeploymentException("Failed to deploy application " + getApplicationName(), e);
    }
  }

}

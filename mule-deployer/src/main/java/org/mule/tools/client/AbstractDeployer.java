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

import java.io.File;

import org.apache.maven.project.MavenProject;

import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.utils.DeployerLog;

/**
 * @since 3.0.0
 */
public abstract class AbstractDeployer {

  protected final DeployerLog log;
  protected final Deployment deploymentConfiguration;

  public AbstractDeployer(Deployment deploymentConfiguration, DeployerLog log) throws DeploymentException {
    this.deploymentConfiguration = deploymentConfiguration;
    this.log = log;
    initialize();
  }

  /**
   * Deploys the application.
   *
   * @throws DeploymentException
   */
  public abstract void deploy() throws DeploymentException;

  /**
   * Undeploys the application.
   *
   * @throws DeploymentException
   */
  public abstract void undeploy(MavenProject mavenProject) throws DeploymentException;

  /**
   * Logs an info message in the plugin.
   *
   * @param message The message to log.
   */
  protected void info(String message) {
    log.info(message);
  }

  /**
   * Logs an error message in the plugin.
   * 
   * @param message The message to log.
   */
  protected void error(String message) {
    log.error(message);
  }

  public String getApplicationName() {
    return deploymentConfiguration.getApplicationName();
  }

  public File getApplicationFile() {
    return deploymentConfiguration.getArtifact();
  }

  protected abstract void initialize() throws DeploymentException;

}

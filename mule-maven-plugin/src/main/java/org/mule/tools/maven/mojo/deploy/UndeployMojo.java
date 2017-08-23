/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo.deploy;

import groovy.util.ScriptException;
import org.mule.tools.client.AbstractDeployer;
import org.mule.tools.client.AbstractDeployerFactory;
import org.mule.tools.client.standalone.deployment.StandaloneUndeployer;
import org.mule.tools.client.agent.AgentClient;
import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.client.cloudhub.CloudhubClient;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.model.DeploymentConfigurator;
import org.mule.tools.utils.GroovyUtils;

import javax.ws.rs.NotFoundException;

/**
 * Undeploys all the applications on a Mule Runtime Standalone server, regardless of whether it was started using start or deploy
 * goals.
 *
 * @see DeployMojo
 * @since 1.0
 */
@Mojo(name = "undeploy", requiresProject = true)
public class UndeployMojo extends AbstractMuleDeployerMojo {

  @Override
  public void execute() throws MojoFailureException, MojoExecutionException {
    super.execute();
    try {
      AbstractDeployer deployer = new AbstractDeployerFactory().getDeployer(deploymentConfiguration, getLog());
      deployer.undeploy(mavenProject);
    } catch (DeploymentException e) {
      getLog().error("Failed to undeploy " + deploymentConfiguration.getApplicationName() + ": " + e.getMessage(), e);
      throw new MojoFailureException("Failed to undeploy [" + deploymentConfiguration.getApplication() + "]");
    }
  }
}

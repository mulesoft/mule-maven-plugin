/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

import org.apache.maven.plugins.annotations.Parameter;
import org.mule.tools.client.standalone.Undeployer;
import org.mule.tools.client.agent.AgentClient;
import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.client.cloudhub.CloudhubClient;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import javax.ws.rs.NotFoundException;

/**
 * Undeploys all the applications on a Mule Runtime Standalone server, regardless of whether it was started using start or deploy goals.
 *
 * @author <a href="mailto:asequeira@gmail.com">Ale Sequeira</a>
 * @see DeployMojo
 * @since 1.0
 */
@Mojo(name = "undeploy", requiresProject = true)
public class UndeployMojo extends AbstractMuleMojo {

  /**
   * When set to false, undeployment won't fail if the specified application does not exist.
   *
   * @since 2.2
   */
  @Parameter(defaultValue = "true")
  private boolean failIfNotExists;

  @Override
  protected void doExecute() throws MojoExecutionException, MojoFailureException {
    initializeApplication();
    initializeEnvironment();
    switch (deploymentType) {
      case standalone:
        standalone();
        break;
      case cluster:
        cluster();
        break;
      case arm:
        arm();
        break;
      case cloudhub:
        cloudhub();
        break;
      case agent:
        agent();
        break;
      default:
        throw new MojoFailureException("Unsupported deployment type: " + deploymentType);
    }
  }

  private void cloudhub() throws MojoFailureException {
    CloudhubClient cloudhubClient = new CloudhubClient(uri, getLog(), username, password, environment, businessGroup);
    cloudhubClient.init();
    getLog().info("Stopping application " + applicationName);
    cloudhubClient.stopApplication(applicationName);
  }

  private void arm() throws MojoFailureException {
    ArmClient armClient = new ArmClient(getLog(), uri, username, password, environment, businessGroup, armInsecure);
    armClient.init();
    getLog().info("Undeploying application " + applicationName);
    try {
      armClient.undeployApplication(applicationName, targetType, target);
    } catch (NotFoundException e) {
      if (failIfNotExists) {
        throw e;
      } else {
        getLog().warn("Application not found: " + applicationName);
      }
    }
  }

  private void agent() throws MojoFailureException {
    AgentClient agentClient = new AgentClient(getLog(), uri);
    getLog().info("Undeploying application " + applicationName);
    agentClient.undeployApplication(applicationName);
  }

  private void cluster() throws MojoFailureException, MojoExecutionException {
    File[] muleHomes = new File[size];
    for (int i = 0; i < size; i++) {
      File parentDir = new File(mavenProject.getBuild().getDirectory(), "mule" + i);
      muleHomes[i] = new File(parentDir, "mule-enterprise-standalone-" + muleVersion);

      if (!muleHomes[i].exists()) {
        throw new MojoFailureException(muleHomes[i].getAbsolutePath() + "directory does not exist.");
      }
    }
    new Undeployer(getLog(), applicationName, muleHomes).execute();
  }

  public void standalone() throws MojoFailureException, MojoExecutionException {
    if (!muleHome.exists()) {
      throw new MojoFailureException("MULE_HOME directory does not exist.");
    }
    getLog().info("Using MULE_HOME: " + muleHome);
    new Undeployer(getLog(), applicationName, muleHome).execute();
  }

}

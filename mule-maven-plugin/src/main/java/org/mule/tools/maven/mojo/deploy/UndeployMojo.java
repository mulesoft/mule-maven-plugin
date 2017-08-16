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

import org.mule.tools.client.standalone.deployment.StandaloneUndeployer;
import org.mule.tools.client.agent.AgentClient;
import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.client.cloudhub.CloudhubClient;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.mule.tools.model.DeploymentConfigurator;

import javax.ws.rs.NotFoundException;

/**
 * Undeploys all the applications on a Mule Runtime Standalone server, regardless of whether it was started using start or deploy
 * goals.
 *
 * @author <a href="mailto:asequeira@gmail.com">Ale Sequeira</a>
 * @see DeployMojo
 * @since 1.0
 */
@Mojo(name = "undeploy", requiresProject = true)
public class UndeployMojo extends AbstractMuleDeployerMojo {

  @Override
  protected void cloudhub() throws MojoFailureException {
    CloudhubClient cloudhubClient =
        new CloudhubClient(deploymentConfiguration.getUri(), getLog(), deploymentConfiguration.getUsername(),
                           deploymentConfiguration.getPassword(),
                           deploymentConfiguration.getEnvironment(),
                           deploymentConfiguration.getBusinessGroup());
    cloudhubClient.init();
    getLog().info("Stopping application " + deploymentConfiguration.getApplicationName());
    cloudhubClient.stopApplication(deploymentConfiguration.getApplicationName());
  }

  @Override
  protected void arm() throws MojoFailureException {
    ArmClient armClient =
        new ArmClient(getLog(), deploymentConfiguration.getUri(), deploymentConfiguration.getUsername(),
                      deploymentConfiguration.getPassword(),
                      deploymentConfiguration.getEnvironment(), deploymentConfiguration.getBusinessGroup(),
                      deploymentConfiguration.isArmInsecure());
    armClient.init();
    getLog().info("Undeploying application " + deploymentConfiguration.getApplicationName());
    try {
      armClient.undeployApplication(deploymentConfiguration.getApplicationName(), deploymentConfiguration.getTargetType(),
                                    deploymentConfiguration.getTarget());
    } catch (NotFoundException e) {
      if (deploymentConfiguration.isFailIfNotExists()) {
        throw e;
      } else {
        getLog().warn("Application not found: " + deploymentConfiguration.getApplicationName());
      }
    }
  }

  @Override
  protected void agent() throws MojoFailureException {
    AgentClient agentClient = new AgentClient(getLog(), deploymentConfiguration.getUri());
    getLog().info("Undeploying application " + deploymentConfiguration.getApplicationName());
    agentClient.undeployApplication(deploymentConfiguration.getApplicationName());
  }

  @Override
  protected void cluster() throws MojoFailureException, MojoExecutionException {
    File[] muleHomes = new File[deploymentConfiguration.getSize()];
    for (int i = 0; i < deploymentConfiguration.getSize(); i++) {
      File parentDir = new File(mavenProject.getBuild().getDirectory(), "mule" + i);
      muleHomes[i] = new File(parentDir, "mule-enterprise-standalone-" + deploymentConfiguration.getMuleVersion());

      if (!muleHomes[i].exists()) {
        throw new MojoFailureException(muleHomes[i].getAbsolutePath() + "directory does not exist.");
      }
    }
    new StandaloneUndeployer(getLog(), deploymentConfiguration.getApplicationName(), muleHomes).execute();
  }

  @Override
  protected void standalone() throws MojoFailureException, MojoExecutionException {
    if (!deploymentConfiguration.getMuleHome().exists()) {
      throw new MojoFailureException("MULE_HOME directory does not exist.");
    }
    getLog().info("Using MULE_HOME: " + deploymentConfiguration.getMuleHome());
    new StandaloneUndeployer(getLog(), deploymentConfiguration.getApplicationName(), deploymentConfiguration.getMuleHome())
        .execute();
  }
}

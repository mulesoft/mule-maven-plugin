/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.maven.mojo.deploy;

import org.mule.tools.deployment.DefaultDeployer;
import org.mule.tools.deployment.Deployer;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.mule.tools.client.core.exception.DeploymentException;

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
  public void doExecute() throws MojoFailureException, MojoExecutionException {
    try {
      Deployer deployer = new DefaultDeployer(deploymentConfiguration, log);
      deployer.undeploy();
    } catch (DeploymentException e) {
      getLog().error("Failed to undeploy " + deploymentConfiguration.getApplicationName() + ": " + e.getMessage(), e);
      throw new MojoFailureException("Failed to undeploy [" + deploymentConfiguration.getArtifact() + "]");
    }
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_UNDEPLOY_PREVIOUS_RUN_PLACEHOLDER";
  }
}

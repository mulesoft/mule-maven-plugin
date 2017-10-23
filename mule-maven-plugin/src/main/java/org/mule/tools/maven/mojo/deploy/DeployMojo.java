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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.mule.tools.client.AbstractDeployer;
import org.mule.tools.client.standalone.controller.MuleProcessController;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.maven.mojo.deploy.logging.MavenDeployerLog;
import org.mule.tools.model.standalone.MuleRuntimeDeployment;
import org.mule.tools.utils.DeployerFactory;
import org.mule.tools.utils.GroovyUtils;

/**
 * Maven plugin to deploy Mule applications to different kind of servers: Standalone (both Community and Enterprise), Clustered,
 * Anypoint Runtime Manager and CloudHub. Main uses are running integration tests and deploying applications. Some of the features
 * are: Download Mule Standalone from a Maven Repository and install it locally. Deploy a Mule application to a server. Undeploy a
 * Mule appliction. Assemble a Mule cluster and deploy applications.
 *
 * @author <a href="mailto:asequeira@gmail.com">Ale Sequeira</a>
 * @see UndeployMojo
 * @see MuleProcessController
 * @since 1.0
 */
@Mojo(name = "deploy", requiresProject = true)
public class DeployMojo extends AbstractMuleDeployerMojo {

  @Component
  protected ArchiverManager archiverManager;

  @Override
  public void execute() throws MojoFailureException, MojoExecutionException {
    super.execute();
    if (deploymentConfiguration instanceof MuleRuntimeDeployment) {
      runScript((MuleRuntimeDeployment) deploymentConfiguration);
    }

    try {
      AbstractDeployer deployer =
          new DeployerFactory().createDeployer(deploymentConfiguration, new MavenDeployerLog(getLog()));
      deployer.resolveDependencies(mavenProject, artifactResolver, archiverManager, artifactFactory, localRepository);
      deployer.deploy();
    } catch (DeploymentException | ScriptException e) {
      getLog().error("Failed to deploy " + deploymentConfiguration.getApplicationName() + ": " + e.getMessage(), e);
      throw new MojoFailureException("Failed to deploy [" + deploymentConfiguration.getApplication() + "]");
    }
  }

  private void runScript(MuleRuntimeDeployment deploymentConfiguration) throws MojoExecutionException {
    if (null != deploymentConfiguration.getScript()) {
      try {
        GroovyUtils.executeScript(mavenProject, deploymentConfiguration.getScript());
      } catch (ScriptException e) {
        throw new MojoExecutionException("There was a problem trying to deploy the application: "
            + deploymentConfiguration.getApplicationName(), e);
      }
    }
  }
}

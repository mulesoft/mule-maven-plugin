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

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.mule.tools.maven.mojo.deploy.logging.MavenDeployerLog;
import org.mule.tools.model.DeployerLog;
import org.mule.tools.model.DeploymentConfiguration;
import org.mule.tools.model.DeploymentConfigurator;

public abstract class AbstractMuleDeployerMojo extends AbstractMojo {

  @Parameter
  protected DeploymentConfiguration deploymentConfiguration;

  @Component
  protected Settings settings;

  @Component
  protected SettingsDecrypter decrypter;

  @Component
  protected MavenProject mavenProject;

  @Component
  protected ArtifactFactory artifactFactory;

  @Component
  protected ArtifactResolver artifactResolver;

  @Parameter(defaultValue = "${localRepository}", readonly = true)
  protected ArtifactRepository localRepository;
  private DeploymentConfigurator deploymentConfigurator;

  protected DeployerLog log;


  /**
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    log = new MavenDeployerLog(getLog());
    if (deploymentConfiguration == null) {
      throw new MojoFailureException("No deployment configuration was defined. Aborting.");
    } else if (StringUtils.isNotEmpty(deploymentConfiguration.getSkip()) && "true".equals(deploymentConfiguration.getSkip())) {
      getLog().info("Skipping execution: skip=" + deploymentConfiguration.getSkip());
    } else {
      deploymentConfigurator = new DeploymentConfigurator(deploymentConfiguration, new MavenDeployerLog(getLog()));
      getLog().debug("Executing mojo, skip=" + deploymentConfiguration.getSkip());
      doExecute();
    }
  }

  protected void doExecute() throws MojoExecutionException, MojoFailureException {
    deploymentConfigurator.initializeApplication(artifactFactory, mavenProject, artifactResolver, localRepository);
    deploymentConfigurator.initializeEnvironment(settings, decrypter);
    switch (deploymentConfiguration.getDeploymentType()) {
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
        throw new MojoFailureException("Unsupported deploymentConfiguration type: "
            + deploymentConfiguration.getDeploymentType());
    }
  }

  protected abstract void cluster() throws MojoFailureException, MojoExecutionException;

  protected abstract void standalone() throws MojoFailureException, MojoExecutionException;

  protected abstract void arm() throws MojoFailureException, MojoExecutionException;

  protected abstract void cloudhub() throws MojoFailureException, MojoExecutionException;

  protected abstract void agent() throws MojoFailureException, MojoExecutionException;

}

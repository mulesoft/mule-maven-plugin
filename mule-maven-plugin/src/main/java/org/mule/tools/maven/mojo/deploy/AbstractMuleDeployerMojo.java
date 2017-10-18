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
import org.mule.tools.client.standalone.deployment.StandaloneDeployer;
import org.mule.tools.maven.mojo.deploy.logging.MavenDeployerLog;
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.model.anypoint.AnypointDeployment;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.model.standalone.ClusterDeployment;
import org.mule.tools.model.standalone.StandaloneDeployment;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.model.DeploymentConfiguration;
import org.mule.tools.model.anypoint.DeploymentConfigurator;

import static com.google.common.base.Preconditions.checkState;

public abstract class AbstractMuleDeployerMojo extends AbstractMojo {

  @Parameter
  protected DeploymentConfiguration deploymentConfiguration;

  @Parameter
  protected CloudHubDeployment cloudHubDeployment;

  @Parameter
  protected ArmDeployment armDeployment;

  @Parameter
  protected StandaloneDeployment standaloneDeployment;

  @Parameter
  protected AgentDeployment agentDeployment;

  @Parameter
  protected ClusterDeployment clusterDeployment;

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
    validateUniqueDeployment();
    log = new MavenDeployerLog(getLog());
    if (deploymentConfiguration == null) {
      throw new MojoFailureException("No deployment configuration was defined. Aborting.");
    } else if (StringUtils.isNotEmpty(deploymentConfiguration.getSkip()) && "true".equals(deploymentConfiguration.getSkip())) {
      getLog().info("Skipping execution: skip=" + deploymentConfiguration.getSkip());
    } else if (deploymentConfiguration instanceof AnypointDeployment) {
      deploymentConfigurator =
          new DeploymentConfigurator((AnypointDeployment) deploymentConfiguration, new MavenDeployerLog(getLog()));
      deploymentConfigurator.initializeApplication(artifactFactory, mavenProject, artifactResolver, localRepository);
      deploymentConfigurator.initializeEnvironment(settings, decrypter);
      getLog().debug("Executing mojo, skip=" + deploymentConfiguration.getSkip());
    }
  }

  private void validateUniqueDeployment() throws MojoExecutionException {
    checkDeployment(cloudHubDeployment, deploymentConfiguration);
    checkDeployment(armDeployment, deploymentConfiguration);
    checkDeployment(standaloneDeployment, deploymentConfiguration);
    checkDeployment(agentDeployment, deploymentConfiguration);
    checkDeployment(clusterDeployment, deploymentConfiguration);
    checkState(deploymentConfiguration != null, "Deployment configuration is missing");
  }

  protected void checkDeployment(DeploymentConfiguration deploymentImplementation,
                                 DeploymentConfiguration deploymentConfiguration)
      throws MojoExecutionException {
    if (deploymentImplementation != null) {
      if (deploymentConfiguration != null) {
        throw new MojoExecutionException("One and only one deployment type can be set up per build. Aborting");
      }
      this.deploymentConfiguration = deploymentImplementation;
    }
  }
}

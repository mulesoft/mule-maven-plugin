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
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.maven.mojo.AbstractGenericMojo;
import org.mule.tools.maven.mojo.deploy.logging.MavenDeployerLog;
import org.mule.tools.model.anypoint.AnypointDeployment;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.model.anypoint.DeploymentConfigurator;

import java.util.Objects;
import java.util.stream.Stream;

import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.DEPLOY;

public abstract class AbstractMuleDeployerMojo extends AbstractGenericMojo {

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

  private DeploymentConfigurator deploymentConfigurator;

  @Parameter(readonly = true, property = "applicationName", defaultValue = "${project.artifactId}")
  protected String applicationName;

  @Parameter(readonly = true, property = "artifact", defaultValue = "${project.artifact.file}")
  protected String artifact;

  protected DeployerLog log;

  /**
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      getProjectValidator().isProjectValid(DEPLOY.id());
      setDeployment();
    } catch (DeploymentException | ValidationException e) {
      throw new MojoExecutionException("Deployment configuration is not valid, ", e);
    }
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

  protected void setDeployment() throws DeploymentException {
    deploymentConfiguration = Stream.of(agentDeployment, standaloneDeployment, armDeployment, cloudHubDeployment,
                                        clusterDeployment)
        .filter(Objects::nonNull).findFirst()
        .orElseThrow(() -> new DeploymentException("Please define one deployment configuration"));
    deploymentConfiguration.setDefaultValues(mavenProject);
  }
}

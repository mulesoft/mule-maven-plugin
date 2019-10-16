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

import static org.mule.tools.maven.config.proxy.ProxyConfiguration.isAbleToSetupProxy;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.validation.deployment.ProjectDeploymentValidator;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.maven.config.proxy.ProxyConfiguration;
import org.mule.tools.maven.mojo.AbstractGenericMojo;
import org.mule.tools.maven.mojo.deploy.logging.MavenDeployerLog;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.AnypointDeployment;
import org.mule.tools.model.anypoint.DeploymentConfigurator;
import org.mule.tools.model.anypoint.MavenResolverMetadata;
import org.mule.tools.utils.DeployerLog;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.SettingsDecrypter;

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

  @Parameter(readonly = true, property = "applicationName", defaultValue = "${project.artifactId}")
  protected String applicationName;

  @Parameter(readonly = true, property = "artifact", defaultValue = "${project.artifact.file}")
  protected String artifact;

  protected DeployerLog log;

  protected Deployment deploymentConfiguration;

  /**
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    initMojo();
    log = new MavenDeployerLog(getLog());
    try {
      validateUniqueDeploymentConfiguration();
      new ProjectDeploymentValidator(getProjectInformation()).isDeployable();
      deploymentConfiguration = getDeploymentConfiguration();
    } catch (DeploymentException | ValidationException e) {
      throw new MojoExecutionException("Deployment configuration is not valid, ", e);
    }

    if (StringUtils.isNotEmpty(deploymentConfiguration.getSkip()) && "true".equals(deploymentConfiguration.getSkip())) {
      getLog().info("Skipping execution: skip=" + deploymentConfiguration.getSkip());
      return;
    }

    setupProxy();

    if (deploymentConfiguration instanceof AnypointDeployment) {
      initializeAnypointDeploymentEnvironment();
    }

    getLog().debug("Executing mojo, skip=" + deploymentConfiguration.getSkip());
    doExecute();
  }

  protected Deployment getDeploymentConfiguration() throws DeploymentException {
    List<Deployment> deployments = getProjectInformation().getDeployments().stream().filter(Objects::nonNull)
        .collect(Collectors.toList());

    if (deployments.isEmpty()) {
      throw new DeploymentException("No deployment configuration was defined. Aborting.");
    }

    Deployment deploymentConfiguration = deployments.get(0);
    deploymentConfiguration.setDefaultValues(mavenProject);
    return deploymentConfiguration;
  }

  private void validateUniqueDeploymentConfiguration() throws ValidationException {
    List<Deployment> deployments = getProjectInformation().getDeployments().stream()
        .filter(Objects::nonNull).collect(Collectors.toList());

    if (deployments.isEmpty()) {
      throw new ValidationException("No deployment configuration was defined. Aborting.");
    }
    if (deployments.size() > 1) {
      throw new ValidationException("One and only one deployment type can be set up per build. Aborting");
    }
  }

  public MavenResolverMetadata getMetadata() {
    return new MavenResolverMetadata()
        .setFactory(artifactFactory)
        .setLocalRepository(localRepository)
        .setProject(project)
        .setResolver(artifactResolver);
  }

  public void initializeAnypointDeploymentEnvironment() throws MojoFailureException, MojoExecutionException {
    DeploymentConfigurator deploymentConfigurator =
        new DeploymentConfigurator((AnypointDeployment) deploymentConfiguration, new MavenDeployerLog(getLog()));
    deploymentConfigurator.initializeApplication(getMetadata());
    deploymentConfigurator.initializeEnvironment(settings, decrypter);
  }

  protected void setupProxy() {
    if (isAbleToSetupProxy(settings)) {
      try {
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration(getLog(), settings);
        proxyConfiguration.handleProxySettings();
      } catch (Exception e) {
        getLog().error("Fail to configure proxy settings.", e);
      }
    }
  }
}

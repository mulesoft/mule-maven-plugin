/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.cloudhub;

import groovy.util.ScriptException;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.mule.tools.client.AbstractDeployer;
import org.mule.tools.client.exception.ClientException;
import org.mule.tools.client.standalone.exception.DeploymentException;

import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.utils.DeployerLog;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class CloudhubDeployer extends AbstractDeployer {

  private final CloudHubDeployment cloudhubDeployment;
  private CloudhubClient cloudhubClient;

  public CloudhubDeployer(CloudHubDeployment cloudHubDeployment, DeployerLog log) throws DeploymentException {
    super(cloudHubDeployment, log);
    this.cloudhubDeployment = cloudHubDeployment;
  }

  @Override
  public void deploy() throws DeploymentException {
    cloudhubClient.init();

    info("Deploying application " + getApplicationName() + " to Cloudhub");

    if (!getApplicationFile().exists()) {
      throw new DeploymentException("Application file " + getApplicationFile() + " does not exist.");
    }

    try {
      boolean domainAvailable = cloudhubClient.isNameAvailable(getApplicationName());

      if (domainAvailable) {
        info("Creating application " + getApplicationName());
        cloudhubClient.createApplication(getApplicationName(), cloudhubDeployment.getRegion(),
                                         cloudhubDeployment.getMuleVersion().get(), cloudhubDeployment.getWorkers().get(),
                                         cloudhubDeployment.getWorkerType(), cloudhubDeployment.getProperties());
      } else {
        Application app = findApplicationFromCurrentUser(getApplicationName());

        if (app != null) {
          info("Application " + getApplicationName() + " already exists, redeploying");

          String updateRegion = (cloudhubDeployment.getRegion() == null) ? app.region : cloudhubDeployment.getRegion();
          String updateMuleVersion =
              (cloudhubDeployment.getMuleVersion() == null) ? app.muleVersion : cloudhubDeployment.getMuleVersion().get();
          Integer updateWorkers =
              (cloudhubDeployment.getWorkers() == null) ? app.workers : cloudhubDeployment.getWorkers().get();
          String updateWorkerType =
              (cloudhubDeployment.getWorkerType() == null) ? app.workerType : cloudhubDeployment.getWorkerType();

          cloudhubClient.updateApplication(getApplicationName(), updateRegion, updateMuleVersion, updateWorkers, updateWorkerType,
                                           cloudhubDeployment.getProperties());
        } else {
          error("Domain " + getApplicationName() + " is not available. Aborting.");
          throw new DeploymentException("Domain " + getApplicationName() + " is not available. Aborting.");
        }
      }

      info("Uploading application contents " + getApplicationName());
      cloudhubClient.uploadFile(getApplicationName(), getApplicationFile());

      info("Starting application " + getApplicationName());
      cloudhubClient.startApplication(getApplicationName());
    } catch (ClientException e) {
      error("Failed: " + e.getMessage());
      throw new DeploymentException("Failed to deploy application " + getApplicationName(), e);
    }
  }

  @Override
  public void undeploy(MavenProject mavenProject) throws DeploymentException {
    CloudhubClient cloudhubClient =
        new CloudhubClient(cloudhubDeployment, log);
    cloudhubClient.init();
    log.info("Stopping application " + cloudhubDeployment.getApplicationName());
    cloudhubClient.stopApplication(cloudhubDeployment.getApplicationName());
  }

  @Override
  protected void initialize() {
    this.cloudhubClient = new CloudhubClient((CloudHubDeployment) deploymentConfiguration, log);
  }

  @Override
  public void resolveDependencies(MavenProject mavenProject, ArtifactResolver artifactResolver, ArchiverManager archiverManager,
                                  ArtifactFactory artifactFactory, ArtifactRepository localRepository)
      throws DeploymentException, ScriptException {

  }

  protected Application findApplicationFromCurrentUser(String appName) {
    checkArgument(StringUtils.isNotBlank(appName), "Application name should not be blank nor null");
    for (Application app : getApplications()) {
      if (appName.equalsIgnoreCase(app.domain)) {
        return app;
      }
    }
    return null;
  }

  public List<Application> getApplications() {
    return cloudhubClient.getApplications();
  }
}

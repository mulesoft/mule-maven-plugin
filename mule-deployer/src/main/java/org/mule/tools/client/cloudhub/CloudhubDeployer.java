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
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.mule.tools.client.AbstractDeployer;
import org.mule.tools.client.exception.ClientException;
import org.mule.tools.client.standalone.exception.DeploymentException;

import org.mule.tools.model.DeployerLog;
import org.mule.tools.model.DeploymentConfiguration;

public class CloudhubDeployer extends AbstractDeployer {

  private CloudhubClient cloudhubClient;

  public CloudhubDeployer(DeploymentConfiguration deploymentConfiguration, DeployerLog log) throws DeploymentException {
    super(deploymentConfiguration, log);
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
        cloudhubClient.createApplication(getApplicationName(), deploymentConfiguration.getRegion(),
                                         deploymentConfiguration.getMuleVersion(), deploymentConfiguration.getWorkers(),
                                         deploymentConfiguration.getWorkerType(), deploymentConfiguration.getProperties());
      } else {
        Application app = findApplicationFromCurrentUser(getApplicationName());

        if (app != null) {
          info("Application " + getApplicationName() + " already exists, redeploying");

          String updateRegion = (deploymentConfiguration.getRegion() == null) ? app.region : deploymentConfiguration.getRegion();
          String updateMuleVersion =
              (deploymentConfiguration.getMuleVersion() == null) ? app.muleVersion : deploymentConfiguration.getMuleVersion();
          Integer updateWorkers =
              (deploymentConfiguration.getWorkers() == null) ? app.workers : deploymentConfiguration.getWorkers();
          String updateWorkerType =
              (deploymentConfiguration.getWorkerType() == null) ? app.workerType : deploymentConfiguration.getWorkerType();

          cloudhubClient.updateApplication(getApplicationName(), updateRegion, updateMuleVersion, updateWorkers, updateWorkerType,
                                           deploymentConfiguration.getProperties());
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
        new CloudhubClient(deploymentConfiguration.getUri(), log, deploymentConfiguration.getUsername(),
                           deploymentConfiguration.getPassword(),
                           deploymentConfiguration.getEnvironment(),
                           deploymentConfiguration.getBusinessGroup());
    cloudhubClient.init();
    log.info("Stopping application " + deploymentConfiguration.getApplicationName());
    cloudhubClient.stopApplication(deploymentConfiguration.getApplicationName());
  }

  @Override
  protected void initialize() {
    this.cloudhubClient = new CloudhubClient(deploymentConfiguration.getUri(), log, deploymentConfiguration.getUsername(),
                                             deploymentConfiguration.getPassword(), deploymentConfiguration.getEnvironment(),
                                             deploymentConfiguration.getBusinessGroup());
  }

  @Override
  public void resolveDependencies(MavenProject mavenProject, ArtifactResolver artifactResolver, ArchiverManager archiverManager,
                                  ArtifactFactory artifactFactory, ArtifactRepository localRepository)
      throws DeploymentException, ScriptException {

  }

  private Application findApplicationFromCurrentUser(String appName) {
    for (Application app : cloudhubClient.getApplications()) {
      if (appName.equals(app.domain)) {
        return app;
      }
    }
    return null;
  }

}

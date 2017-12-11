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

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.tools.client.cloudhub.CloudhubClient.STARTED_STATUS;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.project.MavenProject;

import org.mule.tools.client.AbstractDeployer;
import org.mule.tools.client.cloudhub.OperationRetrier.RetriableOperation;
import org.mule.tools.client.exception.ClientException;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.utils.DeployerLog;

public class CloudhubDeployer extends AbstractDeployer {

  private static final String VALIDATE_APPLICATION_STARTED = "cloudhub.deployer.validate.application.started";
  private static final String VALIDATE_APPLICATION_STARTED_SLEEP = "cloudhub.deployer.validate.application.started.sleep";
  private static final String VALIDATE_APPLICATION_STARTED_ATTEMPTS = "cloudhub.deployer.validate.application.started.attempts";

  private CloudhubClient cloudhubClient;
  private final CloudHubDeployment cloudhubDeployment;

  public CloudhubDeployer(CloudHubDeployment cloudHubDeployment, DeployerLog log) throws DeploymentException {
    super(cloudHubDeployment, log);
    this.cloudhubDeployment = cloudHubDeployment;
  }

  @Override
  public void deploy() throws DeploymentException {
    getCloudhubClient().init();

    info("Deploying application " + getApplicationName() + " to Cloudhub");

    if (!getApplicationFile().exists()) {
      throw new DeploymentException("Application file " + getApplicationFile() + " does not exist.");
    }

    try {
      boolean domainAvailable = cloudhubClient.isNameAvailable(getApplicationName());

      if (domainAvailable) {
        info("Creating application: " + getApplicationName());
        getCloudhubClient().createApplication(getApplicationName(), cloudhubDeployment.getRegion(),
                                              cloudhubDeployment.getMuleVersion().get(), cloudhubDeployment.getWorkers().get(),
                                              cloudhubDeployment.getWorkerType(), cloudhubDeployment.getProperties());
      } else {
        Application app = findApplicationFromCurrentUser(getApplicationName());

        if (app != null) {
          info("Application: " + getApplicationName() + " already exists, redeploying");

          String updateRegion = (cloudhubDeployment.getRegion() == null) ? app.region : cloudhubDeployment.getRegion();
          String updateMuleVersion =
              (cloudhubDeployment.getMuleVersion() == null) ? app.muleVersion : cloudhubDeployment.getMuleVersion().get();
          Integer updateWorkers =
              (cloudhubDeployment.getWorkers() == null) ? app.workers : cloudhubDeployment.getWorkers().get();
          String updateWorkerType =
              (cloudhubDeployment.getWorkerType() == null) ? app.workerType : cloudhubDeployment.getWorkerType();

          getCloudhubClient().updateApplication(getApplicationName(), updateRegion, updateMuleVersion, updateWorkers,
                                                updateWorkerType, cloudhubDeployment.getProperties());
        } else {
          error("Application name: " + getApplicationName() + " is not available. Aborting.");
          throw new DeploymentException("Domain " + getApplicationName() + " is not available. Aborting.");
        }
      }

      info("Uploading application contents " + getApplicationName());
      getCloudhubClient().uploadFile(getApplicationName(), getApplicationFile());

      info("Starting application: " + getApplicationName());
      getCloudhubClient().startApplication(getApplicationName());

      if (validateApplicationHasStarted()) {
        info("Checking application: " + getApplicationName() + " has started");
        validateApplicationIsInStatus(getApplicationName(), STARTED_STATUS);
      }

    } catch (ClientException e) {
      error("Failed: " + e.getMessage());
      throw new DeploymentException("Failed to deploy application " + getApplicationName(), e);
    }
  }

  @Override
  public void undeploy(MavenProject mavenProject) throws DeploymentException {
    getCloudhubClient().init();

    log.info("Stopping application " + cloudhubDeployment.getApplicationName());
    getCloudhubClient().stopApplication(cloudhubDeployment.getApplicationName());
  }

  @Override
  protected void initialize() {
    cloudhubClient = new CloudhubClient((CloudHubDeployment) deploymentConfiguration, log);
  }

  protected CloudhubClient getCloudhubClient() {
    if (cloudhubClient == null) {
      throw new IllegalStateException("You must initialize the " + this.getClass().getName());
    }
    return cloudhubClient;
  }

  protected Application findApplicationFromCurrentUser(String appName) {
    checkArgument(StringUtils.isNotBlank(appName), "Application name should not be blank nor null");
    for (Application app : getCloudhubClient().getApplications()) {
      if (appName.equalsIgnoreCase(app.domain)) {
        return app;
      }
    }
    return null;
  }

  private Boolean validateApplicationHasStarted() {
    return Boolean.valueOf(System.getProperty(VALIDATE_APPLICATION_STARTED, "false"));
  }


  private void validateApplicationIsInStatus(String applicationName, String status) throws DeploymentException {
    log.debug("Checking application " + applicationName + " for status " + status + "...");

    RetriableOperation operation = () -> {
      Application application = cloudhubClient.getApplication(applicationName);
      if (application != null && status.equals(application.status)) {
        return false;
      }
      return true;
    };

    try {
      OperationRetrier operationRetrier = new OperationRetrier();
      operationRetrier.setAttempts(Integer.valueOf(System.getProperty(VALIDATE_APPLICATION_STARTED_ATTEMPTS, "20")));
      operationRetrier.setSleepTime(Long.valueOf(System.getProperty(VALIDATE_APPLICATION_STARTED_SLEEP, "60000")));

      operationRetrier.retry(operation);
    } catch (Exception e) {
      throw new DeploymentException("Failed to deploy application " + applicationName
          + ". Fail to verify the application has started", e);
    }
  }
}

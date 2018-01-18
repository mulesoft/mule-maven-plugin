/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.cloudhub;

import org.apache.commons.lang3.StringUtils;
import org.mule.tools.client.OperationRetrier;
import org.mule.tools.client.cloudhub.Application;
import org.mule.tools.client.cloudhub.ApplicationMetadata;
import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.deployment.artifact.ArtifactDeployer;
import org.mule.tools.utils.DeployerLog;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Boolean.getBoolean;
import static java.lang.Integer.getInteger;
import static java.lang.Long.getLong;
import static org.mule.tools.client.cloudhub.CloudHubClient.STARTED_STATUS;

/**
 * Deploys mule artifacts to CloudHub using the {@link CloudHubClient}.
 */
public class CloudHubArtifactDeployer implements ArtifactDeployer {

  private static final Long DEFAULT_CLOUDHUB_DEPLOYMENT_TIMEOUT = 1200000L;
  private final CloudHubDeployment deployment;
  private final DeployerLog log;
  private CloudHubClient client;
  private ApplicationMetadata application;
  private boolean isClientInitialized = false;

  public CloudHubArtifactDeployer(Deployment deployment, DeployerLog log) {
    this(deployment, getCloudHubClient(deployment, log), log);
  }

  public CloudHubArtifactDeployer(Deployment deployment, CloudHubClient cloudHubClient, DeployerLog log) {
    this.deployment = (CloudHubDeployment) deployment;
    this.client = cloudHubClient;
    this.log = log;
  }

  @Override
  public void deployDomain() throws DeploymentException {
    throw new DeploymentException("Deployment of domains to CloudHub is not supported");
  }

  @Override
  public void undeployDomain() throws DeploymentException {
    throw new DeploymentException("Uneployment of domains from CloudHub is not supported");
  }

  /**
   * Deploys an application to CloudHub. It creates the application in CloudHub, uploads its contents and triggers its start.
   *
   * @throws DeploymentException
   */
  @Override
  public void deployApplication() throws DeploymentException {
    persistApplication();
    uploadContents();
    startApplication();
    checkApplicationHasStarted();
  }

  /**
   * Deploys an application to CloudHub, stopping it.
   *
   * @throws DeploymentException If the application does not exist or some internal error in CloudHub happens
   */
  @Override
  public void undeployApplication() throws DeploymentException {
    log.info("Stopping application " + deployment.getApplicationName());
    getClient().stopApplication(deployment.getApplicationName());
  }

  /**
   * Creates the application in CloudHub if the domain is available. Otherwise, it tries to update the existent application.
   *
   * @throws DeploymentException If the application is not available and cannot be updated
   */
  protected void persistApplication() throws DeploymentException {
    ApplicationMetadata applicationMetadata = getMetadata();
    boolean domainAvailable = getClient().isNameAvailable(deployment.getApplicationName());
    if (domainAvailable) {
      createApplication(applicationMetadata);
    } else {
      updateApplication(applicationMetadata);
    }
  }

  /**
   * Uploads the jar contents to CloudHub.
   */
  protected void uploadContents() {
    log.info("Uploading application contents " + deployment.getApplicationName());
    getClient().uploadFile(deployment.getApplicationName(), deployment.getArtifact());
  }

  /**
   * Creates the application in CloudHub.
   *
   * @param applicationMetadata The metadata of the application to be created
   */
  protected void createApplication(ApplicationMetadata applicationMetadata) {
    log.info("Creating application: " + deployment.getApplicationName());
    getClient().createApplication(applicationMetadata);
  }

  /**
   * Updates the application in CloudHub.
   *
   * @param applicationMetadata The metadata of the application to be updated
   * @throws DeploymentException In case the application is not available for the current user or some other internal in CloudHub
   *         happens
   */
  protected void updateApplication(ApplicationMetadata applicationMetadata) throws DeploymentException {
    Application currentApplication = findApplicationFromCurrentUser(deployment.getApplicationName());
    if (currentApplication != null) {
      log.info("Application: " + deployment.getApplicationName() + " already exists, redeploying");
      applicationMetadata.updateValues(currentApplication);
      getClient().updateApplication(applicationMetadata);
    } else {
      log.error("Application name: " + deployment.getApplicationName() + " is not available. Aborting.");
      throw new DeploymentException("Domain " + deployment.getApplicationName() + " is not available. Aborting.");
    }
  }

  /**
   * Starts an application in CloudHub. The application is supposed to be already created and its contents should have already
   * been uploaded.
   */
  protected void startApplication() {
    log.info("Starting application: " + deployment.getApplicationName());
    getClient().startApplication(deployment.getApplicationName());
  }

  /**
   * Checks if an application in CloudHub has the {@code STARTED_STATUS} status.
   *
   * @throws DeploymentException In case it timeouts while checking for the status
   */
  protected void checkApplicationHasStarted() throws DeploymentException {
    log.info("Checking application: " + deployment.getApplicationName() + " has started");
    validateApplicationIsInStatus(deployment.getApplicationName(), STARTED_STATUS);
  }

  /**
   * Tries to find the application id.
   *
   * @param appName The application name. It cannot be null nor blank
   * @return The id if the application is available for the user defined in the client session; {@code null} otherwise
   */
  protected Application findApplicationFromCurrentUser(String appName) {
    checkArgument(StringUtils.isNotBlank(appName), "Application name should not be blank nor null");
    for (Application app : getClient().getApplications()) {
      if (appName.equalsIgnoreCase(app.domain)) {
        return app;
      }
    }
    return null;
  }

  /**
   * Validates the application status against a specified status.
   * 
   * @param applicationName
   * @param status
   * @throws DeploymentException
   */
  protected void validateApplicationIsInStatus(String applicationName, String status) throws DeploymentException {
    log.info("Checking application " + applicationName + " for status " + status + "...");
    OperationRetrier.RetriableOperation statusIsNotTheExpected = () -> !isExpectedStatus(applicationName, status);
    retryValidation(statusIsNotTheExpected);
  }

  /**
   * Retries the validation. The operation is performed a defined number of attempts, and these attempts happen separated by a
   * specified length of time. The total amount of time attempts x sleep time is the deployment timeout.
   *
   * If it timeouts, the deployment is stopped in CloudHub.
   * 
   * @param validationOperation The validation to be retried
   * @throws DeploymentException If the validation timeouts
   */
  private void retryValidation(OperationRetrier.RetriableOperation validationOperation) throws DeploymentException {
    Long timeout = deployment.getDeploymentTimeout().orElse(DEFAULT_CLOUDHUB_DEPLOYMENT_TIMEOUT);
    try {
      OperationRetrier operationRetrier = new OperationRetrier();
      operationRetrier.setTimeout(timeout);
      operationRetrier.retry(validationOperation);
    } catch (Exception e) {
      undeployApplication();
      String message = "Application " + getApplicationName() + " deployment has timeouted.";
      if (!deployment.getDeploymentTimeout().isPresent()) {
        message += "The default deployment timeout is " + DEFAULT_CLOUDHUB_DEPLOYMENT_TIMEOUT + " ms. It is possible to " +
            "set this property deploymentTimeout in the deployment configuration";
      }
      throw new DeploymentException(message, e);
    }
  }

  /**
   * Retrieves the application name.
   * 
   * @return The application name
   */
  protected String getApplicationName() {
    return deployment.getApplicationName();
  }

  public CloudHubClient getClient() {
    if (!isClientInitialized) {
      client.init();
      isClientInitialized = true;
    }
    return client;
  }


  /**
   * Retrieves a CloudHub client based on the deployment configuration and the log.
   *
   * @param deployment The deployment configuration.
   * @param log
   * @return A {@link CloudHubClient} created based on the deployment configuration and the log.
   */
  private static CloudHubClient getCloudHubClient(Deployment deployment, DeployerLog log) {
    return new CloudHubClient((CloudHubDeployment) deployment, log);
  }

  /**
   * Retrieves an application metadata based on the deployment configuration.
   *
   * @return An {@link ApplicationMetadata} based on the deployment configuration.
   */
  public ApplicationMetadata getMetadata() {
    if (application == null) {
      application = new ApplicationMetadata(deployment);
    }
    return application;
  }

  /**
   * Method to check if the status of the application is the expected.
   * 
   * @param applicationName The name of the application to be checked
   * @param status The expected status
   * @return true if the application can be found and its status is equals to {@param status}
   */
  protected boolean isExpectedStatus(String applicationName, CharSequence status) {
    Application application = getClient().getApplication(applicationName);
    return application != null && StringUtils.equals(status, application.status);
  }
}

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
import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.cloudhub.model.MuleVersion;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.deployment.artifact.ArtifactDeployer;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.verification.cloudhub.CloudHubDeploymentVerification;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Integer.getInteger;
import static java.lang.Long.getLong;

/**
 * Deploys mule artifacts to CloudHub using the {@link CloudHubClient}.
 */
public class CloudHubArtifactDeployer implements ArtifactDeployer {

  private static final Long DEFAULT_CLOUDHUB_DEPLOYMENT_TIMEOUT = 600000L;
  private final CloudHubDeployment deployment;
  private final DeployerLog log;
  private CloudHubClient client;
  private ApplicationMetadata application; // TODO applicationMetadata
  private boolean isClientInitialized = false;

  public CloudHubArtifactDeployer(Deployment deployment, DeployerLog log) {
    this(deployment, getCloudHubClient(deployment, log), log);
  }

  public CloudHubArtifactDeployer(Deployment deployment, CloudHubClient cloudHubClient, DeployerLog log) {
    this.deployment = (CloudHubDeployment) deployment;
    if (!this.deployment.getDeploymentTimeout().isPresent()) {
      this.deployment.setDeploymentTimeout(DEFAULT_CLOUDHUB_DEPLOYMENT_TIMEOUT);
    }
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
    getClient().stopApplications(deployment.getApplicationName());
  }

  /**
   * Creates the application in CloudHub if the domain name is available. Otherwise, it tries to update the existent application.
   *
   * @throws DeploymentException If the application is not available and cannot be updated
   */ // TODO change name
  protected void persistApplication() throws DeploymentException {
    ApplicationMetadata applicationMetadata = getMetadata();
    boolean domainAvailable = getClient().isDomainAvailable(deployment.getApplicationName()); // TODO domain name
    if (domainAvailable) {
      createApplication(applicationMetadata);
    } else {
      updateApplication(applicationMetadata);
    }
  }

  /**
   * Creates the application in CloudHub.
   *
   * @param applicationMetadata The metadata of the application to be created
   */
  protected void createApplication(ApplicationMetadata applicationMetadata) {
    log.info("Creating application: " + deployment.getApplicationName());

    // TODO this conversion should be in another place
    MuleVersion muleVersion = new MuleVersion();
    muleVersion.setVersion(applicationMetadata.getMuleVersion().get());

    Application application = new Application();
    application.setDomain(applicationMetadata.getName());
    application.setMuleVersion(muleVersion);
    application.setProperties(applicationMetadata.getProperties());
    application.setRegion(applicationMetadata.getRegion());


    getClient().createApplications(application, deployment.getArtifact());
  }

  /**
   * Updates the application in CloudHub.
   *
   * @param applicationMetadata The metadata of the application to be updated
   * @throws DeploymentException In case the application is not available for the current user or some other internal in CloudHub
   *         happens
   */
  protected void updateApplication(ApplicationMetadata applicationMetadata) throws DeploymentException {
    Application currentApplication = getClient().getApplications(deployment.getApplicationName());
    if (currentApplication != null) {
      log.info("Application: " + deployment.getApplicationName() + " already exists, redeploying");
      applicationMetadata.updateValues(currentApplication);

      // TODO we should update stuff based on original stuff
      MuleVersion muleVersion = new MuleVersion();
      muleVersion.setVersion(applicationMetadata.getMuleVersion().get());

      Application application = new Application();
      application.setDomain(applicationMetadata.getName());
      application.setMuleVersion(muleVersion);
      application.setProperties(applicationMetadata.getProperties());
      application.setRegion(applicationMetadata.getRegion());



      getClient().updateApplications(application, deployment.getArtifact());
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
    getClient().startApplications(deployment.getApplicationName());
  }

  /**
   * Checks if an application in CloudHub has the {@code STARTED_STATUS} status.
   *
   * @throws DeploymentException In case it timeouts while checking for the status
   */
  protected void checkApplicationHasStarted() throws DeploymentException {
    log.info("Checking if application: " + deployment.getApplicationName() + " has started");
    CloudHubDeploymentVerification verification = getDeploymentVerification();
    verification.assertDeployment(deployment);
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

  public CloudHubDeploymentVerification getDeploymentVerification() {
    return new CloudHubDeploymentVerification(getClient());
  }
}

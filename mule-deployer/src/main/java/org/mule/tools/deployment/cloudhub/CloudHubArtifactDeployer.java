/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.cloudhub;

import org.mule.tools.client.arm.model.User;
import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.client.cloudhub.model.MuleVersion;
import org.mule.tools.client.cloudhub.model.WorkerType;
import org.mule.tools.client.cloudhub.model.Workers;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.deployment.artifact.ArtifactDeployer;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.verification.DeploymentVerification;
import org.mule.tools.verification.cloudhub.CloudHubDeploymentVerification;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Deploys mule artifacts to CloudHub using the {@link CloudHubClient}.
 */
public class CloudHubArtifactDeployer implements ArtifactDeployer {

  private static final String DEFAULT_CH_REGION = "us-east-1";
  private static final String DEFAULT_CH_WORKER_TYPE = "Micro";
  private static final Integer DEFAULT_CH_WORKERS = 1;
  private static final Long DEFAULT_CLOUDHUB_DEPLOYMENT_TIMEOUT = 600000L;
  public static final String OBJECT_STOREV1 = "objectStoreV1";

  private final DeployerLog log;
  private final CloudHubDeployment deployment;

  private CloudHubClient client;
  private DeploymentVerification deploymentVerification;

  public CloudHubArtifactDeployer(Deployment deployment, DeployerLog log) {
    this(deployment, new CloudHubClient((CloudHubDeployment) deployment, log), log);
  }

  public CloudHubArtifactDeployer(Deployment deployment, CloudHubClient cloudHubClient, DeployerLog log) {
    checkArgument(cloudHubClient != null, "The Cloudhub client must not be null.");

    this.log = log;
    this.client = cloudHubClient;
    this.deploymentVerification = new CloudHubDeploymentVerification(client);

    this.deployment = (CloudHubDeployment) deployment;
    if (!this.deployment.getDeploymentTimeout().isPresent()) {
      this.deployment.setDeploymentTimeout(DEFAULT_CLOUDHUB_DEPLOYMENT_TIMEOUT);
    }
  }

  public void setDeploymentVerification(DeploymentVerification deploymentVerification) {
    checkArgument(deploymentVerification != null, "The verificator must not be null.");
    this.deploymentVerification = deploymentVerification;
  }

  @Override
  public void deployDomain() throws DeploymentException {
    throw new DeploymentException("Deployment of domains to CloudHub is not supported");
  }

  @Override
  public void undeployDomain() throws DeploymentException {
    throw new DeploymentException("Undeployment of domains from CloudHub is not supported");
  }

  /**
   * Deploys an application to CloudHub. It creates the application in CloudHub, uploads its contents and triggers its start.
   *
   * @throws DeploymentException
   */
  @Override
  public void deployApplication() throws DeploymentException {
    createOrUpdateApplication();
    startApplication();
    if (!deployment.getSkipDeploymentVerification()) {
      checkApplicationHasStarted();
    }
  }

  /**
   * Deploys an application to CloudHub, stopping it.
   *
   * @throws DeploymentException If the application does not exist or some internal error in CloudHub happens
   */
  @Override
  public void undeployApplication() throws DeploymentException {
    log.info("Stopping application " + deployment.getApplicationName());
    client.stopApplications(deployment.getApplicationName());
    log.info("Deleting application " + deployment.getApplicationName());
    client.deleteApplications(deployment.getApplicationName());
  }

  /**
   * Retrieves the application name.
   *
   * @return The application name
   */
  public String getApplicationName() {
    return deployment.getApplicationName();
  }

  /**
   * Creates or update an application in CloudHub.
   * <p>
   * If the domain name is available it gets created. Otherwise, it tries to update the existent application.
   *
   * @throws DeploymentException If the application is not available and cannot be updated
   */
  protected void createOrUpdateApplication() throws DeploymentException {
    configureObjectStore();
    if (client.isDomainAvailable(deployment.getApplicationName())) {
      createApplication();
    } else {
      updateApplication();
      try {
        Thread.sleep(deployment.getWaitBeforeValidation());
      } catch (InterruptedException e) {
        log.warn("Could not wait for application start-up validation. Application may still be deploying.");
      }
    }
  }

  /**
   * Creates the application in CloudHub.
   */
  protected void createApplication() {
    log.info("Creating application: " + deployment.getApplicationName());
    User user = client.getMe().user;
    Application application = getApplication(null);
    if (user.isClient) {
      application.setUserId(user.id);
    }
    client.createApplication(application, deployment.getArtifact());
  }

  /**
   * Updates the application in CloudHub.
   *
   * @throws DeploymentException In case the application is not available for the current user or some other internal in CloudHub
   *         happens
   */
  protected void updateApplication() throws DeploymentException {
    Application currentApplication = client.getApplications(deployment.getApplicationName());
    if (currentApplication != null) {
      log.info("Application: " + deployment.getApplicationName() + " already exists, redeploying");
      client.updateApplication(getApplication(currentApplication), deployment.getArtifact());
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
    client.startApplications(deployment.getApplicationName());
  }

  /**
   * Checks if an application in CloudHub has the {@code STARTED_STATUS} status.
   *
   * @throws DeploymentException In case it timeouts while checking for the status
   */
  protected void checkApplicationHasStarted() throws DeploymentException {
    log.info("Checking if application: " + deployment.getApplicationName() + " has started");
    deploymentVerification.assertDeployment(deployment);
  }


  private Application getApplication(Application originalApplication) {
    Application application = new Application();
    Integer workersAmount;
    String workerType;
    MuleVersion muleVersion = new MuleVersion();
    deployment.getMuleVersion().ifPresent(muleVersion::setVersion);
    deployment.getJavaVersion().ifPresent(muleVersion::setJavaVersion);
    deployment.getReleaseChannel().ifPresent(muleVersion::setReleaseChannel);
    Boolean isLoggingCustomLog4JEnabled = null;

    if (originalApplication != null) {
      Map<String, String> resolvedProperties = resolveProperties(originalApplication.getProperties(),
                                                                 deployment.getProperties(), deployment.overrideProperties());
      application.setProperties(resolvedProperties);

      if (isBlank(deployment.getRegion())) {
        application.setRegion(originalApplication.getRegion());
      } else {
        application.setRegion(deployment.getRegion());
      }

      if (deployment.getApplyLatestRuntimePatch()
          && originalApplication.getMuleVersion().getVersion().equals(deployment.getMuleVersion().get())) {
        muleVersion.setUpdateId(originalApplication.getMuleVersion().getLatestUpdateId());
      }

      workersAmount = (deployment.getWorkers() == null) ? originalApplication.getWorkers().getAmount() : deployment.getWorkers();
      workerType =
          isBlank(deployment.getWorkerType()) ? originalApplication.getWorkers().getType().getName() : deployment.getWorkerType();
      isLoggingCustomLog4JEnabled =
          Optional.ofNullable(deployment.getDisableCloudHubLogs()).orElse(originalApplication.getLoggingCustomLog4JEnabled());
    } else {
      application.setMonitoringAutoRestart(true);
      application.setProperties(deployment.getProperties());

      String region = isBlank(deployment.getRegion()) ? DEFAULT_CH_REGION : deployment.getRegion();
      application.setRegion(region);

      workersAmount = (deployment.getWorkers() == null) ? DEFAULT_CH_WORKERS : deployment.getWorkers();
      workerType = isBlank(deployment.getWorkerType()) ? DEFAULT_CH_WORKER_TYPE : deployment.getWorkerType();
      isLoggingCustomLog4JEnabled = deployment.getDisableCloudHubLogs();
    }

    application.setDomain(deployment.getApplicationName());
    application.setMuleVersion(muleVersion);

    application.setWorkers(getWorkers(workersAmount, workerType));

    application.setObjectStoreV1(!deployment.getObjectStoreV2());
    application.setPersistentQueues(deployment.getPersistentQueues());
    application.setLoggingCustomLog4JEnabled(Optional.ofNullable(isLoggingCustomLog4JEnabled).orElse(Boolean.FALSE));

    return application;
  }

  protected Map<String, String> resolveProperties(Map<String, String> originalProperties, Map<String, String> properties,
                                                  boolean overrideProperties) {
    if (properties != null) {
      if (!overrideProperties) {
        properties.putAll(originalProperties);
      }
      originalProperties = properties;
    }
    return originalProperties;
  }

  private Workers getWorkers(Integer amount, String type) {
    Workers workers = new Workers();
    workers.setAmount(amount);
    WorkerType workerType = new WorkerType();
    workerType.setName(type);
    workers.setType(workerType);
    return workers;
  }

  private void configureObjectStore() {
    if (deployment.getObjectStoreV2() == null) {
      deployment.setObjectStoreV2(true);
    }
  }

}

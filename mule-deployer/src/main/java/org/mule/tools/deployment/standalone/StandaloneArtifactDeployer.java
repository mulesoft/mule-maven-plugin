/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.standalone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.mule.tools.client.standalone.controller.MuleProcessController;
import org.mule.tools.client.standalone.controller.probing.PollingProber;
import org.mule.tools.client.standalone.controller.probing.Prober;
import org.mule.tools.client.standalone.controller.probing.deployment.DeploymentProbe;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.standalone.exception.MuleControllerException;
import org.mule.tools.model.Deployment;
import org.mule.tools.deployment.artifact.ArtifactDeployer;
import org.mule.tools.model.standalone.StandaloneDeployment;
import org.mule.tools.utils.DeployerLog;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.mule.tools.client.standalone.controller.probing.deployment.DeploymentProbeFactory.createProbe;

/**
 * Deploys controller artifacts to Standalone using the {@link MuleProcessController}.
 */
public class StandaloneArtifactDeployer implements ArtifactDeployer {

  private static final Long DEFAULT_STANDALONE_DEPLOYMENT_TIMEOUT = 60000L;
  private final StandaloneDeployment deployment;
  private final DeployerLog log;

  private static final long DEFAULT_POLLING_DELAY = 1000;

  private MuleProcessController controller;
  private Prober prober;

  public StandaloneArtifactDeployer(Deployment deployment, MuleProcessController controller, DeployerLog log, Prober prober) {
    this.deployment = (StandaloneDeployment) deployment;
    this.controller = controller;
    this.prober = prober;
    this.log = log;
  }

  /**
   * Creates a {@link StandaloneArtifactDeployer} based on the deployment configuration and the log.
   * 
   * @param deployment The deployment configuration
   * @param log A log to output information and errors
   * @throws DeploymentException
   */
  public StandaloneArtifactDeployer(Deployment deployment, DeployerLog log) throws DeploymentException {
    this(deployment, getMuleProcessController(deployment), log, getProber(deployment));
  }

  /**
   * Retrieves a prober that timeouts after the deployment timeout specified in the deployment configuration. If the timeout was
   * not specified, it is as {@code DEFAULT_POLLING_DELAY}.
   * 
   * @param deployment The standalone deployment
   * @return A {@link PollingProber} instance
   */
  private static Prober getProber(Deployment deployment) {
    return new PollingProber(((StandaloneDeployment) deployment).getDeploymentTimeout()
        .orElse(DEFAULT_STANDALONE_DEPLOYMENT_TIMEOUT), DEFAULT_POLLING_DELAY);
  }

  /**
   * Retrieves a mule process controller that is going to work on the mule home defined in the deployment configuration.
   * 
   * @param deployment The deployment configuration with the mule home value
   * @return A {@link MuleProcessController} instance
   */
  private static MuleProcessController getMuleProcessController(Deployment deployment) {
    return new MuleProcessController(((StandaloneDeployment) deployment).getMuleHome().getAbsolutePath());
  }

  /**
   * Waits for the deployment to be done. It does that by creating a prober that verifies if the artifact is running.
   * 
   * @throws DeploymentException If the artifact is not running after the deploymentTimeout time span
   */
  public void waitForDeployments() throws DeploymentException {
    if (!deployment.getArtifact().exists()) {
      throw new DeploymentException("Application does not exist: " + deployment.getArtifact());
    }
    log.info("Waiting for artifact [" + deployment.getArtifact() + "] to be deployed.");
    String app = FilenameUtils.getBaseName(deployment.getArtifact().getName());
    try {
      DeploymentProbe probe = createProbe(deployment.getPackaging());
      prober.check(probe.isDeployed(controller, app));
    } catch (AssertionError e) {
      log.error("Couldn't deploy application [" + app + "] after [" + deployment.getDeploymentTimeout()
          + "] miliseconds. Check Mule Runtime log");
      throw new DeploymentException("Application deployment timeout.");
    }
  }

  /**
   * Renames the file to be deployed have the same name that the application appended by ".jar".
   * 
   * @throws DeploymentException If it fails to rename the application due to an {@link IOException}
   */
  protected void renameApplicationToApplicationName() throws DeploymentException {
    if (!FilenameUtils.getBaseName(deployment.getArtifact().getName()).equals(deployment.getApplicationName())) {
      try {
        File destApplication =
            new File(deployment.getArtifact().getParentFile(), deployment.getApplicationName() + ".jar");
        FileUtils.copyFile(deployment.getArtifact(), destApplication);
        deployment.setArtifact(destApplication);
      } catch (IOException e) {
        throw new DeploymentException("Fail to rename [" + deployment.getArtifact() + "] to ["
            + deployment.getApplicationName()
            + "]");
      }
    }
  }

  /**
   * Checks if there is a mule instance running in the folder defined in the mule controller.
   * 
   * @throws MuleControllerException If there is no mule instance running in the specified directory
   */
  public void verifyMuleIsStarted() throws MuleControllerException {
    log.info("Checking if Mule Runtime is running.");
    if (!controller.isRunning()) {
      throw new MuleControllerException("Mule Runtime is not running! Aborting.");
    }
  }

  /**
   * Deploys the domain specified in the deployment configuration. It is important to remind that this method does not deploy the
   * domain specified as the main artifact of the deployment configuration but just the domain defined as a dependency of a mule
   * application.
   * 
   * @param configuration A deployment configuration
   * @throws DeploymentException If there is no domain defined in the deployment configuration
   */
  public void addDomainFromstandaloneDeployment(StandaloneDeployment configuration) throws DeploymentException {
    if (configuration.getDomain().isPresent()) {
      log.info("Adding domain with configuration: " + configuration.getDomain());
      controller.deployDomain(configuration.getDomain().get().getAbsolutePath());
    } else {
      log.info("Domain configuration not found: " + configuration.getDomain());
    }
  }

  @Override
  public String toString() {
    return String.format("StandaloneDeployer with [Controller=%s, log=%s, application=%s, timeout=%d, pollingDelay=%d ]",
                         controller, log, deployment.getArtifact(), deployment.getDeploymentTimeout(),
                         DEFAULT_POLLING_DELAY);
  }

  /**
   * Deploys a mule domain to Standalone. It first renames the file to be deployed to have the same name as of the project. Then
   * it deploys also any possible domain that is specified as a dependency of the application.
   *
   * @throws DeploymentException
   */
  @Override
  public void deployDomain() throws DeploymentException {
    renameApplicationToApplicationName();
    File domain = deployment.getArtifact();
    checkArgument(domain != null, "Domain cannot be null");
    try {
      controller.deployDomain(domain.getAbsolutePath());
    } catch (MuleControllerException e) {
      log.error("Couldn't deploy domain: " + domain);
      throw new DeploymentException("Couldn't deploy domain: " + domain);
    }
  }

  @Override
  public void undeployDomain() throws DeploymentException {
    throw new DeploymentException("Undeployment of domains to Standalone is not supported");
  }

  /**
   * Deploys a mule application to Standalone. It first renames the file to be deployed to have the same name as of the project.
   * Then it deploys also any possible domain that is specified as a dependency of the application.
   *
   * @throws DeploymentException
   */
  @Override
  public void deployApplication() throws DeploymentException {
    renameApplicationToApplicationName();
    addDomainFromstandaloneDeployment(deployment);
    File application = deployment.getArtifact();
    checkState(application != null, "Application cannot be null");
    try {
      controller.deploy(application.getAbsolutePath());
    } catch (MuleControllerException e) {
      log.error("Couldn't deploy application: " + application);
      throw new DeploymentException("Couldn't deploy application: " + application);
    }
  }

  /**
   * Undeploys a mule application from Standalone.
   *
   * @throws DeploymentException If the mule home defined in the deployment configuration does not exist
   */
  @Override
  public void undeployApplication() throws DeploymentException {
    File muleHome = deployment.getMuleHome();
    if (!muleHome.exists()) {
      throw new DeploymentException("MULE_HOME directory does not exist. Please verify the deployment configuration");
    }
    log.info("Using MULE_HOME: " + muleHome);
    undeploy(muleHome);
  }

  /**
   * Undeploys a mule application a standalone instance running in the specified {@param muleHome}.
   * 
   * @param muleHome The mule home of the mule instance
   * @throws DeploymentException In case the artifact can not be found or some {@link IOException} occurs
   */
  protected void undeploy(File muleHome) throws DeploymentException {
    File appsDir = new File(muleHome + "/apps/");

    for (File file : appsDir.listFiles()) {
      if (FilenameUtils.getBaseName(file.getName()).equals(deployment.getApplicationName())) {
        try {
          log.info("Deleting " + file);
          FileUtils.forceDelete(file);
          return;
        } catch (IOException e) {
          log.error("Could not delete " + file.getAbsolutePath());
          throw new DeploymentException("Could not delete directory [" + file.getAbsolutePath() + "]", e);
        }
      }
    }
    throw new DeploymentException("Application " + deployment.getApplicationName() + " not found.");
  }
}

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.deployment;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.project.MavenProject;
import org.mule.tools.client.standalone.configuration.ClusterConfigurator;
import org.mule.tools.client.standalone.controller.MuleProcessController;
import org.mule.tools.client.standalone.controller.probing.PollingProber;
import org.mule.tools.client.standalone.controller.probing.deployment.DeploymentProbe;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.standalone.exception.MuleControllerException;
import org.mule.tools.model.standalone.ClusterDeployment;
import org.mule.tools.utils.DeployerLog;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.mule.tools.client.standalone.controller.probing.deployment.DeploymentProbeFactory.createProbe;

public class ClusterDeployer {/* extends Deployer { */

  private static final double MAX_CLUSTER_SIZE = 8;
  private static final long DEFAULT_POLLING_DELAY = 1000;
  private static final Long DEFAULT_CLUSTER_DEPLOYMENT_TIMEOUT = 60000L;

  private final DeployerLog log;

  private File[] paths;
  private List<MuleProcessController> mules;
  private ClusterConfigurator configurator = new ClusterConfigurator();

  private final ClusterDeployment clusterDeployment;

  public ClusterDeployer(ClusterDeployment clusterDeployment, DeployerLog log) throws DeploymentException {
    this.log = log;
    this.clusterDeployment = clusterDeployment;
  }

  public String toString() {
    return String.format("StandaloneDeployer with [Controllers=%s, log=%s, application=%s, timeout=%d, pollingDelay=%d ]",
                         mules, log, clusterDeployment.getArtifact(),
                         clusterDeployment.getDeploymentTimeout().orElse(DEFAULT_CLUSTER_DEPLOYMENT_TIMEOUT),
                         DEFAULT_POLLING_DELAY);
  }

  private void waitForDeployments() throws DeploymentException {
    for (MuleProcessController m : mules) {
      if (!clusterDeployment.getArtifact().exists()) {
        throw new DeploymentException("Application does not exists: " + clusterDeployment.getArtifact());
      }
      DeploymentProbe probe = createProbe(clusterDeployment.getPackaging());
      log.debug("Checking for application [" + clusterDeployment.getArtifact() + "] to be deployed.");
      String app = getApplicationName(clusterDeployment.getArtifact());
      try {
        new PollingProber(clusterDeployment.getDeploymentTimeout().orElse(DEFAULT_CLUSTER_DEPLOYMENT_TIMEOUT),
                          DEFAULT_POLLING_DELAY)
                              .check(probe.isDeployed(m, app));
      } catch (AssertionError e) {
        log.error("Couldn't deploy application [" + clusterDeployment.getArtifact() + "] after ["
            + clusterDeployment.getDeploymentTimeout()
            + "] miliseconds. Check Mule Runtime log");
        throw new DeploymentException("Application deployment timeout.");
      }

    }
  }

  private String getApplicationName(File application) {
    String name = application.getName();
    int extensionBeginning = name.lastIndexOf('.');
    return extensionBeginning == -1 ? name : name.substring(0, extensionBeginning);
  }

  private void deployApplications() throws DeploymentException {
    for (MuleProcessController m : mules) {
      if (!clusterDeployment.getArtifact().exists()) {
        throw new DeploymentException("Application does not exists: "
            + clusterDeployment.getArtifact().getAbsolutePath());
      }
      log.info("Deploying application [" + clusterDeployment.getArtifact() + "]");
      try {
        m.deploy(clusterDeployment.getArtifact().getAbsolutePath());
      } catch (MuleControllerException e) {
        log.error("Couldn't deploy application: " + clusterDeployment.getArtifact() + ". Check Mule Runtime logs");
      }
    }
  }

  private void startMulesIfStopped() {
    for (MuleProcessController m : mules) {
      log.debug("Checking if Mule Runtime is running.");
      if (!m.isRunning()) {
        try {
          log.info("Starting Mule Runtime");
          if (clusterDeployment.getArguments() == null) {
            m.start();
          } else {
            m.start(clusterDeployment.getArguments());
          }
        } catch (MuleControllerException e) {
          log.error("Couldn't start Mule Runtime. Check Mule Runtime logs");
        }
      }
    }
  }

  public void deploy() throws DeploymentException {
    try {
      configurator.configureCluster(paths, mules);
      startMulesIfStopped();
      deployApplications();
      waitForDeployments();
    } catch (MuleControllerException e) {
      throw new DeploymentException("Error deploying application: [" + clusterDeployment.getArtifact() + "]");
    } catch (RuntimeException e) {
      throw new DeploymentException("Unexpected error deploying application: [" + clusterDeployment.getArtifact()
          + "]", e);
    }
  }

  public void undeploy(MavenProject mavenProject) throws DeploymentException {
    File[] muleHomes = new File[clusterDeployment.getSize()];
    for (int i = 0; i < clusterDeployment.getSize(); i++) {
      File parentDir = new File(mavenProject.getBuild().getDirectory(), "mule" + i);
      muleHomes[i] = new File(parentDir, "mule-enterprise-standalone-" + clusterDeployment.getMuleVersion());

      if (!muleHomes[i].exists()) {
        throw new DeploymentException(muleHomes[i].getAbsolutePath() + "directory does not exist.");
      }
    }
    new StandaloneUndeployer(log, clusterDeployment.getApplicationName(), muleHomes).execute();
  }

  public void initialize() throws DeploymentException {
    validateSize();
    renameApplicationToApplicationName();
  }

  private void validateSize() throws DeploymentException {
    if (clusterDeployment.getSize() > MAX_CLUSTER_SIZE) {
      throw new DeploymentException("Cannot create cluster with more than 8 nodes");
    }
  }

  private void renameApplicationToApplicationName() throws DeploymentException {
    if (!FilenameUtils.getBaseName(clusterDeployment.getArtifact().getName())
        .equals(clusterDeployment.getApplicationName())) {
      try {
        File destApplication =
            new File(clusterDeployment.getArtifact().getParentFile(),
                     clusterDeployment.getApplicationName() + ".jar");
        FileUtils.copyFile(clusterDeployment.getArtifact(), destApplication);
        clusterDeployment.setArtifact(destApplication);
      } catch (IOException e) {
        throw new DeploymentException("Couldn't rename [" + clusterDeployment.getArtifact() + "] to ["
            + clusterDeployment.getApplicationName()
            + "]");
      }
    }
  }
}

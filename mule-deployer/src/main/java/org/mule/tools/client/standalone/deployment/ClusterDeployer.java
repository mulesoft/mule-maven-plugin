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



import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import groovy.util.ScriptException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.mule.tools.client.AbstractDeployer;
import org.mule.tools.client.standalone.configuration.ClusterConfigurator;
import org.mule.tools.client.standalone.controller.MuleProcessController;
import org.mule.tools.client.standalone.controller.probing.AppDeploymentProbe;
import org.mule.tools.client.standalone.controller.probing.PollingProber;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.client.standalone.exception.MuleControllerException;
import org.mule.tools.client.standalone.installer.MuleStandaloneInstaller;
import org.mule.tools.model.standalone.ClusterDeployment;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.utils.GroovyUtils;

public class ClusterDeployer extends AbstractDeployer {

  public static final double MAX_CLUSTER_SIZE = 8;
  public static final long DEFAULT_POLLING_DELAY = 1000;
  private final ClusterDeployment clusterDeployment;
  private List<MuleProcessController> mules;
  private File[] paths;
  private ClusterConfigurator configurator = new ClusterConfigurator();

  public ClusterDeployer(ClusterDeployment clusterDeployment, DeployerLog log) throws DeploymentException {
    super(clusterDeployment, log);
    this.clusterDeployment = clusterDeployment;
  }

  public String toString() {
    return String.format("StandaloneDeployer with [Controllers=%s, log=%s, application=%s, timeout=%d, pollingDelay=%d ]",
                         mules, log, clusterDeployment.getApplication(), clusterDeployment.getDeploymentTimeout(),
                         DEFAULT_POLLING_DELAY);
  }

  private void waitForDeployments() throws DeploymentException {
    for (MuleProcessController m : mules) {
      if (!clusterDeployment.getApplication().exists()) {
        throw new DeploymentException("Application does not exists: " + clusterDeployment.getApplication());
      }
      log.debug("Checking for application [" + clusterDeployment.getApplication() + "] to be deployed.");
      String app = getApplicationName(clusterDeployment.getApplication());
      try {
        new PollingProber(clusterDeployment.getDeploymentTimeout(), DEFAULT_POLLING_DELAY)
            .check(AppDeploymentProbe.isDeployed(m, app));
      } catch (AssertionError e) {
        log.error("Couldn't deploy application [" + clusterDeployment.getApplication() + "] after ["
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
      if (!clusterDeployment.getApplication().exists()) {
        throw new DeploymentException("Application does not exists: "
            + clusterDeployment.getApplication().getAbsolutePath());
      }
      log.info("Deploying application [" + clusterDeployment.getApplication() + "]");
      try {
        m.deploy(clusterDeployment.getApplication().getAbsolutePath());
      } catch (MuleControllerException e) {
        log.error("Couldn't deploy application: " + clusterDeployment.getApplication() + ". Check Mule Runtime logs");
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

  @Override
  public void deploy() throws DeploymentException {
    try {
      configurator.configureCluster(paths, mules);
      startMulesIfStopped();
      deployApplications();
      waitForDeployments();
    } catch (MuleControllerException e) {
      throw new DeploymentException("Error deploying application: [" + clusterDeployment.getApplication() + "]");
    } catch (RuntimeException e) {
      throw new DeploymentException("Unexpected error deploying application: [" + clusterDeployment.getApplication()
          + "]", e);
    }
  }

  @Override
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

  @Override
  protected void initialize() throws DeploymentException {
    validateSize();
    renameApplicationToApplicationName();
  }

  @Override
  public void resolveDependencies(MavenProject mavenProject, ArtifactResolver artifactResolver, ArchiverManager archiverManager,
                                  ArtifactFactory artifactFactory, ArtifactRepository localRepository)
      throws DeploymentException, ScriptException {
    paths = new File[clusterDeployment.getSize()];
    List<MuleProcessController> controllers = new LinkedList();
    for (int i = 0; i < clusterDeployment.getSize(); i++) {
      File buildDirectory = new File(mavenProject.getBuild().getDirectory(), "mule" + i);
      buildDirectory.mkdir();
      File home = null;
      try {
        new MuleStandaloneInstaller(clusterDeployment, mavenProject, artifactResolver,
                                    archiverManager, artifactFactory, localRepository, log).doInstallMule(buildDirectory);
      } catch (org.eclipse.aether.deployment.DeploymentException e) {
        throw new DeploymentException("Could not install mule standalone. Aborting.", e);
      }
      controllers.add(new MuleProcessController(home.getAbsolutePath(), clusterDeployment.getTimeout()));
      paths[i] = home;
    }


    if (null != clusterDeployment.getScript()) {
      GroovyUtils.executeScript(mavenProject, clusterDeployment.getScript());
    }
  }

  private void validateSize() throws DeploymentException {
    if (clusterDeployment.getSize() > MAX_CLUSTER_SIZE) {
      throw new DeploymentException("Cannot create cluster with more than 8 nodes");
    }
  }

  private void renameApplicationToApplicationName() throws DeploymentException {
    if (!FilenameUtils.getBaseName(clusterDeployment.getApplication().getName())
        .equals(clusterDeployment.getApplicationName())) {
      try {
        File destApplication =
            new File(clusterDeployment.getApplication().getParentFile(),
                     clusterDeployment.getApplicationName() + ".jar");
        FileUtils.copyFile(clusterDeployment.getApplication(), destApplication);
        clusterDeployment.setApplication(destApplication);
      } catch (IOException e) {
        throw new DeploymentException("Couldn't rename [" + clusterDeployment.getApplication() + "] to ["
            + clusterDeployment.getApplicationName()
            + "]");
      }
    }
  }
}

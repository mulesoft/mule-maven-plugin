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
import org.mule.tools.model.DeployerLog;
import org.mule.tools.model.DeploymentConfiguration;
import org.mule.tools.utils.GroovyUtils;

public class ClusterDeployer extends AbstractDeployer {

  public static final double MAX_CLUSTER_SIZE = 8;
  public static final long DEFAULT_POLLING_DELAY = 1000;
  private List<MuleProcessController> mules;
  private File[] paths;
  private ClusterConfigurator configurator = new ClusterConfigurator();

  public ClusterDeployer(DeploymentConfiguration deploymentConfiguration, DeployerLog log) throws DeploymentException {
    super(deploymentConfiguration, log);
  }

  public String toString() {
    return String.format("StandaloneDeployer with [Controllers=%s, log=%s, application=%s, timeout=%d, pollingDelay=%d ]",
                         mules, log, deploymentConfiguration.getApplication(), deploymentConfiguration.getDeploymentTimeout(),
                         DEFAULT_POLLING_DELAY);
  }

  private void waitForDeployments() throws DeploymentException {
    for (MuleProcessController m : mules) {
      if (!deploymentConfiguration.getApplication().exists()) {
        throw new DeploymentException("Application does not exists: " + deploymentConfiguration.getApplication());
      }
      log.debug("Checking for application [" + deploymentConfiguration.getApplication() + "] to be deployed.");
      String app = getApplicationName(deploymentConfiguration.getApplication());
      try {
        new PollingProber(deploymentConfiguration.getDeploymentTimeout(), DEFAULT_POLLING_DELAY)
            .check(AppDeploymentProbe.isDeployed(m, app));
      } catch (AssertionError e) {
        log.error("Couldn't deploy application [" + deploymentConfiguration.getApplication() + "] after ["
            + deploymentConfiguration.getDeploymentTimeout()
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
      if (!deploymentConfiguration.getApplication().exists()) {
        throw new DeploymentException("Application does not exists: "
            + deploymentConfiguration.getApplication().getAbsolutePath());
      }
      log.info("Deploying application [" + deploymentConfiguration.getApplication() + "]");
      try {
        m.deploy(deploymentConfiguration.getApplication().getAbsolutePath());
      } catch (MuleControllerException e) {
        log.error("Couldn't deploy application: " + deploymentConfiguration.getApplication() + ". Check Mule Runtime logs");
      }
    }
  }

  private void startMulesIfStopped() {
    for (MuleProcessController m : mules) {
      log.debug("Checking if Mule Runtime is running.");
      if (!m.isRunning()) {
        try {
          log.info("Starting Mule Runtime");
          if (deploymentConfiguration.getArguments() == null) {
            m.start();
          } else {
            m.start(deploymentConfiguration.getArguments());
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
      throw new DeploymentException("Error deploying application: [" + deploymentConfiguration.getApplication() + "]");
    } catch (RuntimeException e) {
      throw new DeploymentException("Unexpected error deploying application: [" + deploymentConfiguration.getApplication()
          + "]", e);
    }
  }

  @Override
  public void undeploy(MavenProject mavenProject) throws DeploymentException {
    File[] muleHomes = new File[deploymentConfiguration.getSize()];
    for (int i = 0; i < deploymentConfiguration.getSize(); i++) {
      File parentDir = new File(mavenProject.getBuild().getDirectory(), "mule" + i);
      muleHomes[i] = new File(parentDir, "mule-enterprise-standalone-" + deploymentConfiguration.getMuleVersion());

      if (!muleHomes[i].exists()) {
        throw new DeploymentException(muleHomes[i].getAbsolutePath() + "directory does not exist.");
      }
    }
    new StandaloneUndeployer(log, deploymentConfiguration.getApplicationName(), muleHomes).execute();
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
    paths = new File[deploymentConfiguration.getSize()];
    List<MuleProcessController> controllers = new LinkedList();
    for (int i = 0; i < deploymentConfiguration.getSize(); i++) {
      File buildDirectory = new File(mavenProject.getBuild().getDirectory(), "mule" + i);
      buildDirectory.mkdir();
      File home = null;
      try {
        new MuleStandaloneInstaller(deploymentConfiguration, mavenProject, artifactResolver,
                                    archiverManager, artifactFactory, localRepository, log).doInstallMule(buildDirectory);
      } catch (org.eclipse.aether.deployment.DeploymentException e) {
        throw new DeploymentException("Could not install mule standalone. Aborting.", e);
      }
      controllers.add(new MuleProcessController(home.getAbsolutePath(), deploymentConfiguration.getTimeout()));
      paths[i] = home;
    }


    if (null != deploymentConfiguration.getScript()) {
      GroovyUtils.executeScript(mavenProject, deploymentConfiguration);
    }
  }

  private void validateSize() throws DeploymentException {
    if (deploymentConfiguration.getSize() > MAX_CLUSTER_SIZE) {
      throw new DeploymentException("Cannot create cluster with more than 8 nodes");
    }
  }

  private void renameApplicationToApplicationName() throws DeploymentException {
    if (!FilenameUtils.getBaseName(deploymentConfiguration.getApplication().getName())
        .equals(deploymentConfiguration.getApplicationName())) {
      try {
        File destApplication =
            new File(deploymentConfiguration.getApplication().getParentFile(),
                     deploymentConfiguration.getApplicationName() + ".jar");
        FileUtils.copyFile(deploymentConfiguration.getApplication(), destApplication);
        deploymentConfiguration.setApplication(destApplication);
      } catch (IOException e) {
        throw new DeploymentException("Couldn't rename [" + deploymentConfiguration.getApplication() + "] to ["
            + deploymentConfiguration.getApplicationName()
            + "]");
      }
    }
  }
}

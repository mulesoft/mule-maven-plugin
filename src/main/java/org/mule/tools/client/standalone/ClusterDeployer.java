/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone;

import org.mule.tck.probe.PollingProber;
import org.mule.tck.probe.Prober;
import org.mule.test.infrastructure.process.AppDeploymentProbe;
import org.mule.test.infrastructure.process.MuleControllerException;
import org.mule.test.infrastructure.process.MuleProcessController;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

public class ClusterDeployer {

  private List<MuleProcessController> mules;
  private File[] paths;
  private Log log;
  private File application;
  private Prober prober;
  private long timeout;
  private long pollingDelay;
  private String[] arguments;
  private ClusterConfigurator configurator = new ClusterConfigurator();

  public ClusterDeployer(File[] paths,
                         List<MuleProcessController> mules,
                         Log log,
                         File application,
                         long timeout,
                         String[] arguments,
                         long pollingDelay) {
    this.mules = mules;
    this.log = log;
    this.application = application;
    this.timeout = timeout;
    this.pollingDelay = pollingDelay;
    this.arguments = arguments;
    this.prober = new PollingProber(timeout, pollingDelay);
    this.paths = paths;
    log.debug(toString());
  }

  public String toString() {
    return String.format("Deployer with [Controllers=%s, log=%s, application=%s, timeout=%d, pollingDelay=%d ]",
                         mules, log, application, timeout, pollingDelay);
  }

  public void execute() throws MojoFailureException, MojoExecutionException {
    try {
      configurator.configureCluster(paths, mules);
      startMulesIfStopped();
      deployApplications();
      waitForDeployments();
    } catch (MuleControllerException e) {
      throw new MojoFailureException("Error deploying application: [" + application + "]");
    } catch (RuntimeException e) {
      throw new MojoExecutionException("Unexpected error deploying application: [" + application
          + "]", e);
    }
  }

  private void waitForDeployments() throws MojoFailureException {
    for (MuleProcessController m : mules) {
      if (!application.exists()) {
        throw new MojoFailureException("Application does not exists: " + application);
      }
      log.debug("Checking for application [" + application + "] to be deployed.");
      String app = getApplicationName(application);
      try {
        prober.check(AppDeploymentProbe.isDeployed(m, app));
      } catch (AssertionError e) {
        log.error("Couldn't deploy application [" + application + "] after [" + timeout
            + "] miliseconds. Check Mule Runtime log");
        throw new MojoFailureException("Application deployment timeout.");
      }

    }
  }

  private String getApplicationName(File application) {
    String name = application.getName();
    int extensionBeginning = name.lastIndexOf('.');
    return extensionBeginning == -1 ? name : name.substring(0, extensionBeginning);
  }

  private void deployApplications() throws MojoFailureException {
    for (MuleProcessController m : mules) {
      if (!application.exists()) {
        throw new MojoFailureException("Application does not exists: " + application.getAbsolutePath());
      }
      log.info("Deploying application [" + application + "]");
      try {
        m.deploy(application.getAbsolutePath());
      } catch (MuleControllerException e) {
        log.error("Couldn't deploy application: " + application + ". Check Mule Runtime logs");
      }
    }
  }

  private void startMulesIfStopped() {
    for (MuleProcessController m : mules) {
      log.debug("Checking if Mule Runtime is running.");
      if (!m.isRunning()) {
        try {
          log.info("Starting Mule Runtime");
          if (arguments == null) {
            m.start();
          } else {
            m.start(arguments);
          }
        } catch (MuleControllerException e) {
          log.error("Couldn't start Mule Runtime. Check Mule Runtime logs");
        }
      }
    }

  }

  public ClusterDeployer addLibraries(List<File> libs) {
    for (File file : libs) {
      for (MuleProcessController c : mules) {
        c.addLibrary(file);
      }

      log.debug(String.format("Adding library %s...", file));
    }
    return this;
  }


}

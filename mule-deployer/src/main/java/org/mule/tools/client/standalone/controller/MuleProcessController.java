/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller;

import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

import java.io.File;

public class MuleProcessController {

  public static final String MULE_PROCESS_CONTROLLER_TIMEOUT_PROPERTY = "mule.process.controller.timeout";
  public static final int DEFAULT_TIMEOUT = 60000;
  private final int controllerTimeout = Integer.getInteger(MULE_PROCESS_CONTROLLER_TIMEOUT_PROPERTY, DEFAULT_TIMEOUT);

  private final Controller controller;

  public MuleProcessController(String muleHome) {
    AbstractOSController osSpecificController =
        IS_OS_WINDOWS ? new WindowsController(muleHome, controllerTimeout) : new UnixController(muleHome, controllerTimeout);
    controller = buildController(muleHome, osSpecificController);
  }

  protected Controller buildController(String muleHome, AbstractOSController osSpecificController) {
    return new Controller(osSpecificController, muleHome);
  }

  public boolean isRunning() {
    return getController().isRunning();
  }

  public void start(String... args) {
    getController().start(args);
  }

  public void stop(String... args) {
    getController().stop(args);
  }

  public int status(String... args) {
    return getController().status(args);
  }

  public int getProcessId() {
    return getController().getProcessId();
  }

  public void restart(String... args) {
    getController().restart(args);
  }

  public void deploy(String path) {
    getController().deploy(path);
  }

  public boolean isDeployed(String appName) {
    return getController().isDeployed(appName);
  }

  public File getArtifactInternalRepository(String artifactName) {
    return getController().getArtifactInternalRepository(artifactName);
  }

  public File getRuntimeInternalRepository() {
    return getController().getRuntimeInternalRepository();
  }

  public boolean isDomainDeployed(String domainName) {
    return getController().isDomainDeployed(domainName);
  }

  public void undeploy(String application) {
    getController().undeploy(application);
  }

  public void undeployDomain(String domain) {
    getController().undeployDomain(domain);
  }

  public void undeployAll() {
    getController().undeployAll();
  }

  public void installLicense(String path) {
    getController().installLicense(path);
  }

  public void uninstallLicense() {
    getController().uninstallLicense();
  }

  public void addLibrary(File jar) {
    getController().addLibrary(jar);
  }

  public void deployDomain(String domain) {
    getController().deployDomain(domain);
  }

  public File getLog() {
    return getController().getLog();
  }

  public File getLog(String appName) {
    return getController().getLog(appName);
  }

  public void addConfProperty(String value) {
    getController().addConfProperty(value);
  }

  protected Controller getController() {
    return controller;
  }

  protected int getControllerTimeout() {
    return controllerTimeout;
  }
}

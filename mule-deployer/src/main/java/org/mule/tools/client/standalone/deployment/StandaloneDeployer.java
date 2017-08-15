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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.mule.tools.client.standalone.controller.probing.AppDeploymentProbe;
import org.mule.tools.client.standalone.controller.probing.PollingProber;
import org.mule.tools.client.standalone.controller.probing.Prober;
import org.mule.tools.client.standalone.controller.MuleProcessController;
import org.mule.tools.client.standalone.exception.MuleControllerException;
import org.mule.tools.model.ArtifactDescription;
import org.mule.tools.model.DeployerLog;
import org.mule.tools.model.DeploymentConfiguration;

public class StandaloneDeployer {

  private MuleProcessController mule;
  private DeployerLog log;
  private File application;
  private Prober prober;
  private Long timeout;
  private Long pollingDelay;
  private String[] arguments;

  public StandaloneDeployer(MuleProcessController mule,
                            DeployerLog log,
                            File application,
                            Long timeout,
                            String[] arguments,
                            Long pollingDelay) {
    this.mule = mule;
    this.log = log;
    this.application = application;
    this.timeout = timeout;
    this.pollingDelay = pollingDelay;
    this.arguments = arguments;
    this.prober = new PollingProber(timeout, pollingDelay);
    log.debug(toString());
  }

  public String toString() {
    return String.format("StandaloneDeployer with [Controller=%s, log=%s, application=%s, timeout=%d, pollingDelay=%d ]",
                         mule, log, application, timeout, pollingDelay);
  }

  public void execute() throws MojoFailureException, MojoExecutionException {
    try {
      verifyMuleIsStarted();
      deployApplications();
      waitForDeployments();
    } catch (MuleControllerException e) {
      throw new MojoFailureException("Error deploying application: [" + application + "]: " + e.getMessage());
    } catch (RuntimeException e) {
      throw new MojoExecutionException("Unexpected error deploying application: [" + application
          + "]", e);
    }
  }

  private void verifyMuleIsStarted() throws MuleControllerException {
    log.debug("Checking if Mule Runtime is running.");
    if (!mule.isRunning()) {
      throw new MuleControllerException("Mule Runtime is not running! Aborting.");
    }
  }

  private void waitForDeployments() throws MojoFailureException {
    if (!application.exists()) {
      throw new MojoFailureException("Application does not exists: " + application);
    }
    log.debug("Checking for application [" + application + "] to be deployed.");
    String app = getApplicationName(application);
    try {
      prober.check(AppDeploymentProbe.isDeployed(mule, app));
    } catch (AssertionError e) {
      log.error("Couldn't deploy application [" + app + "] after [" + timeout
          + "] miliseconds. Check Mule Runtime log");
      throw new MojoFailureException("Application deployment timeout.");
    }
  }

  private String getApplicationName(File application) {
    String name = application.getName();
    int extensionBeginning = name.lastIndexOf('.');
    return extensionBeginning == -1 ? name : name.substring(0, extensionBeginning);
  }

  private void deployApplications() throws MojoFailureException {
    log.info("Deploying application [" + application + "]");
    try {
      mule.deploy(application.getAbsolutePath());
    } catch (MuleControllerException e) {
      log.error("Couldn't deploy application: " + application + ". Check Mule Runtime logs");
    }
  }

  private void startMuleIfStopped() {
    log.debug("Checking if Mule Runtime is running.");
    if (!mule.isRunning()) {
      try {
        log.info("Starting Mule Runtime");
        if (arguments == null) {
          mule.start();
        } else {
          mule.start(arguments);
        }
      } catch (MuleControllerException e) {
        log.error("Couldn't start Mule Runtime. Check Mule Runtime logs.");
      }
    }
  }

  public StandaloneDeployer addLibraries(List<File> libs) {
    for (File file : libs) {
      mule.addLibrary(file);
      log.debug(String.format("Adding library %s...", file));
    }
    return this;
  }


  public StandaloneDeployer addDomain(File domain) throws MojoFailureException {
    try {
      log.debug(String.format("Deploying domain : %s", domain));
      mule.deployDomain(domain.getAbsolutePath());
      return this;
    } catch (MuleControllerException e) {
      log.error("Couldn't deploy domain: " + domain);
      throw new MojoFailureException("Couldn't deploy domain: " + domain);
    }
  }

  public void addDependencies(DeploymentConfiguration configuration, ArtifactFactory factory, ArtifactResolver resolver,
                              MavenProject project, ArtifactRepository repository)
      throws MojoFailureException, MojoExecutionException {
    List<File> libraries = new ArrayList<>();
    for (ArtifactDescription artifact : configuration.getArtifactItems()) {
      libraries.add(getDependency(artifact, factory, resolver, project, repository));
    }
    addLibraries(libraries);
  }

  protected File getDependency(ArtifactDescription artifactDescription, ArtifactFactory factory, ArtifactResolver resolver,
                               MavenProject project, ArtifactRepository repository)
      throws MojoExecutionException, MojoFailureException {
    try {
      Artifact artifact = factory.createArtifact(artifactDescription.getGroupId(),
                                                 artifactDescription.getArtifactId(), artifactDescription.getVersion(),
                                                 null,
                                                 artifactDescription.getType());
      log.info("Resolving " + artifact);
      resolver.resolve(artifact, project.getRemoteArtifactRepositories(), repository);
      return artifact.getFile();
    } catch (AbstractArtifactResolutionException e) {
      throw new MojoExecutionException("Couldn't download artifact: " + e.getMessage(), e);
    } catch (Exception e) {
      throw new MojoFailureException("Couldn't download artifact: " + e.getMessage());
    }
  }

  public void addDomainFromDeploymentConfiguration(DeploymentConfiguration configuration) throws MojoFailureException {
    if (configuration.getDomain() != null && configuration.getDomain().exists()) {
      log.debug("Adding domain with configuration: " + configuration.getDomain());
      addDomain(configuration.getDomain());
    } else {
      log.debug("Domain configuration not found: " + configuration.getDomain());
    }
  }
}

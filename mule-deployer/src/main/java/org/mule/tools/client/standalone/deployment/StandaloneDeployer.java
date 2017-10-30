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
import java.util.ArrayList;
import java.util.List;

import groovy.util.ScriptException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.client.AbstractDeployer;
import org.mule.tools.client.standalone.controller.probing.AppDeploymentProbe;
import org.mule.tools.client.standalone.controller.probing.PollingProber;
import org.mule.tools.client.standalone.controller.probing.Prober;
import org.mule.tools.client.standalone.controller.MuleProcessController;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.client.standalone.exception.MuleControllerException;
import org.mule.tools.model.standalone.StandaloneDeployment;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.utils.GroovyUtils;

import static com.google.common.base.Preconditions.checkNotNull;

public class StandaloneDeployer extends AbstractDeployer {

  private static final long DEFAULT_POLLING_DELAY = 1000;
  private final StandaloneDeployment standaloneDeployment;

  private MuleProcessController mule;
  private Prober prober;

  public StandaloneDeployer(StandaloneDeployment standaloneDeployment, DeployerLog log) throws DeploymentException {
    super(standaloneDeployment, log);
    this.standaloneDeployment = standaloneDeployment;
  }

  public String toString() {
    return String.format("StandaloneDeployer with [Controller=%s, log=%s, application=%s, timeout=%d, pollingDelay=%d ]",
                         mule, log, standaloneDeployment.getApplication(), standaloneDeployment.getDeploymentTimeout(),
                         DEFAULT_POLLING_DELAY);
  }

  private void verifyMuleIsStarted() throws MuleControllerException {
    log.debug("Checking if Mule Runtime is running.");
    if (!mule.isRunning()) {
      throw new MuleControllerException("Mule Runtime is not running! Aborting.");
    }
  }

  private void waitForDeployments() throws DeploymentException {
    if (!standaloneDeployment.getApplication().exists()) {
      throw new DeploymentException("Application does not exists: " + standaloneDeployment.getApplication());
    }
    log.debug("Waiting for application [" + standaloneDeployment.getApplication() + "] to be deployed.");
    String app = FilenameUtils.getBaseName(standaloneDeployment.getApplication().getName());
    try {
      prober.check(AppDeploymentProbe.isDeployed(mule, app));
    } catch (AssertionError e) {
      log.error("Couldn't deploy application [" + app + "] after [" + standaloneDeployment.getDeploymentTimeout()
          + "] miliseconds. Check Mule Runtime log");
      throw new DeploymentException("Application deployment timeout.");
    }
  }

  private void deployApplications() throws DeploymentException {
    log.info("Deploying application [" + standaloneDeployment.getApplication() + "]");
    try {
      mule.deploy(standaloneDeployment.getApplication().getAbsolutePath());
    } catch (MuleControllerException e) {
      throw new DeploymentException("Couldn't deploy application: " + standaloneDeployment.getApplication()
          + ". Check Mule Runtime logs");
    }
  }

  private void startMuleIfStopped() {
    log.debug("Checking if Mule Runtime is running.");
    if (!mule.isRunning()) {
      try {
        log.info("Starting Mule Runtime");
        if (standaloneDeployment.getArguments() == null) {
          mule.start();
        } else {
          mule.start(standaloneDeployment.getArguments());
        }
      } catch (MuleControllerException e) {
        log.error("Couldn't start Mule Runtime. Check Mule Runtime logs.");
      }
    }
  }

  public StandaloneDeployer addLibraries(List<File> libs) {
    checkNotNull(libs, "Libraries cannot be null");
    for (File file : libs) {
      mule.addLibrary(file);
      log.debug(String.format("Adding library %s...", file));
    }
    return this;
  }


  public StandaloneDeployer addDomain(File domain) throws DeploymentException {
    checkNotNull(domain, "Domain cannot be null");
    try {
      log.debug(String.format("Deploying domain : %s", domain));
      mule.deployDomain(domain.getAbsolutePath());
      return this;
    } catch (MuleControllerException e) {
      log.error("Couldn't deploy domain: " + domain);
      throw new DeploymentException("Couldn't deploy domain: " + domain);
    }
  }

  public void addDomainFromstandaloneDeployment(StandaloneDeployment configuration) throws DeploymentException {
    if (configuration.getDomain() != null && configuration.getDomain().exists()) {
      log.debug("Adding domain with configuration: " + configuration.getDomain());
      addDomain(configuration.getDomain());
    } else {
      log.debug("Domain configuration not found: " + configuration.getDomain());
    }
  }

  @Override
  public void deploy() throws DeploymentException {
    try {
      verifyMuleIsStarted();
      deployApplications();
      waitForDeployments();
    } catch (MuleControllerException e) {
      throw new DeploymentException("Error deploying application: [" + standaloneDeployment.getApplication() + "]: "
          + e.getMessage());
    } catch (RuntimeException e) {
      throw new DeploymentException("Unexpected error deploying application: [" + standaloneDeployment.getApplication()
          + "]", e);
    }
  }

  @Override
  public void undeploy(MavenProject mavenProject) throws DeploymentException {
    if (!standaloneDeployment.getMuleHome().exists()) {
      throw new DeploymentException("MULE_HOME directory does not exist.");
    }
    log.info("Using MULE_HOME: " + standaloneDeployment.getMuleHome());
    new StandaloneUndeployer(log, standaloneDeployment.getApplicationName(), standaloneDeployment.getMuleHome())
        .execute();
  }

  @Override
  protected void initialize() throws DeploymentException {
    this.mule =
        new MuleProcessController(standaloneDeployment.getMuleHome().getAbsolutePath(), standaloneDeployment.getTimeout());

    renameApplicationToApplicationName();

    this.prober = new PollingProber(standaloneDeployment.getDeploymentTimeout(), DEFAULT_POLLING_DELAY);
    addDomainFromstandaloneDeployment(standaloneDeployment);

  }

  @Override
  public void resolveDependencies(MavenProject mavenProject, ArtifactResolver artifactResolver,
                                  ArchiverManager archiverManager, ArtifactFactory artifactFactory,
                                  ArtifactRepository localRepository)
      throws DeploymentException, ScriptException {
    List<File> libs = getDependencies(standaloneDeployment, artifactFactory, artifactResolver, mavenProject, localRepository);
    addLibraries(libs);
    if (null != standaloneDeployment.getScript()) {
      GroovyUtils.executeScript(mavenProject, standaloneDeployment.getScript());
    }
  }



  private void renameApplicationToApplicationName() throws DeploymentException {
    if (!FilenameUtils.getBaseName(standaloneDeployment.getApplication().getName())
        .equals(standaloneDeployment.getApplicationName())) {
      try {
        File destApplication =
            new File(standaloneDeployment.getApplication().getParentFile(),
                     standaloneDeployment.getApplicationName() + ".jar");
        FileUtils.copyFile(standaloneDeployment.getApplication(), destApplication);
        standaloneDeployment.setApplication(destApplication);
      } catch (IOException e) {
        throw new DeploymentException("Fail to rename [" + standaloneDeployment.getApplication() + "] to ["
            + standaloneDeployment.getApplicationName()
            + "]");
      }
    }
  }

  public List<File> getDependencies(StandaloneDeployment standaloneDeployment, ArtifactFactory factory, ArtifactResolver resolver,
                                    MavenProject project, ArtifactRepository repository)
      throws DeploymentException {
    List<File> libraries = new ArrayList<>();
    for (ArtifactCoordinates artifact : standaloneDeployment.getArtifactItems()) {
      libraries.add(getDependency(artifact, factory, resolver, project, repository));
    }
    return libraries;
  }

  protected File getDependency(ArtifactCoordinates artifactDescription, ArtifactFactory factory, ArtifactResolver resolver,
                               MavenProject project, ArtifactRepository repository)
      throws DeploymentException {
    try {
      Artifact artifact = factory.createArtifact(artifactDescription.getGroupId(),
                                                 artifactDescription.getArtifactId(), artifactDescription.getVersion(),
                                                 null,
                                                 artifactDescription.getType());
      log.info("Resolving " + artifact);
      resolver.resolve(artifact, project.getRemoteArtifactRepositories(), repository);
      return artifact.getFile();
    } catch (AbstractArtifactResolutionException e) {
      throw new DeploymentException("Couldn't download artifact: " + e.getMessage(), e);
    } catch (Exception e) {
      throw new DeploymentException("Couldn't download artifact: " + e.getMessage());
    }
  }
}

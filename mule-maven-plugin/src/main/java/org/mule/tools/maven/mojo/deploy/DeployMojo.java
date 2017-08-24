/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo.deploy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import groovy.util.ScriptException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.mule.tools.client.AbstractDeployer;
import org.mule.tools.client.standalone.controller.MuleProcessController;
import org.mule.tools.client.standalone.deployment.ClusterDeployer;
import org.mule.tools.client.standalone.deployment.StandaloneDeployer;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.client.agent.AgentDeployer;
import org.mule.tools.client.arm.ArmDeployer;
import org.mule.tools.client.cloudhub.CloudhubDeployer;
import org.mule.tools.client.standalone.installer.MuleStandaloneInstaller;
import org.mule.tools.model.ArtifactDescription;
import org.mule.tools.model.DeploymentConfiguration;
import org.mule.tools.utils.GroovyUtils;

/**
 * Maven plugin to deploy Mule applications to different kind of servers: Standalone (both Community and Enterprise), Clustered,
 * Anypoint Runtime Manager and CloudHub. Main uses are running integration tests and deploying applications. Some of the features
 * are: Download Mule Standalone from a Maven Repository and install it locally. Deploy a Mule application to a server. Undeploy a
 * Mule appliction. Assemble a Mule cluster and deploy applications.
 *
 * @author <a href="mailto:asequeira@gmail.com">Ale Sequeira</a>
 * @see UndeployMojo
 * @see MuleProcessController
 * @since 1.0
 */
@Mojo(name = "deploy", requiresProject = true)
public class DeployMojo extends AbstractMuleDeployerMojo {

  private static final long DEFAULT_POLLING_DELAY = 1000;
  private static final Integer MAX_CLUSTER_SIZE = 8;
  private MuleStandaloneInstaller muleStandaloneInstaller;

  @Component
  protected ArchiverManager archiverManager;

  @Override
  protected void cloudhub() throws MojoFailureException, MojoExecutionException, ScriptException {
    CloudhubDeployer deployer =
        new CloudhubDeployer(deploymentConfiguration.getUri(), deploymentConfiguration.getUsername(),
                             deploymentConfiguration.getPassword(),
                             deploymentConfiguration.getEnvironment(), deploymentConfiguration.getApplicationName(),
                             deploymentConfiguration.getApplication(),
                             deploymentConfiguration.getRegion(), deploymentConfiguration.getMuleVersion(),
                             deploymentConfiguration.getWorkers(),
                             deploymentConfiguration.getWorkerType(), getLog(), deploymentConfiguration.getProperties(),
                             deploymentConfiguration.getBusinessGroup());
    deployWithDeployer(deployer);
  }

  @Override
  protected void arm() throws MojoFailureException, MojoExecutionException, ScriptException {
    ArmDeployer deployer =
        new ArmDeployer(deploymentConfiguration.getUri(), deploymentConfiguration.getUsername(),
                        deploymentConfiguration.getPassword(),
                        deploymentConfiguration.getEnvironment(), deploymentConfiguration.getTargetType(),
                        deploymentConfiguration.getTarget(), deploymentConfiguration.getApplication(),
                        deploymentConfiguration.getApplicationName(),
                        getLog(), deploymentConfiguration.getBusinessGroup(), deploymentConfiguration.isArmInsecure());
    deployWithDeployer(deployer);
  }

  @Override
  protected void agent() throws MojoFailureException, MojoExecutionException, ScriptException {
    AgentDeployer deployer = new AgentDeployer(getLog(), deploymentConfiguration.getApplicationName(),
                                               deploymentConfiguration.getApplication(), deploymentConfiguration.getUri());
    deployWithDeployer(deployer);
  }

  private void deployWithDeployer(AbstractDeployer deployer)
      throws MojoExecutionException, MojoFailureException, ScriptException {
    if (null != deploymentConfiguration.getScript()) {
      GroovyUtils.executeScript(mavenProject, deploymentConfiguration);
    }
    try {
      deployer.deploy();
    } catch (DeploymentException e) {
      getLog().error("Failed to deploy " + deploymentConfiguration.getApplicationName() + ": " + e.getMessage(), e);
      throw new MojoFailureException("Failed to deploy [" + deploymentConfiguration.getApplication() + "]");
    }
  }

  @Override
  protected void cluster() throws MojoExecutionException, MojoFailureException, ScriptException {

    validateSize();
    File[] muleHomes = new File[deploymentConfiguration.getSize()];
    List<MuleProcessController> controllers = new LinkedList();
    for (int i = 0; i < deploymentConfiguration.getSize(); i++) {
      File buildDirectory = new File(mavenProject.getBuild().getDirectory(), "mule" + i);
      buildDirectory.mkdir();
      File home = getMuleStandaloneInstaller().doInstallMule(buildDirectory);
      controllers.add(new MuleProcessController(home.getAbsolutePath(), deploymentConfiguration.getTimeout()));
      muleHomes[i] = home;
    }

    renameApplicationToApplicationName();

    if (null != deploymentConfiguration.getScript()) {
      GroovyUtils.executeScript(mavenProject, deploymentConfiguration);
    }
    new ClusterDeployer(muleHomes, controllers, getLog(), deploymentConfiguration.getApplication(),
                        deploymentConfiguration.getDeploymentTimeout(),
                        deploymentConfiguration.getArguments(), DEFAULT_POLLING_DELAY)
                            .addLibraries(deploymentConfiguration.getLibs()).execute();
  }

  private void validateSize() throws MojoFailureException {
    if (deploymentConfiguration.getSize() > MAX_CLUSTER_SIZE) {
      throw new MojoFailureException("Cannot create cluster with more than 8 nodes");
    }
  }

  @Override
  protected void standalone() throws DeploymentException, ScriptException, MojoFailureException {
    // File muleHome = getMuleStandaloneInstaller().installMule(new File(mavenProject.getBuild().getDirectory()));
    File muleHome = deploymentConfiguration.getMuleHome();
    MuleProcessController mule =
        new MuleProcessController(muleHome.getAbsolutePath(), deploymentConfiguration.getTimeout());

    renameApplicationToApplicationName();

    StandaloneDeployer standaloneDeployer = new StandaloneDeployer(mule, log, deploymentConfiguration.getApplication(),
                                                                   deploymentConfiguration.getDeploymentTimeout(),
                                                                   deploymentConfiguration.getArguments(),
                                                                   DEFAULT_POLLING_DELAY)
                                                                       .addLibraries(deploymentConfiguration.getLibs());
    standaloneDeployer.addDomainFromDeploymentConfiguration(deploymentConfiguration);
    List<File> libs = getDependencies(deploymentConfiguration, artifactFactory, artifactResolver, mavenProject, localRepository);
    standaloneDeployer.addLibraries(libs);
    if (null != deploymentConfiguration.getScript()) {
      GroovyUtils.executeScript(mavenProject, deploymentConfiguration);
    }
    standaloneDeployer.execute();
  }

  public List<File> getDependencies(DeploymentConfiguration configuration, ArtifactFactory factory, ArtifactResolver resolver,
                                    MavenProject project, ArtifactRepository repository)
      throws DeploymentException {
    List<File> libraries = new ArrayList<>();
    for (ArtifactDescription artifact : configuration.getArtifactItems()) {
      libraries.add(getDependency(artifact, factory, resolver, project, repository));
    }
    return libraries;
  }

  protected File getDependency(ArtifactDescription artifactDescription, ArtifactFactory factory, ArtifactResolver resolver,
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

  private void renameApplicationToApplicationName() throws MojoFailureException {
    if (!FilenameUtils.getBaseName(deploymentConfiguration.getApplication().getName())
        .equals(deploymentConfiguration.getApplicationName())) {
      try {
        File destApplication =
            new File(deploymentConfiguration.getApplication().getParentFile(),
                     deploymentConfiguration.getApplicationName() + ".jar");
        FileUtils.copyFile(deploymentConfiguration.getApplication(), destApplication);
        deploymentConfiguration.setApplication(destApplication);
      } catch (IOException e) {
        throw new MojoFailureException("Couldn't rename [" + deploymentConfiguration.getApplication() + "] to ["
            + deploymentConfiguration.getApplicationName()
            + "]");
      }
    }
  }

  public MuleStandaloneInstaller getMuleStandaloneInstaller() {
    if (muleStandaloneInstaller == null) {
      muleStandaloneInstaller = new MuleStandaloneInstaller(deploymentConfiguration, mavenProject, artifactResolver,
                                                            archiverManager, artifactFactory, localRepository, getLog());
    }
    return muleStandaloneInstaller;
  }
}

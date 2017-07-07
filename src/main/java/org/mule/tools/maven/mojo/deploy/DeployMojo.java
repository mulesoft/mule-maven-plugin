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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.mule.test.infrastructure.process.MuleProcessController;
import org.mule.tools.client.AbstractDeployer;
import org.mule.tools.client.standalone.ClusterDeployer;
import org.mule.tools.client.standalone.Deployer;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.client.agent.AgentDeployer;
import org.mule.tools.client.arm.ArmDeployer;
import org.mule.tools.client.cloudhub.CloudhubDeployer;
import org.mule.tools.maven.mojo.model.ArtifactDescription;
import org.mule.util.FilenameUtils;

/**
 * Maven plugin to deploy Mule applications to different kind of servers: Standalone (both Community and Enterprise), Clustered,
 * Anypoint Runtime Manager and CloudHub. Main uses are running integration tests and deploying applications. Some of the features
 * are: Download Mule Standalone from a Maven Repository and install it locally. Deploy a Mule application to a server. Undeploy a
 * Mule appliction. Assemble a Mule cluster and deploy applications.
 *
 * @author <a href="mailto:asequeira@gmail.com">Ale Sequeira</a>
 * @see UndeployMojo
 * @see org.mule.test.infrastructure.process.MuleProcessController
 * @since 1.0
 */
@Mojo(name = "deploy", requiresProject = true)
public class DeployMojo extends AbstractMuleDeployerMojo {

  private static final long DEFAULT_POLLING_DELAY = 1000;
  private static final Integer MAX_CLUSTER_SIZE = 8;

  @Component
  protected ArchiverManager archiverManager;


  public void doExecute() throws MojoExecutionException, MojoFailureException {
    initializeApplication();
    initializeEnvironment();
    switch (deploymentConfiguration.getDeploymentType()) {
      case standalone:
        standalone();
        break;
      case cluster:
        cluster();
        break;
      case arm:
        arm();
        break;
      case cloudhub:
        cloudhub();
        break;
      case agent:
        agent();
        break;
      default:
        throw new MojoFailureException("Unsupported deploymentConfiguration type: "
            + deploymentConfiguration.getDeploymentType());
    }
  }

  private void cloudhub() throws MojoFailureException, MojoExecutionException {
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

  private void arm() throws MojoFailureException, MojoExecutionException {
    ArmDeployer deployer =
        new ArmDeployer(deploymentConfiguration.getUri(), deploymentConfiguration.getUsername(),
                        deploymentConfiguration.getPassword(),
                        deploymentConfiguration.getEnvironment(), deploymentConfiguration.getTargetType(),
                        deploymentConfiguration.getTarget(), deploymentConfiguration.getApplication(),
                        deploymentConfiguration.getApplicationName(),
                        getLog(), deploymentConfiguration.getBusinessGroup(), deploymentConfiguration.isArmInsecure());
    deployWithDeployer(deployer);
  }

  private void agent() throws MojoFailureException, MojoExecutionException {
    AgentDeployer deployer = new AgentDeployer(getLog(), deploymentConfiguration.getApplicationName(),
                                               deploymentConfiguration.getApplication(), deploymentConfiguration.getUri());
    deployWithDeployer(deployer);
  }

  private void deployWithDeployer(AbstractDeployer deployer) throws MojoExecutionException, MojoFailureException {
    if (null != deploymentConfiguration.getScript()) {
      executeGroovyScript();
    }
    try {
      deployer.deploy();
    } catch (DeploymentException e) {
      getLog().error("Failed to deploy " + deploymentConfiguration.getApplicationName() + ": " + e.getMessage(), e);
      throw new MojoFailureException("Failed to deploy [" + deploymentConfiguration.getApplication() + "]");
    }
  }

  private void cluster() throws MojoExecutionException, MojoFailureException {
    validateSize();
    File[] muleHomes = new File[deploymentConfiguration.getSize()];
    List<MuleProcessController> controllers = new LinkedList();
    for (int i = 0; i < deploymentConfiguration.getSize(); i++) {
      File buildDirectory = new File(mavenProject.getBuild().getDirectory(), "mule" + i);
      buildDirectory.mkdir();
      File home = doInstallMule(buildDirectory);
      controllers.add(new MuleProcessController(home.getAbsolutePath(), deploymentConfiguration.getTimeout()));
      muleHomes[i] = home;
    }

    renameApplicationToApplicationName();

    if (null != deploymentConfiguration.getScript()) {
      executeGroovyScript();
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

  public void standalone() throws MojoExecutionException, MojoFailureException {
    File muleHome = installMule(new File(mavenProject.getBuild().getDirectory()));
    MuleProcessController mule = new MuleProcessController(muleHome.getAbsolutePath(), deploymentConfiguration.getTimeout());

    renameApplicationToApplicationName();

    Deployer deployer = new Deployer(mule, getLog(), deploymentConfiguration.getApplication(),
                                     deploymentConfiguration.getDeploymentTimeout(), deploymentConfiguration.getArguments(),
                                     DEFAULT_POLLING_DELAY)
                                         .addLibraries(deploymentConfiguration.getLibs());
    addDomain(deployer);
    addDependencies(deployer);
    if (null != deploymentConfiguration.getScript()) {
      executeGroovyScript();
    }
    deployer.execute();
  }

  private void renameApplicationToApplicationName() throws MojoFailureException {
    if (!FilenameUtils.getBaseName(deploymentConfiguration.getApplication().getName())
        .equals(deploymentConfiguration.getApplicationName())) {
      try {
        File destApplication =
            new File(deploymentConfiguration.getApplication().getParentFile(),
                     deploymentConfiguration.getApplicationName() + ".zip");
        FileUtils.copyFile(deploymentConfiguration.getApplication(), destApplication);
        deploymentConfiguration.setApplication(destApplication);
      } catch (IOException e) {
        throw new MojoFailureException("Couldn't rename [" + deploymentConfiguration.getApplication() + "] to ["
            + deploymentConfiguration.getApplicationName()
            + "]");
      }
    }
  }

  private File installMule(File buildDirectory) throws MojoExecutionException, MojoFailureException {
    if (deploymentConfiguration.getMuleHome() == null) {
      deploymentConfiguration.setMuleHome(doInstallMule(buildDirectory));
    }
    mavenProject.getProperties().setProperty("mule.home", deploymentConfiguration.getMuleHome().getAbsolutePath());
    getLog().info("Using MULE_HOME: " + deploymentConfiguration.getMuleHome());
    return deploymentConfiguration.getMuleHome();
  }

  private File doInstallMule(File buildDirectory) throws MojoExecutionException, MojoFailureException {
    if (deploymentConfiguration.getMuleDistribution() == null) {
      if (deploymentConfiguration.isCommunity()) {
        deploymentConfiguration.setMuleDistribution(new ArtifactDescription("org.mule.distributions", "mule-standalone",
                                                                            deploymentConfiguration.getMuleVersion(), "tar.gz"));
        this.getLog()
            .debug("muleDistribution not set, using default community artifact: "
                + deploymentConfiguration.getMuleDistribution());
      } else {
        deploymentConfiguration
            .setMuleDistribution(new ArtifactDescription("com.mulesoft.muleesb.distributions", "mule-ee-distribution-standalone",
                                                         deploymentConfiguration.getMuleVersion(), "tar.gz"));
        this.getLog().debug("muleDistribution not set, using default artifact: " + deploymentConfiguration.getMuleDistribution());
      }
    }
    unpackMule(deploymentConfiguration.getMuleDistribution(), buildDirectory);
    return new File(buildDirectory, deploymentConfiguration.getMuleDistribution().getContentDirectory());
  }

  /**
   * This code was inspired by maven-dependency-plugin GetMojo.
   */
  private void unpackMule(ArtifactDescription muleDistribution, File destDir)
      throws MojoExecutionException, MojoFailureException {
    File src = getDependency(muleDistribution);
    getLog().info("Copying " + src.getAbsolutePath() + " to " + destDir.getAbsolutePath());
    extract(src, destDir, muleDistribution.getType());
  }

  private void extract(File src, File dest, String type)
      throws MojoExecutionException, MojoFailureException {
    try {
      UnArchiver unArchiver = getArchiver(type);
      unArchiver.setSourceFile(src);
      unArchiver.setDestDirectory(dest);
      unArchiver.extract();
    } catch (ArchiverException e) {
      throw new MojoExecutionException("Couldn't extract file " + src + " to " + dest);
    } catch (Exception e) {
      throw new MojoFailureException("Couldn't extract file " + src + " to " + dest);
    }
  }

  private UnArchiver getArchiver(String type) throws MojoExecutionException {
    UnArchiver unArchiver;
    try {
      unArchiver = archiverManager.getUnArchiver(type);
      getLog().debug("Found unArchiver by extension: " + unArchiver);
      return unArchiver;
    } catch (NoSuchArchiverException e) {
      throw new MojoExecutionException("Couldn't find archiver for type: " + type);
    }
  }


}

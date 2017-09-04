/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.installer;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.eclipse.aether.deployment.DeploymentException;
import org.mule.tools.model.ArtifactDescription;
import org.mule.tools.model.DeployerLog;
import org.mule.tools.model.DeploymentConfiguration;

import java.io.File;

public class MuleStandaloneInstaller {

  private DeploymentConfiguration deploymentConfiguration;
  private MavenProject mavenProject;
  private ArtifactResolver artifactResolver;
  private ArchiverManager archiverManager;
  private ArtifactFactory artifactFactory;
  private DeployerLog log;
  private ArtifactRepository localRepository;

  public MuleStandaloneInstaller(DeploymentConfiguration deploymentConfiguration, MavenProject mavenProject,
                                 ArtifactResolver artifactResolver, ArchiverManager archiverManager,
                                 ArtifactFactory artifactFactory, ArtifactRepository localRepository,
                                 DeployerLog log) {
    this.deploymentConfiguration = deploymentConfiguration;
    this.mavenProject = mavenProject;
    this.artifactResolver = artifactResolver;
    this.archiverManager = archiverManager;
    this.artifactFactory = artifactFactory;
    this.localRepository = localRepository;
    this.log = log;
  }

  public File installMule(File buildDirectory) throws DeploymentException {
    if (deploymentConfiguration.getMuleHome() == null) {
      deploymentConfiguration.setMuleHome(doInstallMule(buildDirectory));
    }
    mavenProject.getProperties().setProperty("mule.home", deploymentConfiguration.getMuleHome().getAbsolutePath());
    log.info("Using MULE_HOME: " + deploymentConfiguration.getMuleHome());
    return deploymentConfiguration.getMuleHome();
  }

  public File doInstallMule(File buildDirectory) throws DeploymentException {
    if (deploymentConfiguration.getMuleDistribution() == null) {
      if (deploymentConfiguration.isCommunity()) {
        deploymentConfiguration.setMuleDistribution(new ArtifactDescription("org.mule.distributions", "mule-standalone",
                                                                            deploymentConfiguration.getMuleVersion(), "tar.gz"));
        log.debug("muleDistribution not set, using default community artifact: "
            + deploymentConfiguration.getMuleDistribution());
      } else {
        deploymentConfiguration
            .setMuleDistribution(new ArtifactDescription("com.mulesoft.mule.distributions", "mule-ee-distribution-standalone",
                                                         deploymentConfiguration.getMuleVersion(), "tar.gz"));
        log.debug("muleDistribution not set, using default artifact: " + deploymentConfiguration.getMuleDistribution());
      }
    }
    unpackMule(deploymentConfiguration.getMuleDistribution(), buildDirectory);
    String contentDirectory = resolveMuleContentDirectory(deploymentConfiguration.getMuleDistribution());
    return new File(buildDirectory, contentDirectory);
  }

  private String resolveMuleContentDirectory(ArtifactDescription muleDistribution) {
    return "mule-" + ("mule-standalone".equals(muleDistribution.getArtifactId()) ? "" : "enterprise-") + "standalone-"
        + muleDistribution.getVersion();
  }

  /**
   * This code was inspired by maven-dependency-plugin GetMojo.
   */
  public void unpackMule(ArtifactDescription muleDistribution, File destDir) throws DeploymentException {
    File src = getDependency(muleDistribution);
    log.info("Copying " + src.getAbsolutePath() + " to " + destDir.getAbsolutePath());
    extract(src, destDir, muleDistribution.getType());
  }

  private void extract(File src, File dest, String type)
      throws DeploymentException {
    try {
      UnArchiver unArchiver = getArchiver(type);
      unArchiver.setSourceFile(src);
      unArchiver.setDestDirectory(dest);
      unArchiver.extract();
    } catch (ArchiverException e) {
      throw new DeploymentException("Couldn't extract file " + src + " to " + dest);
    }
  }

  private UnArchiver getArchiver(String type) throws DeploymentException {
    UnArchiver unArchiver;
    try {
      unArchiver = archiverManager.getUnArchiver(type);
      log.debug("Found unArchiver by extension: " + unArchiver);
      return unArchiver;
    } catch (NoSuchArchiverException e) {
      throw new DeploymentException("Couldn't find archiver for type: " + type);
    }
  }

  protected File getDependency(ArtifactDescription artifactDescription) throws DeploymentException {
    try {
      Artifact artifact = artifactFactory.createArtifact(artifactDescription.getGroupId(),
                                                         artifactDescription.getArtifactId(), artifactDescription.getVersion(),
                                                         null,
                                                         artifactDescription.getType());
      log.info("Resolving " + artifact);
      artifactResolver.resolve(artifact, mavenProject.getRemoteArtifactRepositories(), localRepository);
      return artifact.getFile();
    } catch (AbstractArtifactResolutionException e) {
      throw new DeploymentException("Fail download artifact: " + artifactDescription.toString(), e);
    }
  }
}

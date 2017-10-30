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

import org.apache.commons.lang3.StringUtils;
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
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.model.standalone.ClusterDeployment;
import org.mule.tools.utils.DeployerLog;

import java.io.File;

public class MuleStandaloneInstaller {

  private ClusterDeployment clusterDeployment;
  private MavenProject mavenProject;
  private ArtifactResolver artifactResolver;
  private ArchiverManager archiverManager;
  private ArtifactFactory artifactFactory;
  private DeployerLog log;
  private ArtifactRepository localRepository;

  public MuleStandaloneInstaller(ClusterDeployment clusterDeployment, MavenProject mavenProject,
                                 ArtifactResolver artifactResolver, ArchiverManager archiverManager,
                                 ArtifactFactory artifactFactory, ArtifactRepository localRepository,
                                 DeployerLog log) {
    this.clusterDeployment = clusterDeployment;
    this.mavenProject = mavenProject;
    this.artifactResolver = artifactResolver;
    this.archiverManager = archiverManager;
    this.artifactFactory = artifactFactory;
    this.localRepository = localRepository;
    this.log = log;
  }

  public File installMule(File buildDirectory) throws DeploymentException {
    if (clusterDeployment.getMuleHome() == null) {
      clusterDeployment.setMuleHome(doInstallMule(buildDirectory));
    }
    mavenProject.getProperties().setProperty("mule.home", clusterDeployment.getMuleHome().getAbsolutePath());
    log.info("Using MULE_HOME: " + clusterDeployment.getMuleHome());
    return clusterDeployment.getMuleHome();
  }

  public File doInstallMule(File buildDirectory) throws DeploymentException {
    if (clusterDeployment.getMuleDistribution() == null) {
      clusterDeployment.setMuleDistribution(new ArtifactCoordinates("org.mule.distributions", "mule-standalone",
                                                                    clusterDeployment.getMuleVersion().get(), "tar.gz",
                                                                    StringUtils.EMPTY));
      log.debug("muleDistribution not set, using default community artifact: "
          + clusterDeployment.getMuleDistribution());

    }
    unpackMule(clusterDeployment.getMuleDistribution(), buildDirectory);
    String contentDirectory = resolveMuleContentDirectory(clusterDeployment.getMuleDistribution());
    return new File(buildDirectory, contentDirectory);
  }

  private String resolveMuleContentDirectory(ArtifactCoordinates muleDistribution) {
    return "mule-" + ("mule-standalone".equals(muleDistribution.getArtifactId()) ? "" : "enterprise-") + "standalone-"
        + muleDistribution.getVersion();
  }

  /**
   * This code was inspired by maven-dependency-plugin GetMojo.
   */
  public void unpackMule(ArtifactCoordinates muleDistribution, File destDir) throws DeploymentException {
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

  protected File getDependency(ArtifactCoordinates artifactDescription) throws DeploymentException {
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

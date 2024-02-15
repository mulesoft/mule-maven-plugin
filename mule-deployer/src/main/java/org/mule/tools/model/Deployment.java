/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model;

import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.mule.tools.client.core.exception.DeploymentException;

import java.io.File;
import java.util.Optional;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class Deployment {

  @Parameter
  protected String artifact;

  @Parameter
  protected String applicationName;

  @Parameter
  protected String skip;

  @Parameter
  protected String muleVersion;

  @Parameter
  protected String javaVersion;

  @Parameter
  protected String releaseChannel;

  /**
   * The allowed elapsed time between the start of the deployment process and the confirmation that the artifact has been
   * deployed. In the case of the deployment to standalone, it is defined as the elapsed time between the instant when the
   * deployable is copied to the runtime and the creation of the respective anchor file.
   *
   */
  @Parameter
  protected Long deploymentTimeout;

  private String packaging;

  private String artifactId;
  private String groupId;
  private String version;

  /**
   * Application file to be deployed.
   *
   */
  public File getArtifact() {
    return Optional.ofNullable(this.artifact).map(File::new).orElse(null);
  }

  public void setArtifact(File artifact) {
    this.artifact = Optional.ofNullable(artifact).map(File::getPath).orElse(null);
  }

  public void setArtifact(String artifactPath) {
    this.artifact = artifactPath;
  }

  /**
   * Name of the application to deploy/undeploy. If not specified, the artifact id will be used as the name. This parameter allows
   * to override this behavior to specify a custom name.
   *
   * @since 2.0
   */
  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public String getSkip() {
    return skip;
  }

  public void setSkip(String skip) {
    this.skip = skip;
  }

  public Optional<String> getMuleVersion() {
    return Optional.ofNullable(this.muleVersion);
  }

  public void setMuleVersion(String muleVersion) {
    this.muleVersion = muleVersion;
  }

  /**
   * Packaging type of artifact to be deployed.
   *
   */
  public String getPackaging() {
    return packaging;
  }

  public void setPackaging(String packaging) {
    this.packaging = packaging;
  }

  /**
   * DeploymentConfiguration timeout in milliseconds.
   *
   * @since 1.0
   */
  public Optional<Long> getDeploymentTimeout() {
    return Optional.ofNullable(deploymentTimeout);
  }

  public void setDeploymentTimeout(Long deploymentTimeout) {
    this.deploymentTimeout = deploymentTimeout;
  }

  public void setDefaultValues(MavenProject project) throws DeploymentException {
    setBasicDeploymentValues(project);
    setEnvironmentSpecificValues();
  }

  public abstract void setEnvironmentSpecificValues() throws DeploymentException;

  protected void setBasicDeploymentValues(MavenProject project) throws DeploymentException {

    String muleApplicationName = getProperty("mule.application.name");
    if (isNotBlank(muleApplicationName)) {
      setApplicationName(muleApplicationName);
    }
    if (isBlank(getApplicationName())) {
      setApplicationName(project.getArtifactId());
    }

    String isSkip = getProperty("mule.skip");
    if (isNotBlank(isSkip)) {
      setSkip(isSkip);
    }
    if (isBlank(getSkip())) {
      setSkip("false");
    }

    String muleVersion = getProperty("mule.version");
    if (isNotBlank(muleVersion)) {
      setMuleVersion(muleVersion);
    }

    String applicationPath = getProperty("mule.artifact");
    if (isNotBlank(applicationPath)) {
      setArtifact(new File(applicationPath));
    }
    if (getArtifact() == null) {
      if (project.getArtifact() == null) {
        throw new DeploymentException("Artifact to be deployed could not be found. Please set its location setting -Dmule.artifact=path/to/jar or in the deployment configuration pom element");
      }
      setArtifact(project.getArtifact().getFile());
    }

    String packaging = project.getPackaging();
    if (isNotBlank(packaging)) {
      setPackaging(packaging);
    }

    String deploymentTimeout = getProperty("mule.deploymentConfiguration.timeout");
    if (isNotBlank(deploymentTimeout)) {
      setDeploymentTimeout(Long.valueOf(deploymentTimeout));
    }

    setArtifactId(project.getArtifactId());
    setGroupId(project.getGroupId());
    setVersion(project.getVersion());
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getVersion() {
    return version;
  }

  public Optional<String> getJavaVersion() {
    return Optional.ofNullable(javaVersion);
  }

  public void setJavaVersion(String javaVersion) {
    this.javaVersion = javaVersion;
  }

  public Optional<String> getReleaseChannel() {
    return Optional.ofNullable(releaseChannel);
  }

  public void setReleaseChannel(String releaseChannel) {
    this.releaseChannel = releaseChannel;
  }

  public boolean validateVersion() {
    return false;
  }
}

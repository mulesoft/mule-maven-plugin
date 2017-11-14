/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.model;

import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.mule.tools.client.standalone.exception.DeploymentException;

import java.io.File;
import java.util.Optional;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class Deployment {

  @Parameter
  protected File artifact;

  @Parameter
  protected String applicationName;

  @Parameter
  protected String skip;

  @Parameter
  protected String muleVersion;

  /**
   * Application file to be deployed.
   *
   */
  public File getArtifact() {
    return artifact;
  }

  public void setArtifact(File artifact) {
    this.artifact = artifact;
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
      if (project.getAttachedArtifacts().isEmpty()) {
        throw new DeploymentException("Artifact to be deployed could not be found. Please set its location setting -Dmule.artifact=path/to/jar or in the deployment configuration pom element");
      }
      setArtifact(project.getAttachedArtifacts().get(0).getFile());
    }
  }
}

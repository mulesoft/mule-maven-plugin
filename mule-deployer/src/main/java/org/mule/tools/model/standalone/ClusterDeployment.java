/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.model.standalone;

import org.apache.maven.plugins.annotations.Parameter;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.model.Deployment;

import java.io.File;
import java.util.Optional;

public class ClusterDeployment implements MuleRuntimeDeployment {

  @Parameter
  protected Integer timeout;

  // TODO validate what for?
  @Parameter
  protected File script;

  @Parameter(readonly = true)
  protected ArtifactCoordinates muleDistribution;

  @Parameter
  protected Long deploymentTimeout;

  @Parameter
  protected String[] arguments;

  @Parameter
  protected Integer size;

  @Parameter
  protected File muleHome;

  // TODO change name to artifact
  @Parameter
  protected File application; // VALIDATIONS REQURIED

  @Parameter
  protected String applicationName;

  // TODO validate what for?
  @Parameter
  protected String skip;

  @Parameter
  protected String muleVersion;

  /**
   * Application file to be deployed.
   *
   * @since 1.0
   */
  public File getApplication() {
    return application;
  }

  public void setApplication(File application) {
    this.application = application;
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

  @Override
  public Optional<String> getMuleVersion() {
    return Optional.ofNullable(this.muleVersion);
  }

  @Override
  public void setMuleVersion(String muleVersion) {
    this.muleVersion = muleVersion;
  }

  public Integer getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public File getScript() {
    return script;
  }

  public void setScript(File script) {
    this.script = script;
  }

  /**
   * Maven coordinates for the Mule Runtime distribution to download. You need to specify:
   * <li>groupId</li>
   * <li>artifactId</li>
   * <li>version</li> This parameter and <code>muleVersion</code> are mutual exclusive
   *
   * @since 1.0
   * @deprecated Use the official maven artifact descriptor, if you need to use Community distribution @see community property
   */
  public ArtifactCoordinates getMuleDistribution() {
    return muleDistribution;
  }

  public void setMuleDistribution(ArtifactCoordinates muleDistribution) {
    this.muleDistribution = muleDistribution;
  }

  /**
   * DeploymentConfiguration timeout in milliseconds.
   *
   * @since 1.0
   */
  public Long getDeploymentTimeout() {
    return deploymentTimeout;
  }

  public void setDeploymentTimeout(Long deploymentTimeout) {
    this.deploymentTimeout = deploymentTimeout;
  }

  /**
   * List of Mule Runtime Standalone command line arguments. Adding a property to this list is the same that adding it to the
   * command line when starting Mule using bin/mule. If you want to add a Mule property don't forget to prepend <code>-M-D</code>.
   * If you want to add a System property for the Wrapper don't forget to prepend <code>-D</code>.
   * <p>
   * Example:
   * <code>&lt;arguments&gt;&lt;argument&gt;-M-Djdbc.url=jdbc:oracle:thin:@myhost:1521:orcl&lt;/argument&gt;&lt;/arguments&gt;</code>
   *
   * @since 1.0
   */
  public String[] getArguments() {
    if (arguments == null) {
      arguments = new String[0];
    }
    return arguments;
  }

  public void setArguments(String[] arguments) {
    this.arguments = arguments;
  }

  /**
   * Number of cluster nodes.
   *
   * @since 1.0
   */
  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  /**
   * Path to a Mule Standalone server.
   */
  public File getMuleHome() {
    return muleHome;
  }

  /**
   * Set path to a Mule Standalone server.
   */
  public void setMuleHome(File muleHome) {
    this.muleHome = muleHome;
  }

}

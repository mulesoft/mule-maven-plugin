/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.model.anypoint;

import org.apache.maven.plugins.annotations.Parameter;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.model.DeploymentConfiguration;

import java.util.Map;
import java.util.Optional;

public class CloudHubDeployment extends DeploymentConfiguration implements AnypointDeployment {

  @Parameter(required = false, property = "anypoint.username")
  protected String username;

  @Parameter(required = false, property = "anypoint.password")
  protected String password;

  @Parameter(required = false, property = "anypoint.environment")
  protected String environment;

  @Parameter(defaultValue = "", property = "anypoint.businessGroup")
  protected String businessGroup = "";

  @Parameter(readonly = true, property = "anypoint.uri", defaultValue = "https://anypoint.mulesoft.com")
  protected String uri;

  @Parameter(property = "cloudhub.workers")
  protected Integer workers = 1;

  @Parameter(defaultValue = "Medium", property = "cloudhub.workerType")
  protected String workerType;

  @Parameter(property = "cloudhub.region", defaultValue = "us-east-1")
  protected String region;

  // TODO validate what for?
  @Parameter(required = false, property = "maven.server")
  protected String server;

  @Parameter(required = false)
  protected Map<String, String> properties;

  /**
   * Anypoint Platform username.
   *
   * @since 2.0
   */
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Anypoint Platform password.
   *
   * @since 2.0
   */
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Anypoint environment name.
   *
   * @since 2.0
   */
  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  /**
   * Business group for deploymentConfiguration, if it is a nested one its format should be first.second.
   *
   * @since 2.1
   */
  public String getBusinessGroup() {
    return businessGroup;
  }

  public void setBusinessGroup(String businessGroup) {
    this.businessGroup = businessGroup;
  }

  /**
   * Anypoint Platform URI, can be configured to use with On Premise platform..
   *
   * @since 2.0
   */
  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  /**
   * Region to deploy the application in Cloudhub.
   *
   * @since 2.0
   */
  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  /**
   * Number of workers for the deploymentConfiguration of the application in Cloudhub.
   *
   * @since 2.0
   */
  public Integer getWorkers() {
    return workers;
  }

  public void setWorkers(Integer workers) {
    this.workers = workers;
  }

  /**
   * Type of workers for the deploymentConfiguration of the application in Cloudhub.
   *
   * @since 2.0
   */
  public String getWorkerType() {
    return workerType;
  }

  public void setWorkerType(String workerType) {
    this.workerType = workerType;
  }

  /**
   * Maven server with Anypoint Platform credentials. This is only needed if you want to use your credentials stored in your Maven
   * settings.xml file. This is NOT your Mule server name.
   *
   * @since 2.2
   */
  public String getServer() {
    return server;
  }

  public void setServer(String server) {
    this.server = server;
  }

  /**
   * CloudHub properties.
   *
   * @since 2.0
   *
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
}

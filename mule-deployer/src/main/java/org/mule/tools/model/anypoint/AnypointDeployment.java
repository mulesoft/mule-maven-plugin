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

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugins.annotations.Parameter;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;

import java.util.Map;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.mule.tools.client.AbstractMuleClient.DEFAULT_BASE_URL;

public abstract class AnypointDeployment extends Deployment {

  public static final String ANYPOINT_BASE_URI = "anypoint.baseUri";

  @Parameter
  protected String username;

  @Parameter
  protected String password;

  @Parameter
  protected String authToken;

  @Parameter
  protected String environment;

  @Parameter
  protected String businessGroup;

  @Parameter
  protected String businessGroupId;

  @Parameter
  protected String uri;

  @Parameter
  protected String server;


  @Parameter
  protected Map<String, String> properties;

  @Parameter
  protected boolean skipDeploymentVerification = false;

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
   * Anypoint Platform Pre-authenticated bearer token.
   *
   * @since 3.3.0
   */
  public String getAuthToken() {
    return this.authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
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
   * Business group Id for deploymentConfiguration.
   *
   * @since 2.1
   */
  public String getBusinessGroupId() {
    return businessGroupId;
  }

  public void setBusinessGroupId(String businessGroupId) {
    this.businessGroupId = businessGroupId;
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
   * Properties map.
   *
   * @return map of properties
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  /**
   * Property to skip deployment verification to Anypoint Platform.
   *
   * @since 2.0
   */
  public Boolean getSkipDeploymentVerification() {
    return skipDeploymentVerification;
  }

  public void setSkipDeploymentVerification(Boolean skipDeploymentVerification) {
    this.skipDeploymentVerification = skipDeploymentVerification;
  }

  public void setEnvironmentSpecificValues() throws DeploymentException {
    // TODO why we use a prop if this are a parameter ?

    String anypointUri = getProperty(ANYPOINT_BASE_URI);
    if (isNotBlank(anypointUri)) {
      setUri(anypointUri);
    }
    if (isBlank(getUri())) {
      setUri(DEFAULT_BASE_URL);
    }

    String businessGroup = getProperty("anypoint.businessGroup");
    if (isNotBlank(businessGroup)) {
      setBusinessGroup(businessGroup);
    }
    if (isBlank(getBusinessGroup())) {
      setBusinessGroup(StringUtils.EMPTY);
    }

    String anypointEnvironment = getProperty("anypoint.environment");
    if (isNotBlank(anypointEnvironment)) {
      setEnvironment(anypointEnvironment);
    }

    String password = getProperty("anypoint.password");
    if (isNotBlank(password)) {
      setPassword(password);
    }

    String mavenServer = getProperty("maven.server");
    if (isNotBlank(mavenServer)) {
      setServer(mavenServer);
    }

    String username = getProperty("anypoint.username");
    if (isNotBlank(username)) {
      setUsername(username);
    }

    String authToken = getProperty("anypoint.authToken");
    if (isNotBlank(authToken)) {
      setAuthToken(authToken);
    }
  }
}

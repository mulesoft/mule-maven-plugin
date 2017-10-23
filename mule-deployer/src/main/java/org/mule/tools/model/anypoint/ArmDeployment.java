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
import org.mule.tools.client.model.TargetType;
import org.mule.tools.model.DeploymentConfiguration;
/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

public class ArmDeployment extends DeploymentConfiguration implements AnypointDeployment {

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

  @Parameter(property = "anypoint.target")
  protected String target;

  @Parameter(property = "anypoint.target.type")
  protected TargetType targetType;

  @Parameter(defaultValue = "Medium", readonly = true, property = "arm.insecure")
  protected boolean armInsecure;

  @Parameter(defaultValue = "true")
  protected boolean failIfNotExists;

  // TODO validate what for?
  @Parameter(required = false, property = "maven.server")
  protected String server;

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
   * Anypoint Platform target name.
   *
   * @since 2.0
   */
  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  /**
   * Anypoint Platform target type: server, serverGroup or cluster.
   *
   * @since 2.0
   */
  public TargetType getTargetType() {
    return targetType;
  }

  public void setTargetType(TargetType targetType) {
    this.targetType = targetType;
  }

  /**
   * Use insecure mode for ARM deploymentConfiguration: do not validate certificates, nor hostname.
   *
   * @since 2.1
   */
  public boolean isArmInsecure() {
    return armInsecure;
  }

  public void setArmInsecure(boolean armInsecure) {
    this.armInsecure = armInsecure;
  }

  /**
   * When set to false, undeployment won't fail if the specified application does not exist.
   *
   * @since 2.2
   */
  public boolean isFailIfNotExists() {
    return failIfNotExists;
  }

  public void setFailIfNotExists(boolean failIfNotExists) {
    this.failIfNotExists = failIfNotExists;
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
}

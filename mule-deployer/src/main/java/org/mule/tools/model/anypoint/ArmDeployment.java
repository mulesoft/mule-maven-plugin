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
import org.mule.tools.model.Deployment;

import java.io.File;
import java.util.Optional;
/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

public class ArmDeployment implements AnypointDeployment {

  @Parameter
  protected String username;

  @Parameter
  protected String password;

  @Parameter
  protected String environment;

  @Parameter
  protected String businessGroup;

  @Parameter
  protected String uri;

  @Parameter
  protected String target;

  @Parameter
  protected TargetType targetType;

  @Parameter
  protected Boolean armInsecure;

  @Parameter
  protected Boolean failIfNotExists;

  // TODO validate what for?
  @Parameter
  protected String server;

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
  public Optional<Boolean> isArmInsecure() {
    return Optional.ofNullable(armInsecure);
  }

  public void setArmInsecure(boolean armInsecure) {
    this.armInsecure = armInsecure;
  }

  /**
   * When set to false, undeployment won't fail if the specified application does not exist.
   *
   * @since 2.2
   */
  public Optional<Boolean> isFailIfNotExists() {
    return Optional.ofNullable(failIfNotExists);
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

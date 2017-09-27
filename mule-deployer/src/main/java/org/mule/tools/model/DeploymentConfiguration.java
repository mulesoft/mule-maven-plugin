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
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.client.model.TargetType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeploymentConfiguration {

  @Parameter(required = false, property = "maven.server")
  protected String server;

  @Parameter(required = false, property = "anypoint.username")
  protected String username;

  @Parameter(required = false, property = "anypoint.password")
  protected String password;

  @Parameter(required = true)
  protected DeploymentType deploymentType;

  @Parameter(readonly = true, property = "anypoint.uri", defaultValue = "https://anypoint.mulesoft.com")
  protected String uri;

  @Parameter(required = false, property = "anypoint.environment")
  protected String environment;

  @Parameter(property = "mule.home")
  protected File muleHome;

  @Parameter(property = "mule.version")
  protected String muleVersion;

  @Parameter(defaultValue = "2", required = true)
  protected Integer size;

  @Parameter(defaultValue = "", property = "anypoint.businessGroup")
  protected String businessGroup = "";

  @Parameter(defaultValue = "Medium", readonly = true, property = "arm.insecure")
  protected boolean armInsecure;

  @Parameter(property = "mule.application")
  protected File application;

  @Parameter(readonly = true, property = "applicationName")
  protected String applicationName;

  @Parameter(property = "anypoint.target")
  protected String target;

  @Parameter(property = "anypoint.target.type")
  protected TargetType targetType;

  @Parameter(property = "mule.skip")
  protected String skip;

  @Parameter
  protected File domain;

  @Parameter(property = "script", required = false)
  protected File script;

  @Parameter(property = "mule.timeout", required = false)
  protected int timeout;

  @Parameter
  protected List<ArtifactCoordinates> artifactItems = new ArrayList<>();


  // DeployMojo configuration parameters

  @Parameter(readonly = true, required = false, defaultValue = "false", property = "mule.community")
  protected boolean community;

  @Parameter(readonly = true)
  protected ArtifactCoordinates muleDistribution;

  @Parameter(property = "mule.deploymentConfiguration.timeout", defaultValue = "60000", required = true)
  protected Long deploymentTimeout;

  @Parameter(property = "mule.arguments", required = false)
  protected String[] arguments;

  @Parameter
  protected List<File> libs = new ArrayList<>();

  @Parameter(property = "cloudhub.region", defaultValue = "us-east-1")
  protected String region;

  @Parameter(property = "cloudhub.workers")
  protected Integer workers = 1;

  @Parameter(defaultValue = "Medium", property = "cloudhub.workerType")
  protected String workerType;

  @Parameter(required = false)
  protected Map<String, String> properties;

  // Undeploy mojo configuration parameters

  @Parameter(defaultValue = "true")
  protected boolean failIfNotExists;

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
   * DeploymentConfiguration information.
   *
   * @since 1.0
   */
  public DeploymentType getDeploymentType() {
    return deploymentType;
  }

  public void setDeploymentType(DeploymentType deploymentType) {
    this.deploymentType = deploymentType;
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
   * Path to a Mule Standalone server. This parameter and <code>muleDistribution</code> and <code>muleVersion</code> are mutual
   * exclusive.
   *
   * @since 2.0
   */
  public File getMuleHome() {
    return muleHome;
  }

  public void setMuleHome(File muleHome) {
    this.muleHome = muleHome;
  }

  /**
   * Version of the Mule Runtime Enterprise distribution to download. If you need to use Community version use
   * <code>muleDistribution</code> parameter. This parameter and <code>muleDistribution</code> are mutual exclusive.
   *
   * @since 1.0
   */
  public String getMuleVersion() {
    return muleVersion;
  }

  public void setMuleVersion(String muleVersion) {
    this.muleVersion = muleVersion;
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

  public String getSkip() {
    return skip;
  }

  public void setSkip(String skip) {
    this.skip = skip;
  }

  public File getDomain() {
    return domain;
  }

  public void setDomain(File domain) {
    this.domain = domain;
  }

  public File getScript() {
    return script;
  }

  public void setScript(File script) {
    this.script = script;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public List<ArtifactCoordinates> getArtifactItems() {
    return artifactItems;
  }

  public void setArtifactItems(List<ArtifactCoordinates> artifactItems) {
    this.artifactItems = artifactItems;
  }

  /**
   * When set to true the plugin will use Mule Standalone Community Edition.
   *
   * @since 2.0
   */
  public boolean isCommunity() {
    return community;
  }

  public void setCommunity(boolean community) {
    this.community = community;
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
   * List of external libs (Jar files) to be added to MULE_HOME/user/lib directory.
   *
   * @since 1.0
   */
  public List<File> getLibs() {
    if (libs == null) {
      libs = new ArrayList<>();
    }
    return libs;
  }

  public void setLibs(List<File> libs) {
    this.libs = libs;
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
}

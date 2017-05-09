/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

import org.apache.maven.plugins.annotations.Parameter;
import org.mule.tools.client.arm.model.Target;
import org.mule.tools.client.model.TargetType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeploymentConfiguration {

  protected String server;

  protected String username;

  protected String password;

  @Parameter(required = true)
  protected AbstractMuleMojo.DeploymentType deploymentType;

  @Parameter(readonly = true)
  protected String uri;

  protected String environment;

  protected File muleHome;

  protected String muleVersion;

  @Parameter(required = true)
  protected Integer size;

  protected String businessGroup = "";

  @Parameter(defaultValue = "Medium", readonly = true, property = "arm.insecure")
  protected boolean armInsecure;

  protected File application;

  @Parameter(readonly = true)
  protected String applicationName;

  protected String target;

  protected TargetType targetType;

  protected String skip;

  protected File domain;

  protected File script;

  protected Integer timeout;

  protected List<ArtifactDescription> artifactItems = new ArrayList<ArtifactDescription>();


  // DeployMojo configuration parameters

  @Parameter(readonly = true)
  protected Boolean community;

  @Parameter(readonly = true)
  protected ArtifactDescription muleDistribution;

  @Parameter(required = true)
  protected Long deploymentTimeout;

  protected String[] arguments;

  protected List<File> libs = new ArrayList<>();

  protected String region;

  protected Integer workers = 1;

  protected String workerType;

  protected Map<String, String> properties;

  // Undeploy mojo configuration parameters

  protected Boolean failIfNotExists;

  /**
   * Maven server with Anypoint Platform credentials. This is only needed if you want to use your credentials stored in your Maven
   * settings.xml file. This is NOT your Mule server name.
   *
   * @since 2.2
   */
  public String getServer() {
    if (server == null) {
      server = System.getProperty("maven.server");
    }
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
    if (username == null) {
      username = System.getProperty("anypoint.username");
    }
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
    if (password == null) {
      password = "anypoint.password";
    }
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
  public AbstractMuleMojo.DeploymentType getDeploymentType() {
    return deploymentType;
  }

  public void setDeploymentType(AbstractMuleMojo.DeploymentType deploymentType) {
    this.deploymentType = deploymentType;
  }

  /**
   * Anypoint Platform URI, can be configured to use with On Premise platform..
   *
   * @since 2.0
   */
  public String getUri() {
    if (uri == null) {
      uri = System.getProperty("anypoint.uri");
    }
    if (uri == null) {
      uri = "https://anypoint.mulesoft.com";
    }
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
    if (environment == null) {
      environment = System.getProperty("anypoint.environment");
    }
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
    if (muleHome == null) {
      String muleHomePath = System.getProperty("mule.home");
      if (muleHomePath != null) {
        muleHome = new File(muleHomePath);
      }
    }
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
    if (muleVersion == null) {
      muleVersion = "mule.version";
    }
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
    if (size == null) {
      size = 2;
    }
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
    if (businessGroup == null) {
      businessGroup = System.getProperty("anypoint.businessGroup");
    }
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
    if (application == null) {
      String applicationPath = System.getProperty("mule.application");
      if (applicationPath != null) {
        application = new File(applicationPath);
      }
    }
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
    if (applicationName == null) {
      applicationName = System.getProperty("applicationName");
    }
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  /**
   * Anypoint Platform target name.
   *
   * @since 2.0
   * @see DeployMojo#targetType
   */
  public String getTarget() {
    if (target == null) {
      target = System.getProperty("anypoint.target");
    }
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
    if (targetType == null) {
      String targetTypeName = System.getProperty("anypoint.target.type");
      if (targetTypeName != null) {
        targetType = TargetType.valueOf(targetTypeName);
      }
    }
    return targetType;
  }

  public void setTargetType(TargetType targetType) {
    this.targetType = targetType;
  }

  public String getSkip() {
    if (skip == null) {
      skip = System.getProperty("mule.skip");
    }
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
    if (script == null) {
      String scriptPath = System.getProperty("script");
      if (scriptPath != null) {
        script = new File(scriptPath);
      }
    }
    return script;
  }

  public void setScript(File script) {
    this.script = script;
  }

  public int getTimeout() {
    if (timeout == null) {
      String timeoutValue = System.getProperty("mule.timeout");
      if (timeoutValue != null) {
        timeout = Integer.parseInt(timeoutValue);
      }
    }
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public List<ArtifactDescription> getArtifactItems() {
    return artifactItems;
  }

  public void setArtifactItems(List<ArtifactDescription> artifactItems) {
    this.artifactItems = artifactItems;
  }

  /**
   * When set to true the plugin will use Mule Standalone Community Edition.
   *
   * @since 2.0
   */
  public boolean isCommunity() {
    if (community == null) {
      community = Boolean.valueOf(System.getProperty("mule.community"));
    }
    if (community == null) {
      community = false;
    }
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
  public ArtifactDescription getMuleDistribution() {
    return muleDistribution;
  }

  public void setMuleDistribution(ArtifactDescription muleDistribution) {
    this.muleDistribution = muleDistribution;
  }

  /**
   * DeploymentConfiguration timeout in milliseconds.
   *
   * @since 1.0
   */
  public Long getDeploymentTimeout() {
    if (deploymentTimeout == null) {
      String deploymentTimeoutValue = System.getProperty("mule.deploymentConfiguration.timeout");
      if (deploymentTimeoutValue != null) {
        deploymentTimeout = Long.parseLong(deploymentTimeoutValue);
      }
    }
    if (deploymentTimeout == null) {
      deploymentTimeout = new Long(60000);
    }
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
      String argumentsValue = System.getProperty("mule.arguments");
      if (argumentsValue != null) {
        arguments = argumentsValue.split(",");
      }
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
    if (region == null) {
      region = System.getProperty("cloudhub.region");
    }
    if (region == null) {
      region = "us-east-1";
    }
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
    if (workers == null) {
      String workersValue = System.getProperty("cloudhub.workers");
      if (workersValue != null) {
        workers = Integer.parseInt(workersValue);
      }
    }
    if (workers == null) {
      workers = 1;
    }
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
    if (workerType == null) {
      workerType = System.getProperty("cloudhub.workerType");
    }
    if (workerType == null) {
      workerType = "Medium";
    }
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
    if (failIfNotExists == null) {
      failIfNotExists = true;
    }
    return failIfNotExists;
  }

  public void setFailIfNotExists(boolean failIfNotExists) {
    this.failIfNotExists = failIfNotExists;
  }
}

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
import org.mule.tools.client.core.exception.DeploymentException;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class RuntimeFabricDeployment extends AnypointDeployment {

  @Parameter
  protected String target;

  @Parameter
  protected RuntimeFabricDeploymentSettings deploymentSettings;

  @Parameter
  protected String provider;

  @Parameter
  protected String replicas;

  @Parameter
  protected Map<String, String> secureProperties;



  public String getReplicas() {
    return replicas;
  }


  public void setReplicas(String replicas) {
    this.replicas = replicas;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String targetId) {
    this.target = targetId;
  }

  public RuntimeFabricDeploymentSettings getDeploymentSettings() {
    return deploymentSettings;
  }

  public void setDeploymentSettings(RuntimeFabricDeploymentSettings settings) {
    this.deploymentSettings = settings;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public Map<String, String> getSecureProperties() {
    return secureProperties;
  }

  public void setSecureProperties(Map<String, String> secureProperties) {
    this.secureProperties = secureProperties;
  }


  public void setEnvironmentSpecificValues() throws DeploymentException {
    super.setEnvironmentSpecificValues();
    if (isEmpty(getTarget())) {
      throw new DeploymentException("Invalid deployment configuration, missing target value. Please set it in the plugin configuration");
    }

    if (isEmpty(getProvider())) {
      throw new DeploymentException("Invalid deployment configuration, missing provider value. Please set the provider as MC, CLUSTER or SERVER");
    }
    if (getDeploymentSettings() == null) {
      setDeploymentSettings(new RuntimeFabricDeploymentSettings());
    }
    getDeploymentSettings().setRuntimeVersion(muleVersion);
    getDeploymentSettings().setEnvironmentSpecificValues();
  }
}

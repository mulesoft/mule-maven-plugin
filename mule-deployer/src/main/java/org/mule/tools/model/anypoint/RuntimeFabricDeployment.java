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

  @Parameter(defaultValue = "1")
  protected Integer replicas;

  @Parameter(defaultValue = "false")
  protected Boolean enableRuntimeClusterMode;

  @Parameter
  protected Double cores;

  @Parameter
  protected Double memory;

  @Parameter
  protected Map<String, String> properties = new HashMap<>();

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public Integer getReplicas() {
    return replicas;
  }

  public void setReplicas(Integer replicas) {
    this.replicas = replicas;
  }

  public Boolean getEnableRuntimeClusterMode() {
    return enableRuntimeClusterMode;
  }

  public void setEnableRuntimeClusterMode(Boolean enableRuntimeClusterMode) {
    this.enableRuntimeClusterMode = enableRuntimeClusterMode;
  }

  public Double getCores() {
    return cores;
  }

  public void setCores(Double cores) {
    this.cores = cores;
  }

  public Double getMemory() {
    return memory;
  }

  public void setMemory(Double memory) {
    this.memory = memory;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public void setEnvironmentSpecificValues() throws DeploymentException {
    super.setEnvironmentSpecificValues();
    if (isEmpty(target)) {
      throw new DeploymentException("Invalid deployment configuration, missing target value. Please set it in the plugin configuration");
    }
    if (getEnableRuntimeClusterMode() && getReplicas().equals(1)) {
      throw new DeploymentException("Invalid deployment configuration, replicas must be bigger than 1 to enable Runtime Cluster Mode. Please either set enableRuntimeClusterMode to false or increase the number of replicas");
    }
    if (getCores() == null) {
      throw new DeploymentException("Invalid deployment configuration. Please set the number of cores in vCPU");
    }
    if (getMemory() == null) {
      throw new DeploymentException("Invalid deployment configuration. Please set the amount of memory in GB");
    }
  }
}

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

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The default values to these parameters are hardcoded on the Runtime Manager UI.
 */
public class RuntimeFabricDeploymentSettings {

  public RuntimeFabricDeploymentSettings() {}

  public RuntimeFabricDeploymentSettings(RuntimeFabricDeploymentSettings settings) {
    runtimeVersion = settings.runtimeVersion;
    replicationFactor = settings.replicationFactor;
    memoryReserved = settings.memoryReserved;
    memoryMax = settings.memoryMax;
    cpuReserved = settings.cpuReserved;
    cpuMax = settings.cpuMax;
    publicUrl = settings.publicUrl;
    lastMileSecurity = settings.lastMileSecurity;
    clusteringEnabled = settings.clusteringEnabled;
  }

  protected String runtimeVersion;

  @Parameter
  protected Integer replicationFactor;

  @Parameter
  protected String memoryReserved;

  @Parameter
  protected String memoryMax;

  @Parameter
  protected String cpuReserved;

  @Parameter
  protected String cpuMax;

  @Parameter
  protected String publicUrl;

  @Parameter
  protected boolean lastMileSecurity;

  @Parameter
  protected boolean clusteringEnabled;

  public String getRuntimeVersion() {
    return runtimeVersion;
  }

  public void setRuntimeVersion(String runtimeVersion) {
    this.runtimeVersion = runtimeVersion;
  }

  public Integer getReplicationFactor() {
    return replicationFactor;
  }

  public void setReplicationFactor(Integer replicationFactor) {
    this.replicationFactor = replicationFactor;
  }

  public String getMemoryReserved() {
    return memoryReserved;
  }

  public void setMemoryReserved(String memoryReserved) {
    this.memoryReserved = memoryReserved;
  }

  public String getMemoryMax() {
    return memoryMax;
  }

  public void setMemoryMax(String memoryMax) {
    this.memoryMax = memoryMax;
  }

  public String getCpuReserved() {
    return cpuReserved;
  }

  public void setCpuReserved(String cpuReserved) {
    this.cpuReserved = cpuReserved;
  }

  public String getCpuMax() {
    return cpuMax;
  }

  public void setCpuMax(String cpuMax) {
    this.cpuMax = cpuMax;
  }

  public String getPublicUrl() {
    return publicUrl;
  }

  public void setPublicUrl(String publicUrl) {
    this.publicUrl = publicUrl;
  }

  public boolean getLastMileSecurity() {
    return lastMileSecurity;
  }

  public void setLastMileSecurity(boolean lastMileSecurity) {
    this.lastMileSecurity = lastMileSecurity;
  }

  public boolean isClusteringEnabled() {
    return clusteringEnabled;
  }

  public void setClusteringEnabled(boolean clusteringEnabled) {
    this.clusteringEnabled = clusteringEnabled;
  }

  public void setEnvironmentSpecificValues() throws DeploymentException {

    if (getReplicationFactor() == null) {
      setReplicationFactor(1);
    }

    if (isClusteringEnabled() && getReplicationFactor().equals(1)) {
      throw new DeploymentException("Invalid deployment configuration, replicas must be bigger than 1 to enable Runtime Cluster Mode. Please either set enableRuntimeClusterMode to false or increase the number of replicas");
    }

    if (isEmpty(getMemoryReserved())) {
      setMemoryReserved("700Mi");
    }

    if (isEmpty(getMemoryMax())) {
      setMemoryMax(getMemoryReserved());
    }

    if (isEmpty(getCpuReserved())) {
      setCpuReserved("500m");
    }

    if (isEmpty(getCpuMax())) {
      setCpuMax(getCpuReserved());
    }
  }
}

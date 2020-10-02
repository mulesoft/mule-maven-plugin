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

  public RuntimeFabricDeploymentSettings() {

  }


  public RuntimeFabricDeploymentSettings(RuntimeFabricDeploymentSettings settings) {
    runtimeVersion = settings.runtimeVersion;
    replicationFactor = settings.replicationFactor;
    resources = settings.getResources();
    publicUrl = settings.publicUrl;
    lastMileSecurity = settings.lastMileSecurity;
    clustered = settings.clustered;
    updateStrategy = settings.updateStrategy;
  }

  @Parameter
  protected String runtimeVersion;

  @Parameter
  protected Integer replicationFactor;

  @Parameter
  protected Resources resources;

  @Parameter
  protected String publicUrl;

  @Parameter
  protected boolean lastMileSecurity;

  @Parameter
  protected boolean clustered;

  @Parameter
  protected String updateStrategy;

  @Parameter
  protected boolean enforceDeployingReplicasAcrossNodes;

  @Parameter
  protected Http http;

  @Parameter
  protected boolean forwardSslSession;

  @Parameter
  protected boolean disableAmLogForwarding;



  @Parameter
  protected String updateStrategy;

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



  public Resources getResources() {
    return resources;
  }


  public void setResources(Resources resources) {
    this.resources = resources;
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

  public boolean isClustered() {
    return clustered;
  }

  public void setClustered(boolean clustered) {
    this.clustered = clustered;
  }

  public String getUpdateStrategy() {
    return updateStrategy;
  }

  public void setUpdateStrategy(String updateStrategy) {
    this.updateStrategy = updateStrategy;
  }

  public void setEnvironmentSpecificValues() throws DeploymentException {

    if (getReplicationFactor() == null) {
      setReplicationFactor(1);
    }

    if (isClustered() && getReplicationFactor().equals(1)) {
      throw new DeploymentException("Invalid deployment configuration, replicas must be bigger than 1 to enable Runtime Cluster Mode. Please either set enableRuntimeClusterMode to false or increase the number of replicas");
    }


    if (isEmpty(getResources().getMemory().getReserved())) {
      getResources().getMemory().setReserved("700Mi");;
    }

    if (isEmpty(getResources().getMemory().getLimit())) {
      getResources().getMemory().setLimit(getResources().getMemory().getReserved());
    }

    if (isEmpty(getResources().getCpu().getReserved())) {
      getResources().getCpu().setReserved("500m");;
    }

    if (isEmpty(getResources().getCpu().getLimit())) {
      getResources().getCpu().setLimit(getResources().getMemory().getReserved());
    }

    if (isEmpty(getResources().getCpu().getLimit())) {
      getResources().getCpu().setLimit(getResources().getMemory().getReserved());
    }

    if (isEmpty(getUpdateStrategy())) {
      setUpdateStrategy("rolling");
    }
  }



}

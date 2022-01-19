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
    http = new Http();
    resources = new Resources();
  }


  public RuntimeFabricDeploymentSettings(RuntimeFabricDeploymentSettings settings) {
    runtimeVersion = settings.runtimeVersion;
    resources = settings.resources;
    lastMileSecurity = settings.lastMileSecurity;
    persistentObjectStore = settings.persistentObjectStore;
    clustered = settings.clustered;
    updateStrategy = settings.updateStrategy;
    if (settings.jvm != null) {
      jvm = settings.jvm;
    }
    enforceDeployingReplicasAcrossNodes = settings.enforceDeployingReplicasAcrossNodes;
    http = settings.http;
    forwardSslSession = settings.forwardSslSession;
    disableAmLogForwarding = settings.disableAmLogForwarding;
  }

  @Parameter
  protected String runtimeVersion;



  @Parameter
  protected Resources resources;


  @Parameter
  protected boolean lastMileSecurity;

  @Parameter
  protected boolean persistentObjectStore;

  @Parameter
  protected boolean clustered;

  @Parameter
  protected String updateStrategy;

  @Parameter
  protected boolean enforceDeployingReplicasAcrossNodes;

  @Parameter
  protected Http http;

  @Parameter
  protected Jvm jvm;

  @Parameter
  protected boolean forwardSslSession;

  @Parameter
  protected boolean disableAmLogForwarding;



  public String getRuntimeVersion() {
    return runtimeVersion;
  }

  public void setRuntimeVersion(String runtimeVersion) {
    this.runtimeVersion = runtimeVersion;
  }



  public Resources getResources() {
    return resources;
  }


  public void setResources(Resources resources) {
    this.resources = resources;
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



  public boolean isEnforceDeployingReplicasAcrossNodes() {
    return enforceDeployingReplicasAcrossNodes;
  }



  public void setEnforceDeployingReplicasAcrossNodes(boolean enforceDeployingReplicasAcrossNodes) {
    this.enforceDeployingReplicasAcrossNodes = enforceDeployingReplicasAcrossNodes;
  }



  public Http getHttp() {
    return http;
  }



  public void setHttp(Http http) {
    this.http = http;
  }


  public boolean isForwardSslSession() {
    return forwardSslSession;
  }



  public void setForwardSslSession(boolean forwardSslSession) {
    this.forwardSslSession = forwardSslSession;
  }


  public boolean isDisableAmLogForwarding() {
    return disableAmLogForwarding;
  }



  public void setDisableAmLogForwarding(boolean disableAmLogForwarding) {
    this.disableAmLogForwarding = disableAmLogForwarding;
  }


  public void setEnvironmentSpecificValues() throws DeploymentException {



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
      getResources().getCpu().setLimit(getResources().getCpu().getReserved());
    }

    if (isEmpty(getUpdateStrategy())) {
      setUpdateStrategy("rolling");
    }
  }



}

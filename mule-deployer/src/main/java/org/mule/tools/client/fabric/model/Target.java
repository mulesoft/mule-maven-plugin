/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.fabric.model;

import org.mule.tools.model.anypoint.RuntimeFabricDeploymentSettings;

public class Target {

  public String targetId;
  public String provider;
  public RuntimeFabricDeploymentSettings deploymentSettings;
  public String replicas;


  public void setTargetId(String targetId) {
    this.targetId = targetId;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public void setDeploymentSettings(RuntimeFabricDeploymentSettings deploymentSettings) {
    this.deploymentSettings = deploymentSettings;
  }


  public String getReplicas() {
    return replicas;
  }


  public void setReplicas(String replicas) {
    this.replicas = replicas;
  }

}

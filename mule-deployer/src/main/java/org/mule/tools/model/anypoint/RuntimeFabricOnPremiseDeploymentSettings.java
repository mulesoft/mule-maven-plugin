/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.model.anypoint;

import org.apache.maven.plugins.annotations.Parameter;
import org.mule.tools.client.core.exception.DeploymentException;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The default values to these parameters are hardcoded on the Runtime Manager UI.
 */
public class RuntimeFabricOnPremiseDeploymentSettings extends RuntimeFabricDeploymentSettings {

  public RuntimeFabricOnPremiseDeploymentSettings() {
    http = new Http();
    resources = new Resources();
  }


  public RuntimeFabricOnPremiseDeploymentSettings(RuntimeFabricOnPremiseDeploymentSettings settings) {
    super(settings);
    resources = settings.resources;
    if (settings.jvm != null) {
      jvm = settings.jvm;
    }
  }

  @Parameter
  protected Resources resources;

  @Parameter
  protected Jvm jvm;



  public Resources getResources() {
    return resources;
  }


  public void setResources(Resources resources) {
    this.resources = resources;
  }


  public void setEnvironmentSpecificValues() throws DeploymentException {
    super.setEnvironmentSpecificValues();


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

  }



}

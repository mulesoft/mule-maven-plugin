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
public class RuntimeFabricOnPremiseDeploymentSettings extends RuntimeFabricDeploymentSettings {

  public RuntimeFabricOnPremiseDeploymentSettings() {
    http = new Http();
    resources = new Resources();
  }


  public RuntimeFabricOnPremiseDeploymentSettings(RuntimeFabricOnPremiseDeploymentSettings settings) {
    super(settings);
    resources = settings.resources;
    persistentObjectStore = settings.persistentObjectStore;
    if (settings.jvm != null) {
      jvm = settings.jvm;
    }
  }

  @Parameter
  protected Resources resources;

  @Parameter
  protected Jvm jvm;

  @Parameter
  protected boolean persistentObjectStore;

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

  public boolean isPersistentObjectStore() {
    return persistentObjectStore;
  }

  public void setPersistentObjectStore(boolean persistentObjectStore) {
    this.persistentObjectStore = persistentObjectStore;
  }

}

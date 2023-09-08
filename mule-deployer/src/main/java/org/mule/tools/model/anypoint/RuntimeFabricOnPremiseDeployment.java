/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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

public class RuntimeFabricOnPremiseDeployment extends RuntimeFabricDeployment {

  @Parameter
  protected RuntimeFabricOnPremiseDeploymentSettings deploymentSettings;

  public RuntimeFabricDeploymentSettings getDeploymentSettings() {
    return deploymentSettings;
  }

  public void setDeploymentSettings(RuntimeFabricDeploymentSettings settings) {
    this.deploymentSettings = (RuntimeFabricOnPremiseDeploymentSettings) settings;
  }

  public void setEnvironmentSpecificValues() throws DeploymentException {
    super.setEnvironmentSpecificValues();
    if (getDeploymentSettings() == null) {
      setDeploymentSettings(new RuntimeFabricOnPremiseDeploymentSettings());
    }
    getDeploymentSettings().setRuntimeVersion(muleVersion);
    getDeploymentSettings().setEnvironmentSpecificValues();
  }
}

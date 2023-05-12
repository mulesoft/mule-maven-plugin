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

import java.util.List;

import org.apache.maven.plugins.annotations.Parameter;
import org.mule.tools.client.core.exception.DeploymentException;

public class Cloudhub2Deployment extends RuntimeFabricDeployment {

  @Parameter
  protected String vCores;

  @Parameter
  protected Integration integrations;

  @Parameter
  protected String artifactName;

  @Parameter
  protected List<ScopeLoggingConfiguration> scopeLoggingConfigurations;

  @Parameter
  protected Cloudhub2DeploymentSettings deploymentSettings;

  public String getvCores() {
    return vCores;
  }

  public void setvCores(String vCores) {
    this.vCores = vCores;
  }


  public Integration getIntegrations() {
    return integrations;
  }


  public void setIntegrations(Integration integrations) {
    this.integrations = integrations;
  }

  public String getArtifactName() {
    return artifactName;
  }

  public List<ScopeLoggingConfiguration> getScopeLoggingConfigurations() {
    return scopeLoggingConfigurations;
  }

  public void setScopeLoggingConfigurations(List<ScopeLoggingConfiguration> scopeLoggingConfigurations) {
    this.scopeLoggingConfigurations = scopeLoggingConfigurations;
  }

  public RuntimeFabricDeploymentSettings getDeploymentSettings() {
    return deploymentSettings;
  }

  public void setDeploymentSettings(RuntimeFabricDeploymentSettings settings) {
    this.deploymentSettings = (Cloudhub2DeploymentSettings) settings;
  }

  public void setEnvironmentSpecificValues() throws DeploymentException {
    super.setEnvironmentSpecificValues();
    if (getDeploymentSettings() == null) {
      setDeploymentSettings(new Cloudhub2DeploymentSettings());
    }
    getDeploymentSettings().setRuntimeVersion(muleVersion);
    getDeploymentSettings().setEnvironmentSpecificValues();
  }


}

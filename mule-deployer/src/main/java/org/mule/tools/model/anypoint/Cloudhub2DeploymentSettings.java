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
public class Cloudhub2DeploymentSettings extends RuntimeFabricDeploymentSettings {

  public Cloudhub2DeploymentSettings() {
    super();
  }


  public Cloudhub2DeploymentSettings(Cloudhub2DeploymentSettings settings) {
    super();
  }



}

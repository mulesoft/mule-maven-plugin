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
public class Cloudhub2DeploymentSettings extends RuntimeFabricDeploymentSettings {

  public Cloudhub2DeploymentSettings() {
    super();
  }


  public Cloudhub2DeploymentSettings(Cloudhub2DeploymentSettings settings) {
    super();
    this.generateDefaultPublicUrl = settings.getGenerateDefaultPublicUrl();
  }

  @Parameter
  protected String generateDefaultPublicUrl;


  public String getGenerateDefaultPublicUrl() {
    return generateDefaultPublicUrl;
  }



  public void setGenerateDefaultPublicUrl(String generateDefaultPublicUrl) {
    this.generateDefaultPublicUrl = generateDefaultPublicUrl;
  }



}

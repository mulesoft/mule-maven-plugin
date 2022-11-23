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

/**
 * The default values to these parameters are hardcoded on the Runtime Manager UI.
 */
public class Cloudhub2DeploymentSettings extends RuntimeFabricDeploymentSettings {

  @Parameter
  protected String instanceType;

  public Cloudhub2DeploymentSettings() {
    super();
  }


  public Cloudhub2DeploymentSettings(Cloudhub2DeploymentSettings settings) {
    super(settings);
    instanceType = settings.instanceType;
  }



  public String getInstanceType() {
    return instanceType;
  }

  public void setInstanceType(String instanceType) {
    this.instanceType = instanceType;
  }



}

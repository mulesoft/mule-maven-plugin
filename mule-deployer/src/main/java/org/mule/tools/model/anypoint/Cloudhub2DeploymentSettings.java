/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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

  public static class Environment {

    protected Http http;

    public Environment(Http http) {
      this.http = http;
    }
  }

  @Parameter
  protected String instanceType;
  protected Environment environment;

  public Cloudhub2DeploymentSettings() {
    super();
    environment = buildEnvironment();
  }


  public Cloudhub2DeploymentSettings(Cloudhub2DeploymentSettings settings) {
    super(settings);
    instanceType = settings.instanceType;
    environment = buildEnvironment();
  }

  public String getInstanceType() {
    return instanceType;
  }

  public void setInstanceType(String instanceType) {
    this.instanceType = instanceType;
  }

  protected Environment buildEnvironment() {
    return new Environment(http);
  }
}

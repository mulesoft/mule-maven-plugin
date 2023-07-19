/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.fabric.model;

public class DeploymentModify {

  public Target target;
  public ApplicationModify application;

  public void setTarget(Target target) {
    this.target = target;
  }

  public void setApplication(ApplicationModify application) {
    this.application = application;
  }
}

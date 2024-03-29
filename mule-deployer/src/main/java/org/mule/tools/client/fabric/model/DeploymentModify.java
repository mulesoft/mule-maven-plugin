/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.fabric.model;

import java.util.List;

public class DeploymentRequest {

  public String name;
  public ApplicationRequest application;
  public List<String> labels;
  public Target target;

  public void setName(String name) {
    this.name = name;
  }

  public void setApplication(ApplicationRequest application) {
    this.application = application;
  }

  public void setLabels(List<String> labels) {
    this.labels = labels;
  }

  public void setTarget(Target target) {
    this.target = target;
  }
}

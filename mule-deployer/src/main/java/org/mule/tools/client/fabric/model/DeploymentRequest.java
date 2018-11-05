/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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

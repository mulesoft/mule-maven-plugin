/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model.anypoint;

import org.apache.maven.plugins.annotations.Parameter;

public class Autoscaling {

  public Autoscaling() {}

  public Autoscaling(Boolean enabled, Integer minReplicas, Integer maxReplicas) {
    this.enabled = enabled;
    this.minReplicas = minReplicas;
    this.maxReplicas = maxReplicas;
  }

  @Parameter
  protected Boolean enabled;

  @Parameter
  protected Integer minReplicas;

  @Parameter
  protected Integer maxReplicas;

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public Integer getMinReplicas() {
    return minReplicas;
  }

  public void setMinReplicas(Integer minReplicas) {
    this.minReplicas = minReplicas;
  }

  public Integer getMaxReplicas() {
    return maxReplicas;
  }

  public void setMaxReplicas(Integer maxReplicas) {
    this.maxReplicas = maxReplicas;
  }
}

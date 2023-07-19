/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.model.anypoint;

import org.apache.maven.plugins.annotations.Parameter;

public class Integration {

  @Parameter
  private Service services;


  public Integration() {}


  public Service getServices() {
    return services;
  }


  public void setServices(Service services) {
    this.services = services;
  }


}

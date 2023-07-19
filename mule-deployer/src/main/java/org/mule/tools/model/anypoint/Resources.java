/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.model.anypoint;


import org.apache.maven.plugins.annotations.Parameter;

public class Resources {

  @Parameter
  protected Resource cpu = new Resource();
  @Parameter
  protected Resource memory = new Resource();

  public Resources() {

  }

  public Resource getCpu() {
    return cpu;
  }

  public void setCpu(Resource cpu) {
    this.cpu = cpu;
  }

  public Resource getMemory() {
    return memory;
  }

  public void setMemory(Resource memory) {
    this.memory = memory;
  }

}

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

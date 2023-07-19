/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.model.anypoint;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugins.annotations.Parameter;

public class Jvm {

  @Parameter
  protected String args = new String();


  public String getArgs() {
    return args;
  }


  public void setArgs(String args) {
    this.args = args;
  }

}

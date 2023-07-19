/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.model.anypoint;

import org.apache.maven.plugins.annotations.Parameter;

public class Http {

  @Parameter
  protected Inbound inbound = new Inbound();


  public Inbound getInbound() {
    return inbound;
  }


  public void setInbound(Inbound inbound) {
    this.inbound = inbound;
  }


}

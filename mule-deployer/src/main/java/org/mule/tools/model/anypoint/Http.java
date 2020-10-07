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

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.model.anypoint;

import org.apache.maven.plugins.annotations.Parameter;

public class Resource {

  @Parameter
  protected String reserved;
  @Parameter
  protected String limit;


  public Resource() {
    super();
  }

  public String getReserved() {
    return reserved;
  }

  public void setReserved(String reserved) {
    this.reserved = reserved;
  }

  public String getLimit() {
    return limit;
  }

  public void setLimit(String limit) {
    this.limit = limit;
  }

}

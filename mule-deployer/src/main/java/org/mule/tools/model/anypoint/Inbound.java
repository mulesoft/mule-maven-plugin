/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.model.anypoint;

import org.apache.maven.plugins.annotations.Parameter;

public class Inbound {

  @Parameter
  protected String publicUrl;

  @Parameter
  protected String pathRewrite;

  @Parameter
  protected String lastMileSecurity;

  @Parameter
  protected String forwardSslSession;

  public String getPublicUrl() {
    return publicUrl;
  }


  public void setPublicUrl(String publicUrl) {
    this.publicUrl = publicUrl;
  }


}

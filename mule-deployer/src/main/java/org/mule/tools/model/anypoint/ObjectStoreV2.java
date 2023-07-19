/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.model.anypoint;

import org.apache.maven.plugins.annotations.Parameter;

public class ObjectStoreV2 {

  @Parameter
  protected boolean enabled;



  public ObjectStoreV2() {}


  public boolean isEnabled() {
    return enabled;
  }


  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }


}

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.cloudhub.model;

public class Environment {

  private boolean objectStoreV1Enabled;
  private String launchDarklyClientId;

  public boolean getObjectStoreV1Enabled() {
    return objectStoreV1Enabled;
  }

  public void setObjectStoreV1Enabled(boolean enabled) {
    objectStoreV1Enabled = enabled;
  }

  public String getLaunchDarklyClientId() {
    return launchDarklyClientId;
  }

  public void setLaunchDarklyClientId(String launchDarklyClientId) {
    this.launchDarklyClientId = launchDarklyClientId;
  }

}

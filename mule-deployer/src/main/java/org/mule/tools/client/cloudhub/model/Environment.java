/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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

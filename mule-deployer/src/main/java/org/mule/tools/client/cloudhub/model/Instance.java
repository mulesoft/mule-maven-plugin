/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.cloudhub.model;

public class Instance {

  private String instanceId;
  private String publicIPAddress;
  private String status;
  private String region;

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getPublicIPAddress() {
    return publicIPAddress;
  }

  public void setPublicIPAddress(String publicIPAddress) {
    this.publicIPAddress = publicIPAddress;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }
}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.cloudhub.model;

public class DeploymentLogRequest {

  private String deploymentId;
  private Long startTime;
  private Long endTime;
  private int limitMsgLen = 5000;

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public Long getStartTime() {
    return startTime;
  }

  public void setStartTime(Long startTime) {
    this.startTime = startTime;
  }

  public Long getEndTime() {
    return endTime;
  }

  public void setEndTime(Long endTime) {
    this.endTime = endTime;
  }

  public int getLimitMsgLen() {
    return limitMsgLen;
  }

  public void setLimitMsgLen(int limitMsgLen) {
    this.limitMsgLen = limitMsgLen;
  }
}

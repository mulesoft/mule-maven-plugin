/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.cloudhub.model;

import java.util.Date;
import java.util.List;

public class Deployment {

  private String deploymentId;
  private Date createTime;
  private Date startTime;
  private Date endTime;
  private List<Instance> instances;

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public List<Instance> getInstances() {
    return instances;
  }

  public void setInstances(List<Instance> instances) {
    this.instances = instances;
  }
}

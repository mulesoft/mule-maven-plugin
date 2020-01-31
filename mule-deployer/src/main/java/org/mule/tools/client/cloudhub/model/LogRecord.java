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

public class LogRecord {

  private String recordId;
  private String deploymentId;
  private String instanceId;
  private Long line;
  private LogMessage event;

  public String getRecordId() {
    return recordId;
  }

  public void setRecordId(String recordId) {
    this.recordId = recordId;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public Long getLine() {
    return line;
  }

  public void setLine(Long line) {
    this.line = line;
  }

  public LogMessage getEvent() {
    return event;
  }

  public void setEvent(LogMessage event) {
    this.event = event;
  }
}

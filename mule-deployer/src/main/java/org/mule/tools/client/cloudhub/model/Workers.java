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

import java.util.Map;

/**
 * @author Mulesoft Inc.
 * @since 3.2.0
 */
public class Workers {

  private WorkerType type;
  private Integer amount;
  private Double remainingOrgWorkers;
  private Double totalOrgWorkers;
  private Map<String, String> recentStatistics;

  public WorkerType getType() {
    return type;
  }

  public void setType(WorkerType type) {
    this.type = type;
  }

  public Integer getAmount() {
    return amount;
  }

  public void setAmount(Integer amount) {
    this.amount = amount;
  }

  public Double getRemainingOrgWorkers() {
    return remainingOrgWorkers;
  }

  public void setRemainingOrgWorkers(Double remainingOrgWorkers) {
    this.remainingOrgWorkers = remainingOrgWorkers;
  }

  public Double getTotalOrgWorkers() {
    return totalOrgWorkers;
  }

  public void setTotalOrgWorkers(Double totalOrgWorkers) {
    this.totalOrgWorkers = totalOrgWorkers;
  }

  public Map<String, String> getRecentStatistics() {
    return recentStatistics;
  }

  public void setRecentStatistics(Map<String, String> recentStatistics) {
    this.recentStatistics = recentStatistics;
  }
}

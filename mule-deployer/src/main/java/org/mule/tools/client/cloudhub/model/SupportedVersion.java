/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.cloudhub.model;

/**
 * @author Mulesoft Inc.
 * @since 3.2.0
 */
public class SupportedVersion {

  private String state;
  private String version;
  private Boolean recommended;
  private Long endOfSupportDate;
  private Long endOfLifeDate;
  private LatestUpdate latestUpdate;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Boolean getRecommended() {
    return recommended;
  }

  public void setRecommended(Boolean recommended) {
    this.recommended = recommended;
  }

  public Long getEndOfSupportDate() {
    return endOfSupportDate;
  }

  public void setEndOfSupportDate(Long endOfSupportDate) {
    this.endOfSupportDate = endOfSupportDate;
  }

  public Long getEndOfLifeDate() {
    return endOfLifeDate;
  }

  public void setEndOfLifeDate(Long endOfLifeDate) {
    this.endOfLifeDate = endOfLifeDate;
  }

  public LatestUpdate getLatestUpdate() {
    return latestUpdate;
  }

  public void setLatestUpdate(LatestUpdate latestUpdate) {
    this.latestUpdate = latestUpdate;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }
}

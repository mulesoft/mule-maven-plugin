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

/**
 * @author Mulesoft Inc.
 * @since 3.2.0
 */
public class MuleVersion {

  private String state;
  private String version;
  private Boolean log4j1Used;
  private String vpnSupported;
  private Boolean monitoringSupported;
  private Boolean persistentQueuesSupported;
  private String updateId;
  private String latestUpdateId;

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Boolean getLog4j1Used() {
    return log4j1Used;
  }

  public void setLog4j1Used(Boolean log4j1Used) {
    this.log4j1Used = log4j1Used;
  }

  public String getVpnSupported() {
    return vpnSupported;
  }

  public void setVpnSupported(String vpnSupported) {
    this.vpnSupported = vpnSupported;
  }

  public Boolean getMonitoringSupported() {
    return monitoringSupported;
  }

  public void setMonitoringSupported(Boolean monitoringSupported) {
    this.monitoringSupported = monitoringSupported;
  }

  public Boolean getPersistentQueuesSupported() {
    return persistentQueuesSupported;
  }

  public void setPersistentQueuesSupported(Boolean persistentQueuesSupported) {
    this.persistentQueuesSupported = persistentQueuesSupported;
  }

  public String getUpdateId() {
    return updateId;
  }

  public void setUpdateId(String updateId) {
    this.updateId = updateId;
  }

  public String getLatestUpdateId() {
    return latestUpdateId;
  }

  public void setLatestUpdateId(String latestUpdateId) {
    this.latestUpdateId = latestUpdateId;
  }
}

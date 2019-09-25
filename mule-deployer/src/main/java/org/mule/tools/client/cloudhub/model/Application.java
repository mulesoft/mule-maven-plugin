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


import java.util.List;
import java.util.Map;

/**
 *
 */
public class Application {

  private String id;
  private String domain;
  private String fullDomain;
  private String description;
  private Map<String, String> properties;
  // private propertiesOptions <-- dafuck
  private String deploymentUpdateStatus;
  private String status;
  private Long lastUpdateTime;
  private String filename;
  private Integer tentants;
  private String userId;
  private String userName;
  private Boolean persistentQueues;
  private Boolean persistentQueuesEncryptionEnabled;
  private Boolean persistentQueuesEncrypted;
  private Boolean monitoringEnabled;
  private Boolean monitoringAutoRestart;
  private Boolean staticIPsEnabled;
  private Boolean multitenanted;
  private Boolean hasFile;
  private Boolean secureDataGatewayEnabled;
  private Boolean vpnEnabled;
  private Boolean objectStoreV1;
  private Map<String, String> trackingSettings;
  private MuleVersion muleVersion;
  private MuleVersion previousMuleVersion;
  private List<MuleVersion> supportedVersions;

  private String region;
  private Workers workers;
  private List<LogLevelInfo> logLevels;
  private List<IpAddress> ipAddresses;

  // TODO a class VpnConfig
  private Map<String, String> vpnConfig;

  public void setDeploymentUpdateStatus(String deploymentUpdateStatus) {
    this.deploymentUpdateStatus = deploymentUpdateStatus;
  }

  public String getDeploymentUpdateStatus() {
    return deploymentUpdateStatus;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getFullDomain() {
    return fullDomain;
  }

  public void setFullDomain(String fullDomain) {
    this.fullDomain = fullDomain;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(Long lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public Integer getTentants() {
    return tentants;
  }

  public void setTentants(Integer tentants) {
    this.tentants = tentants;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public List<IpAddress> getIpAddresses() {
    return ipAddresses;
  }

  public void setIpAddresses(List<IpAddress> ipAddresses) {
    this.ipAddresses = ipAddresses;
  }

  public List<LogLevelInfo> getLogLevels() {
    return logLevels;
  }

  public void setLogLevels(List<LogLevelInfo> logLevels) {
    this.logLevels = logLevels;
  }

  public Boolean getPersistentQueues() {
    return persistentQueues;
  }

  public void setPersistentQueues(Boolean persistentQueues) {
    this.persistentQueues = persistentQueues;
  }

  public Boolean getPersistentQueuesEncryptionEnabled() {
    return persistentQueuesEncryptionEnabled;
  }

  public void setPersistentQueuesEncryptionEnabled(Boolean persistentQueuesEncryptionEnabled) {
    this.persistentQueuesEncryptionEnabled = persistentQueuesEncryptionEnabled;
  }

  public Boolean getPersistentQueuesEncrypted() {
    return persistentQueuesEncrypted;
  }

  public void setPersistentQueuesEncrypted(Boolean persistentQueuesEncrypted) {
    this.persistentQueuesEncrypted = persistentQueuesEncrypted;
  }

  public Boolean getMonitoringEnabled() {
    return monitoringEnabled;
  }

  public void setMonitoringEnabled(Boolean monitoringEnabled) {
    this.monitoringEnabled = monitoringEnabled;
  }

  public Boolean getMonitoringAutoRestart() {
    return monitoringAutoRestart;
  }

  public void setMonitoringAutoRestart(Boolean monitoringAutoRestart) {
    this.monitoringAutoRestart = monitoringAutoRestart;
  }

  public Boolean getStaticIPsEnabled() {
    return staticIPsEnabled;
  }

  public void setStaticIPsEnabled(Boolean staticIPsEnabled) {
    this.staticIPsEnabled = staticIPsEnabled;
  }

  public Boolean getMultitenanted() {
    return multitenanted;
  }

  public void setMultitenanted(Boolean multitenanted) {
    this.multitenanted = multitenanted;
  }

  public Boolean getHasFile() {
    return hasFile;
  }

  public void setHasFile(Boolean hasFile) {
    this.hasFile = hasFile;
  }

  public Boolean getSecureDataGatewayEnabled() {
    return secureDataGatewayEnabled;
  }

  public void setSecureDataGatewayEnabled(Boolean secureDataGatewayEnabled) {
    this.secureDataGatewayEnabled = secureDataGatewayEnabled;
  }

  public Boolean getVpnEnabled() {
    return vpnEnabled;
  }

  public void setVpnEnabled(Boolean vpnEnabled) {
    this.vpnEnabled = vpnEnabled;
  }

  public Map<String, String> getTrackingSettings() {
    return trackingSettings;
  }

  public void setTrackingSettings(Map<String, String> trackingSettings) {
    this.trackingSettings = trackingSettings;
  }

  public MuleVersion getMuleVersion() {
    return muleVersion;
  }

  public void setMuleVersion(MuleVersion muleVersion) {
    this.muleVersion = muleVersion;
  }

  public MuleVersion getPreviousMuleVersion() {
    return previousMuleVersion;
  }

  public void setPreviousMuleVersion(MuleVersion previousMuleVersion) {
    this.previousMuleVersion = previousMuleVersion;
  }

  public List<MuleVersion> getSupportedVersions() {
    return supportedVersions;
  }

  public void setSupportedVersions(List<MuleVersion> supportedVersions) {
    this.supportedVersions = supportedVersions;
  }

  public Workers getWorkers() {
    return workers;
  }

  public void setWorkers(Workers workers) {
    this.workers = workers;
  }

  public Map<String, String> getVpnConfig() {
    return vpnConfig;
  }

  public void setVpnConfig(Map<String, String> vpnConfig) {
    this.vpnConfig = vpnConfig;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public Boolean getObjectStoreV1() {
    return objectStoreV1;
  }

  public void setObjectStoreV1(Boolean objectStoreV1) {
    this.objectStoreV1 = objectStoreV1;
  }
}

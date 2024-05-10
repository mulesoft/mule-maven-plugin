/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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
  private Boolean loggingCustomLog4JEnabled;
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

  public Application setDeploymentUpdateStatus(String deploymentUpdateStatus) {
    this.deploymentUpdateStatus = deploymentUpdateStatus;
    return this;
  }

  public String getDeploymentUpdateStatus() {
    return deploymentUpdateStatus;
  }

  public String getId() {
    return id;
  }

  public Application setId(String id) {
    this.id = id;
    return this;
  }

  public String getDomain() {
    return domain;
  }

  public Application setDomain(String domain) {
    this.domain = domain;
    return this;
  }

  public String getFullDomain() {
    return fullDomain;
  }

  public Application setFullDomain(String fullDomain) {
    this.fullDomain = fullDomain;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Application setDescription(String description) {
    this.description = description;
    return this;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public Application setProperties(Map<String, String> properties) {
    this.properties = properties;
    return this;
  }

  public String getStatus() {
    return status;
  }

  public Application setStatus(String status) {
    this.status = status;
    return this;
  }

  public Long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public Application setLastUpdateTime(Long lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
    return this;
  }

  public String getFilename() {
    return filename;
  }

  public Application setFilename(String filename) {
    this.filename = filename;
    return this;
  }

  public Integer getTentants() {
    return tentants;
  }

  public Application setTentants(Integer tentants) {
    this.tentants = tentants;
    return this;
  }

  public String getUserId() {
    return userId;
  }

  public Application setUserId(String userId) {
    this.userId = userId;
    return this;
  }

  public String getUserName() {
    return userName;
  }

  public Application setUserName(String userName) {
    this.userName = userName;
    return this;
  }

  public List<IpAddress> getIpAddresses() {
    return ipAddresses;
  }

  public Application setIpAddresses(List<IpAddress> ipAddresses) {
    this.ipAddresses = ipAddresses;
    return this;
  }

  public List<LogLevelInfo> getLogLevels() {
    return logLevels;
  }

  public Application setLogLevels(List<LogLevelInfo> logLevels) {
    this.logLevels = logLevels;
    return this;
  }

  public Boolean getPersistentQueues() {
    return persistentQueues;
  }

  public Application setPersistentQueues(Boolean persistentQueues) {
    this.persistentQueues = persistentQueues;
    return this;
  }

  public Boolean getPersistentQueuesEncryptionEnabled() {
    return persistentQueuesEncryptionEnabled;
  }

  public Application setPersistentQueuesEncryptionEnabled(Boolean persistentQueuesEncryptionEnabled) {
    this.persistentQueuesEncryptionEnabled = persistentQueuesEncryptionEnabled;
    return this;
  }

  public Boolean getPersistentQueuesEncrypted() {
    return persistentQueuesEncrypted;
  }

  public Application setPersistentQueuesEncrypted(Boolean persistentQueuesEncrypted) {
    this.persistentQueuesEncrypted = persistentQueuesEncrypted;
    return this;
  }

  public Boolean getLoggingCustomLog4JEnabled() {
    return loggingCustomLog4JEnabled;
  }

  public Application setLoggingCustomLog4JEnabled(Boolean loggingCustomLog4JEnabled) {
    this.loggingCustomLog4JEnabled = loggingCustomLog4JEnabled;
    return this;
  }

  public Boolean getMonitoringEnabled() {
    return monitoringEnabled;
  }

  public Application setMonitoringEnabled(Boolean monitoringEnabled) {
    this.monitoringEnabled = monitoringEnabled;
    return this;
  }

  public Boolean getMonitoringAutoRestart() {
    return monitoringAutoRestart;
  }

  public Application setMonitoringAutoRestart(Boolean monitoringAutoRestart) {
    this.monitoringAutoRestart = monitoringAutoRestart;
    return this;
  }

  public Boolean getStaticIPsEnabled() {
    return staticIPsEnabled;
  }

  public Application setStaticIPsEnabled(Boolean staticIPsEnabled) {
    this.staticIPsEnabled = staticIPsEnabled;
    return this;
  }

  public Boolean getMultitenanted() {
    return multitenanted;
  }

  public Application setMultitenanted(Boolean multitenanted) {
    this.multitenanted = multitenanted;
    return this;
  }

  public Boolean getHasFile() {
    return hasFile;
  }

  public Application setHasFile(Boolean hasFile) {
    this.hasFile = hasFile;
    return this;
  }

  public Boolean getSecureDataGatewayEnabled() {
    return secureDataGatewayEnabled;
  }

  public Application setSecureDataGatewayEnabled(Boolean secureDataGatewayEnabled) {
    this.secureDataGatewayEnabled = secureDataGatewayEnabled;
    return this;
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

  public Application setTrackingSettings(Map<String, String> trackingSettings) {
    this.trackingSettings = trackingSettings;
    return this;
  }

  public MuleVersion getMuleVersion() {
    return muleVersion;
  }

  public Application setMuleVersion(MuleVersion muleVersion) {
    this.muleVersion = muleVersion;
    return this;
  }

  public MuleVersion getPreviousMuleVersion() {
    return previousMuleVersion;
  }

  public Application setPreviousMuleVersion(MuleVersion previousMuleVersion) {
    this.previousMuleVersion = previousMuleVersion;
    return this;
  }

  public List<MuleVersion> getSupportedVersions() {
    return supportedVersions;
  }

  public Application setSupportedVersions(List<MuleVersion> supportedVersions) {
    this.supportedVersions = supportedVersions;
    return this;
  }

  public Workers getWorkers() {
    return workers;
  }

  public Application setWorkers(Workers workers) {
    this.workers = workers;
    return this;
  }

  public Map<String, String> getVpnConfig() {
    return vpnConfig;
  }

  public Application setVpnConfig(Map<String, String> vpnConfig) {
    this.vpnConfig = vpnConfig;
    return this;
  }

  public String getRegion() {
    return region;
  }

  public Application setRegion(String region) {
    this.region = region;
    return this;
  }

  public Boolean getObjectStoreV1() {
    return objectStoreV1;
  }

  public Application setObjectStoreV1(Boolean objectStoreV1) {
    this.objectStoreV1 = objectStoreV1;
    return this;
  }
}

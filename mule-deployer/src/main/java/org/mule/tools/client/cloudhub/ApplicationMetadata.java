/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.cloudhub;

import org.mule.tools.model.anypoint.CloudHubDeployment;

import java.util.Map;
import java.util.Optional;

/**
 * Represents all the metadata relative to a application being deployed to CloudHub.
 */
public class ApplicationMetadata {

  private String name;
  private String region;
  private Optional<String> muleVersion;
  private Integer workers;
  private String workerType;
  private Map<String, String> properties;

  public ApplicationMetadata(CloudHubDeployment deployment) {
    this.setName(deployment.getApplicationName())
        .setRegion(deployment.getRegion())
        .setMuleVersion(deployment.getMuleVersion())
        .setWorkers(deployment.getWorkers().get())
        .setWorkerType(deployment.getWorkerType())
        .setProperties(deployment.getProperties());
  }

  public ApplicationMetadata setName(String name) {
    this.name = name;
    return this;
  }

  public ApplicationMetadata setRegion(String region) {
    this.region = region;
    return this;
  }

  public ApplicationMetadata setMuleVersion(Optional<String> muleVersion) {
    this.muleVersion = muleVersion;
    return this;
  }

  public ApplicationMetadata setWorkers(Integer workers) {
    this.workers = workers;
    return this;
  }

  public ApplicationMetadata setWorkerType(String workerType) {
    this.workerType = workerType;
    return this;
  }

  public ApplicationMetadata setProperties(Map<String, String> properties) {
    this.properties = properties;
    return this;
  }

  public String getName() {
    return name;
  }

  public String getRegion() {
    return region;
  }

  public Optional<String> getMuleVersion() {
    return muleVersion;
  }

  public Integer getWorkers() {
    return workers;
  }

  public String getWorkerType() {
    return workerType;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void updateValues(Application currentApplication) {
    region = region == null ? currentApplication.region : region;
    muleVersion = !muleVersion.isPresent() ? Optional.ofNullable(currentApplication.muleVersion) : muleVersion;
    workers = workers == null ? currentApplication.workers : workers;
    workerType = workerType == null ? currentApplication.workerType : workerType;
  }
}

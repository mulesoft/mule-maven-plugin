/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.model.anypoint;

import org.apache.maven.plugins.annotations.Parameter;
import org.mule.tools.client.cloudhub.model.LogLevelInfo;
import org.mule.tools.client.core.exception.DeploymentException;

import java.util.List;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class CloudHubDeployment extends AnypointDeployment {

  @Parameter
  protected Integer workers;

  @Parameter
  protected String workerType;

  @Parameter
  protected String region;

  @Parameter
  protected Boolean overrideProperties;

  // the default value will be handled in the Deployer depending if the environment supports OSv1 or not
  @Parameter
  protected Boolean objectStoreV2 = null;

  @Parameter
  protected Boolean persistentQueues = false;

  @Parameter
  protected Boolean disableCloudHubLogs = false;

  @Parameter
  protected Integer waitBeforeValidation = 6000;

  @Parameter
  protected Boolean applyLatestRuntimePatch = false;

  @Parameter
  protected List<LogLevelInfo> logLevelInfos;

  /**
   * Region to deploy the application in Cloudhub.
   *
   * @since 2.0
   */
  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  /**
   * Number of workers for the deploymentConfiguration of the application in Cloudhub.
   *
   * @since 2.0
   */
  public Integer getWorkers() {
    return workers;
  }

  public void setWorkers(Integer workers) {
    this.workers = workers;
  }

  /**
   * Type of workers for the deploymentConfiguration of the application in Cloudhub.
   *
   * @since 2.0
   */
  public String getWorkerType() {
    return workerType;
  }

  public void setWorkerType(String workerType) {
    this.workerType = workerType;
  }

  /**
   * Define object store version of the application in Cloudhub.
   *
   * @since 3.3.3
   */
  public Boolean getObjectStoreV2() {
    return objectStoreV2;
  }

  public void setObjectStoreV2(Boolean objectStoreV2) {
    this.objectStoreV2 = objectStoreV2;
  }

  /**
   * Define object store version of the application in Cloudhub.
   *
   * @since 3.3.3
   */
  public Boolean getPersistentQueues() {
    return persistentQueues;
  }

  public void setPersistentQueues(Boolean persistentQueues) {
    this.persistentQueues = persistentQueues;
  }

  public Boolean getDisableCloudHubLogs() {
    return disableCloudHubLogs;
  }

  public void setDisableCloudHubLogs(Boolean disableCloudHubLogs) {
    this.disableCloudHubLogs = disableCloudHubLogs;
  }

  public Integer getWaitBeforeValidation() {
    return waitBeforeValidation;
  }

  public void setWaitBeforeValidation(Integer time) {
    this.waitBeforeValidation = time;
  }

  /**
   * Apply latest runtime patch in Cloudhub.
   *
   * @since 3.4.0
   */
  public Boolean getApplyLatestRuntimePatch() {
    return applyLatestRuntimePatch;
  }

  public void setApplyLatestRuntimePatch(Boolean applyLatestRuntimePatch) {
    this.applyLatestRuntimePatch = applyLatestRuntimePatch;
  }

  /**
   * Logging configuration to set in Cloudhub.
   */
  public List<LogLevelInfo> getLogLevelInfos() {
    return logLevelInfos;
  }

  public void setLogLevelInfos(List<LogLevelInfo> logLevelInfos) {
    this.logLevelInfos = logLevelInfos;
  }

  public void setEnvironmentSpecificValues() throws DeploymentException {
    super.setEnvironmentSpecificValues();

    String cloudHubWorkers = getProperty("cloudhub.workers");
    if (isNotBlank(cloudHubWorkers)) {
      setWorkers(Integer.valueOf(cloudHubWorkers));
    }

    String cloudHubWorkerType = getProperty("cloudhub.workerType");
    if (isNotBlank(cloudHubWorkerType)) {
      setWorkerType(cloudHubWorkerType);
    }
  }

  public boolean overrideProperties() {
    return overrideProperties == null ? true : overrideProperties;
  }
}

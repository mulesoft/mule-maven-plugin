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
import org.mule.tools.client.core.exception.DeploymentException;

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

  @Parameter
  protected Boolean objectStoreV2 = false;

  @Parameter
  protected Boolean persistentQueues = false;

  @Parameter
  protected Integer waitBeforeValidation = 6000;

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

  public Integer getWaitBeforeValidation() {
    return waitBeforeValidation;
  }

  public void setWaitBeforeValidation(Integer time) {
    this.waitBeforeValidation = time;
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

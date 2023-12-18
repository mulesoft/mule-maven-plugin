/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model.anypoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.core.exception.DeploymentException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

public class CloudHubDeploymentTest {

  private CloudHubDeployment deploymentSpy;

  @BeforeEach
  public void setUp() {
    deploymentSpy = spy(CloudHubDeployment.class);
  }

  @Test
  public void setCloudHubDeploymentDefaultValuesCloudHubWorkersSetSystemPropertyTest() throws DeploymentException {
    String cloudHubWorkers = "10";
    System.setProperty("cloudhub.workers", cloudHubWorkers);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.getWorkers()).describedAs("The cloudhub workers was not resolved by system property")
        .isEqualTo(Integer.valueOf(cloudHubWorkers));
    System.clearProperty("cloudhub.workers");
  }

  @Test
  public void setCloudHubDeploymentDefaultValuesCloudHubWorkerTypeSetSystemPropertyTest() throws DeploymentException {
    String cloudHubWorkerType = "worker-type";
    System.setProperty("cloudhub.workerType", cloudHubWorkerType);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.getWorkerType()).describedAs("The cloudhub worker type property was not resolved by system property")
        .isEqualTo(cloudHubWorkerType);
    System.clearProperty("cloudhub.workerType");
  }

  @Test
  public void defaultOsV2ValueIsNull() {
    assertThat(deploymentSpy.getObjectStoreV2()).describedAs("The default value for Object Store v2 property is not null")
        .isNull();
  }

  @Test
  public void defaultPersistentQueuesValueIsFalse() {
    assertThat(deploymentSpy.getPersistentQueues()).describedAs("The default value for Persistent Queues property is not false")
        .isFalse();
  }

  @Test
  public void defaultDisableCloudHubLogsValueIsFalse() {
    assertThat(deploymentSpy.getDisableCloudHubLogs()).describedAs("Custom Log4J property is not null")
        .isNull();
  }

  @Test
  public void defaultWaitBeforeValidationIsZero() {
    assertThat(deploymentSpy.getWaitBeforeValidation()).describedAs("The default value for pepe is not zero").isEqualTo(6000);
  }

  @Test
  public void defaultApplyLatestRuntimePatch() {
    assertThat(deploymentSpy.getApplyLatestRuntimePatch()).describedAs("The default value for apply patch property must be false")
        .isFalse();
  }
}

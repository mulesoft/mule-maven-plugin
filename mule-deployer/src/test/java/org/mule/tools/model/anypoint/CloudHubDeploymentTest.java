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

import org.junit.Before;
import org.junit.Test;
import org.mule.tools.client.core.exception.DeploymentException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.spy;

public class CloudHubDeploymentTest {

  private CloudHubDeployment deploymentSpy;

  @Before
  public void setUp() {
    deploymentSpy = spy(CloudHubDeployment.class);
  }

  @Test
  public void setCloudHubDeploymentDefaultValuesCloudHubWorkersSetSystemPropertyTest() throws DeploymentException {
    String cloudHubWorkers = "10";
    System.setProperty("cloudhub.workers", cloudHubWorkers);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat("The cloudhub workers was not resolved by system property",
               deploymentSpy.getWorkers(), equalTo(Integer.valueOf(cloudHubWorkers)));
    System.clearProperty("cloudhub.workers");
  }

  @Test
  public void setCloudHubDeploymentDefaultValuesCloudHubWorkerTypeSetSystemPropertyTest() throws DeploymentException {
    String cloudHubWorkerType = "worker-type";
    System.setProperty("cloudhub.workerType", cloudHubWorkerType);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat("The cloudhub worker type property was not resolved by system property",
               deploymentSpy.getWorkerType(), equalTo(cloudHubWorkerType));
    System.clearProperty("cloudhub.workerType");
  }

  @Test
  public void defaultOsV2ValueIsFalse() {
    assertThat("The default value for Object Store v2 property is not false",
               deploymentSpy.getObjectStoreV2(), equalTo(false));
  }

  @Test
  public void defaultPersistentQueuesValueIsFalse() {
    assertThat("The default value for Persistent Queues property is not false",
               deploymentSpy.getPersistentQueues(), equalTo(false));
  }

  @Test
  public void defaultWaitBeforeValidationIsZero() {
    assertThat("The default value for pepe is not zero",
               deploymentSpy.getWaitBeforeValidation(), equalTo(6000));
  }
}

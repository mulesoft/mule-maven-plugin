/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model.standalone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.core.exception.DeploymentException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

public class ClusterDeploymentTest {

  private static final String MULE_HOME = "mule_home";

  private ClusterDeployment deploymentSpy;

  @BeforeEach
  public void setUp() {
    deploymentSpy = spy(ClusterDeployment.class);
  }

  @Test
  public void setClusterDeploymentValuesSizeNotSetTest() throws DeploymentException {
    System.setProperty("mule.home", MULE_HOME);

    Integer clusterDefaultSize = 2;
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.getSize()).describedAs("The cluster size was not resolved to the default value")
        .isEqualTo(clusterDefaultSize);
    System.clearProperty("mule.home");

  }
}

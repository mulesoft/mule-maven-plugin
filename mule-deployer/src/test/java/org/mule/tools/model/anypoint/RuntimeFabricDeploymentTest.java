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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

public class RuntimeFabricDeploymentTest {

  private static final String TARGET_NAME = "target";
  public static final String PROVIDER = "SERVER";
  private RuntimeFabricOnPremiseDeployment fabricDeployment;

  private RuntimeFabricOnPremiseDeploymentSettings deploymentSettingsMock;

  @BeforeEach
  public void setUp() {
    fabricDeployment = new RuntimeFabricOnPremiseDeployment();
    fabricDeployment.setTarget(TARGET_NAME);
    fabricDeployment.setProvider(PROVIDER);
    deploymentSettingsMock = mock(RuntimeFabricOnPremiseDeploymentSettings.class);
    fabricDeployment.setDeploymentSettings(deploymentSettingsMock);
  }

  @Test
  public void getTarget() {
    assertThatThrownBy(() -> {
      fabricDeployment.setTarget(null);
      fabricDeployment.setEnvironmentSpecificValues();
    }).isExactlyInstanceOf(DeploymentException.class);
  }

  @Test
  public void getProvider() {
    assertThatThrownBy(() -> {
      fabricDeployment.setProvider(null);
      fabricDeployment.setEnvironmentSpecificValues();
    }).isExactlyInstanceOf(DeploymentException.class);
  }

  @Test
  public void validReplicasAndEnableClusterDefaultConfiguration() throws DeploymentException {
    fabricDeployment.setEnvironmentSpecificValues();
  }

}

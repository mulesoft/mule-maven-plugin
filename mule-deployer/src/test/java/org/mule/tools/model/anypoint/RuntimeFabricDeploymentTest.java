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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.core.exception.DeploymentException;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
    assertThrows(DeploymentException.class, () -> {
      fabricDeployment.setTarget(null);
      fabricDeployment.setEnvironmentSpecificValues();
    });
  }

  @Test
  public void getProvider() {
    assertThrows(DeploymentException.class, () -> {
      fabricDeployment.setProvider(null);
      fabricDeployment.setEnvironmentSpecificValues();
    });
  }

  @Test
  public void validReplicasAndEnableClusterDefaultConfiguration() throws DeploymentException {
    fabricDeployment.setEnvironmentSpecificValues();
  }

}

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.client.core.exception.DeploymentException;
import static org.junit.rules.ExpectedException.none;
import static org.powermock.api.mockito.PowerMockito.mock;

public class RuntimeFabricDeploymentTest {

  private static final String TARGET_NAME = "target";
  public static final String PROVIDER = "SERVER";
  private RuntimeFabricDeployment fabricDeployment;


  @Rule
  public ExpectedException expectedException = none();
  private RuntimeFabricDeploymentSettings deploymentSettingsMock;

  @Before
  public void setUp() {
    fabricDeployment = new RuntimeFabricDeployment();
    fabricDeployment.setTarget(TARGET_NAME);
    fabricDeployment.setProvider(PROVIDER);
    deploymentSettingsMock = mock(RuntimeFabricDeploymentSettings.class);
    fabricDeployment.setDeploymentSettings(deploymentSettingsMock);
  }

  @Test
  public void getTarget() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    expectedException.expectMessage("missing target value");
    fabricDeployment.setTarget(null);
    fabricDeployment.setEnvironmentSpecificValues();
  }

  @Test
  public void getProvider() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    expectedException.expectMessage("Please set the provider as MC, CLUSTER or SERVER");
    fabricDeployment.setProvider(null);
    fabricDeployment.setEnvironmentSpecificValues();
  }

  @Test
  public void validReplicasAndEnableClusterDefaultConfiguration() throws DeploymentException {
    fabricDeployment.setEnvironmentSpecificValues();
  }

}

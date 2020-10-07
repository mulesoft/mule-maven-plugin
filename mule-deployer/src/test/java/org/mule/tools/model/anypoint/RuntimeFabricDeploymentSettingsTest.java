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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.rules.ExpectedException.none;

public class RuntimeFabricDeploymentSettingsTest {

  public static final String CORES = "0.1";
  public static final String MEMORY = "0.5";
  public static final int REPLICAS = 1;
  public static final boolean ENABLE_RUNTIME_CLUSTER_MODE = false;
  public static final String PROVIDER = "SERVER";
  public static final String CPU_RESERVED = "500m";
  public static final String CPU_LIMIT = "500m";
  public static final String MEMORY_RESERVED = "700Mi";
  public static final String MEMORY_LIMIT = "700Mi";
  public static final String PUBLIC_URL = "www.pepe.com";
  public static final String DEFAULT_UPDATE_STRATEGY = "rolling";
  private RuntimeFabricDeploymentSettings deploymentSettings;


  @Rule
  public ExpectedException expectedException = none();

  @Before
  public void setUp() {
    deploymentSettings = new RuntimeFabricDeploymentSettings();
    deploymentSettings.setRuntimeVersion("4.1.3");
    deploymentSettings.getHttp().getInbound().setPublicUrl(PUBLIC_URL);
    deploymentSettings.setLastMileSecurity(true);
    // These values are injected by Maven
    deploymentSettings.setClustered(ENABLE_RUNTIME_CLUSTER_MODE);
  }


  @Test
  public void validMultipleReplicasAndEnableClusterDefaultTrueConfiguration() throws DeploymentException {

    deploymentSettings.setClustered(true);
    deploymentSettings.setEnvironmentSpecificValues();
  }



  @Test
  public void undefinedCPUMaxUseCPUReservedValue() throws DeploymentException {

    deploymentSettings.setEnvironmentSpecificValues();
    assertThat("The cpu limit value is not the same as cpu reserved", deploymentSettings.getResources().getCpu().getLimit(),
               equalTo(deploymentSettings.getResources().getCpu().getReserved()));
  }


  @Test
  public void setCPUMaxValue() throws DeploymentException {
    deploymentSettings.getResources().getCpu().setLimit(CPU_LIMIT);
    deploymentSettings.setEnvironmentSpecificValues();
    assertThat("The cpu limit value is not right", deploymentSettings.getResources().getCpu().getLimit(), equalTo(CPU_LIMIT));
  }

  @Test
  public void undefinedMemoryMaxUseMemoryReservedValue() throws DeploymentException {
    deploymentSettings.setEnvironmentSpecificValues();
    assertThat("The memory limit value is not the same as memory reserved",
               deploymentSettings.getResources().getMemory().getLimit(),
               equalTo(deploymentSettings.getResources().getMemory().getReserved()));
  }

  @Test
  public void setMemoryMaxValue() throws DeploymentException {
    deploymentSettings.getResources().getMemory().setLimit(MEMORY_LIMIT);
    deploymentSettings.setEnvironmentSpecificValues();
    assertThat("The memory limit value is not right", deploymentSettings.getResources().getMemory().getLimit(),
               equalTo(MEMORY_LIMIT));
  }

}

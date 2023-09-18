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
  private RuntimeFabricOnPremiseDeploymentSettings deploymentSettings;

  @BeforeEach
  public void setUp() {
    deploymentSettings = new RuntimeFabricOnPremiseDeploymentSettings();
    deploymentSettings.setRuntimeVersion("4.1.3");
    deploymentSettings.getHttp().getInbound().setPublicUrl(PUBLIC_URL);
    deploymentSettings.setLastMileSecurity(true);
    deploymentSettings.setGenerateDefaultPublicUrl(true);
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
    assertThat(deploymentSettings.getResources().getCpu().getLimit())
        .describedAs("The cpu limit value is not the same as cpu reserved")
        .isEqualTo(deploymentSettings.getResources().getCpu().getReserved());
  }


  @Test
  public void setCPUMaxValue() throws DeploymentException {
    deploymentSettings.getResources().getCpu().setLimit(CPU_LIMIT);
    deploymentSettings.setEnvironmentSpecificValues();
    assertThat(deploymentSettings.getResources().getCpu().getLimit()).describedAs("The cpu limit value is not right")
        .isEqualTo(CPU_LIMIT);
  }

  @Test
  public void undefinedMemoryMaxUseMemoryReservedValue() throws DeploymentException {
    deploymentSettings.setEnvironmentSpecificValues();
    assertThat(deploymentSettings.getResources().getMemory().getLimit())
        .describedAs("The memory limit value is not the same as memory reserved")
        .isEqualTo(deploymentSettings.getResources().getMemory().getReserved());
  }

  @Test
  public void setMemoryMaxValue() throws DeploymentException {
    deploymentSettings.getResources().getMemory().setLimit(MEMORY_LIMIT);
    deploymentSettings.setEnvironmentSpecificValues();
    assertThat(deploymentSettings.getResources().getMemory().getLimit()).describedAs("The memory limit value is not right")
        .isEqualTo(MEMORY_LIMIT);
  }

}

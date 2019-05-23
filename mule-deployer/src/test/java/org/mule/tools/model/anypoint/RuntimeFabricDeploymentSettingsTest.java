/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
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
  public static final String CPU_RESERVED = "1";
  public static final String CPU_MAX = "2";
  public static final String MEMORY_RESERVED = "1";
  public static final String MEMORY_MAX = "3";
  public static final String PUBLIC_URL = "www.pepe.com";
  private RuntimeFabricDeploymentSettings deploymentSettings;


  @Rule
  public ExpectedException expectedException = none();

  @Before
  public void setUp() {
    deploymentSettings = new RuntimeFabricDeploymentSettings();
    deploymentSettings.setCpuReserved(CORES);
    deploymentSettings.setMemoryReserved(MEMORY);
    deploymentSettings.setRuntimeVersion("4.1.3");
    deploymentSettings.setCpuReserved(CPU_RESERVED);
    deploymentSettings.setMemoryReserved(MEMORY_RESERVED);
    deploymentSettings.setPublicUrl(PUBLIC_URL);
    deploymentSettings.setLastMileSecurity(true);
    // These values are injected by Maven
    deploymentSettings.setReplicationFactor(REPLICAS);
    deploymentSettings.setClusteringEnabled(ENABLE_RUNTIME_CLUSTER_MODE);
  }

  @Test
  public void getReplicas() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    expectedException.expectMessage("replicas must be bigger than 1 to enable Runtime Cluster Mode");
    deploymentSettings.setClusteringEnabled(true);
    deploymentSettings.setEnvironmentSpecificValues();
  }

  @Test
  public void validMultipleReplicasAndEnableClusterDefaultTrueConfiguration() throws DeploymentException {
    deploymentSettings.setReplicationFactor(2);
    deploymentSettings.setClusteringEnabled(true);
    deploymentSettings.setEnvironmentSpecificValues();
  }

  @Test
  public void undefinedCPUMaxUseCPUReservedValue() throws DeploymentException {
    deploymentSettings.setEnvironmentSpecificValues();
    assertThat("The CpuMax value is not the same as CpuReserved", deploymentSettings.getCpuMax(),
               equalTo(deploymentSettings.getCpuReserved()));
  }

  @Test
  public void setCPUMaxValue() throws DeploymentException {
    deploymentSettings.setCpuMax(CPU_MAX);
    deploymentSettings.setEnvironmentSpecificValues();
    assertThat("The CpuMax value is not right", deploymentSettings.getCpuMax(), equalTo(CPU_MAX));
  }

  @Test
  public void undefinedMemoryMaxUseMemoryReservedValue() throws DeploymentException {
    deploymentSettings.setEnvironmentSpecificValues();
    assertThat("The MemoryMax value is not the same as MemoryReserved", deploymentSettings.getMemoryMax(),
               equalTo(deploymentSettings.getMemoryReserved()));
  }

  @Test
  public void setMemoryMaxValue() throws DeploymentException {
    deploymentSettings.setMemoryMax(MEMORY_MAX);
    deploymentSettings.setEnvironmentSpecificValues();
    assertThat("The MemoryMax value is not right", deploymentSettings.getMemoryMax(), equalTo(MEMORY_MAX));
  }

}

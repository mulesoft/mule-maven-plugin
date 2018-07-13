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

public class RuntimeFabricDeploymentTest {

  public static final double CORES = 0.1;
  private static final String TARGET_NAME = "target";
  public static final double MEMORY = 0.5;
  public static final int REPLICAS = 1;
  public static final boolean ENABLE_RUNTIME_CLUSTER_MODE = false;
  private RuntimeFabricDeployment fabricDeployment;

  @Rule
  public ExpectedException expectedException = none();

  @Before
  public void setUp() {
    fabricDeployment = new RuntimeFabricDeployment();
    fabricDeployment.setTarget(TARGET_NAME);
    fabricDeployment.setCores(CORES);
    fabricDeployment.setMemory(MEMORY);
    // These values are injected by Maven
    fabricDeployment.setReplicas(REPLICAS);
    fabricDeployment.setEnableRuntimeClusterMode(ENABLE_RUNTIME_CLUSTER_MODE);
  }

  @Test
  public void getTarget() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    expectedException.expectMessage("missing target value");
    fabricDeployment.setTarget(null);
    fabricDeployment.setEnvironmentSpecificValues();
  }

  @Test
  public void getReplicas() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    expectedException.expectMessage("replicas must be bigger than 1 to enable Runtime Cluster Mode");
    fabricDeployment.setEnableRuntimeClusterMode(true);
    fabricDeployment.setEnvironmentSpecificValues();
  }

  @Test
  public void getCores() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    expectedException.expectMessage("Please set the number of cores in vCPU");
    fabricDeployment.setCores(null);
    fabricDeployment.setEnvironmentSpecificValues();
  }

  @Test
  public void getMemory() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    expectedException.expectMessage("Please set the amount of memory in GB");
    fabricDeployment.setMemory(null);
    fabricDeployment.setEnvironmentSpecificValues();
  }

  @Test
  public void validReplicasAndEnableClusterDefaultConfiguration() throws DeploymentException {
    fabricDeployment.setEnvironmentSpecificValues();
  }

  @Test
  public void validMultipleReplicasAndEnableClusterDefaultTrueConfiguration() throws DeploymentException {
    fabricDeployment.setReplicas(2);
    fabricDeployment.setEnableRuntimeClusterMode(true);
    fabricDeployment.setEnvironmentSpecificValues();
  }

}

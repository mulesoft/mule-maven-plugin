/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.utils;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.client.agent.AgentDeployer;
import org.mule.tools.client.arm.ArmDeployer;
import org.mule.tools.client.cloudhub.CloudhubDeployer;
import org.mule.tools.client.standalone.deployment.StandaloneDeployer;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.model.standalone.ClusterDeployment;
import org.mule.tools.model.standalone.StandaloneDeployment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class DeployerFactoryTest {

  public DeployerFactory factorySpy = spy(new DeployerFactory());
  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Before
  public void setUp() throws DeploymentException {
    doNothing().when(factorySpy).initializeDeployer(any());
  }

  @Test
  public void createStandaloneDeployerTest() throws DeploymentException {
    assertThat("Deployer is not the expected", factorySpy.createDeployer(new StandaloneDeployment(), mock(DeployerLog.class)),
               new IsInstanceOf(StandaloneDeployer.class));
  }

  @Test
  public void createArmDeployerTest() throws DeploymentException {
    assertThat("Deployer is not the expected", factorySpy.createDeployer(new ArmDeployment(), mock(DeployerLog.class)),
               new IsInstanceOf(ArmDeployer.class));
  }

  @Test
  public void createCloudHubDeployerTest() throws DeploymentException {
    assertThat("Deployer is not the expected", factorySpy.createDeployer(new CloudHubDeployment(), mock(DeployerLog.class)),
               new IsInstanceOf(CloudhubDeployer.class));
  }

  @Test
  public void createAgentDeployerTest() throws DeploymentException {
    assertThat("Deployer is not the expected", factorySpy.createDeployer(new AgentDeployment(), mock(DeployerLog.class)),
               new IsInstanceOf(AgentDeployer.class));
  }

  @Test
  public void createClusterDeployerTest() throws DeploymentException {
    expected.expect(DeploymentException.class);
    expected.expectMessage("Unsupported deploymentConfiguration type: org.mule.tools.model.standalone.ClusterDeployment");
    factorySpy.createDeployer(new ClusterDeployment(), mock(DeployerLog.class));
  }
}

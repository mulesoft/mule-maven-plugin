/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation.agent;

import org.junit.jupiter.api.Test;
import org.mule.tools.client.agent.AgentClient;
import org.mule.tools.client.agent.AgentInfo;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.validation.AbstractDeploymentValidator;
import org.mule.tools.validation.EnvironmentSupportedVersions;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.hamcrest.MatcherAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AgentDeploymentValidatorTest {

  private static final String MULE_VERSION = "4.0.0";
  private static final EnvironmentSupportedVersions EXPECTED_ENVIRONMENT_SUPPORTED_VERSIONS =
      new EnvironmentSupportedVersions(MULE_VERSION);
  private static final String CLIENT_URI = "http://localhost:9999/";
  private static final DeployerLog LOG_MOCK = mock(DeployerLog.class);
  private AbstractDeploymentValidator validatorSpy;

  @Test
  public void getEnvironmentSupportedVersionsTest() throws Exception {
    validatorSpy = spy(new AgentDeploymentValidator(new AgentDeployment()));

    AgentInfo agentInfo = new AgentInfo();
    agentInfo.setMuleVersion(MULE_VERSION);
    AgentDeployment deploymentMock = mock(AgentDeployment.class);
    when(deploymentMock.getUri()).thenReturn(CLIENT_URI);
    AgentClient clientSpy = spy(new AgentClient(LOG_MOCK, deploymentMock));
    doReturn(agentInfo).when(clientSpy).getAgentInfo();

    doReturn(clientSpy).when((AgentDeploymentValidator) validatorSpy).getAgentClient();

    assertThat(validatorSpy.getEnvironmentSupportedVersions())
        .describedAs("Supported version that was generated is not the expected")
        .isEqualTo(EXPECTED_ENVIRONMENT_SUPPORTED_VERSIONS);

  }
}

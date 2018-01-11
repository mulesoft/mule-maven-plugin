/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.agent;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mule.tools.model.agent.AgentDeployment;

import static org.mockito.Mockito.*;
import static org.mule.tools.client.agent.AgentClient.APPLICATIONS_PATH;

public class AgentClientTestCase {

  private static final File APP_FILE = new File("/tmp/echo-test4.zip");
  private static final String BASE_URI = "http://localhost:9999/";
  private AgentClient agentClient;
  private AgentDeployment deploymentMock;
  private AgentClient agentClientSpy;
  private static final String APP_NAME = "fake-name";

  @Before
  public void setup() {
    deploymentMock = mock(AgentDeployment.class);
    when(deploymentMock.getUri()).thenReturn(BASE_URI);
    when(deploymentMock.getApplicationName()).thenReturn(APP_NAME);
    agentClient = new AgentClient(null, deploymentMock);
    agentClientSpy = spy(agentClient);
  }

  @Test
  public void deployApplication() {
    doNothing().when(agentClientSpy).deployArtifact(APP_NAME, APP_FILE, APPLICATIONS_PATH);
    agentClientSpy.deployApplication(APP_NAME, APP_FILE);
    verify(agentClientSpy, times(1)).deployArtifact(APP_NAME, APP_FILE, APPLICATIONS_PATH);
  }

  @Test
  public void undeployApplication() {
    doNothing().when(agentClientSpy).undeployArtifact(APP_NAME, APPLICATIONS_PATH);
    agentClientSpy.undeployApplication(APP_NAME);
    verify(agentClientSpy, times(1)).undeployArtifact(APP_NAME, APPLICATIONS_PATH);
  }

}

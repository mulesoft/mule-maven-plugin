/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.verification.arm;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.client.arm.model.Application;
import org.mule.tools.client.arm.model.Data;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.ArmDeployment;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArmDeploymentVerificationTest {

  private static final String APP_NAME = "app";
  private static final int APP_ID = 0;
  private ArmClient clientMock;
  private Application application;
  private ArmDeploymentVerification verification;
  private Deployment deployment;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    clientMock = mock(ArmClient.class);
    application = new Application();
    application.data = new Data();
    deployment = new ArmDeployment();
    deployment.setApplicationName(APP_NAME);
    verification = new ArmDeploymentVerification(clientMock, APP_ID);
    when(clientMock.getApplication(anyInt())).thenReturn(application);
    when(clientMock.isStarted(anyInt())).thenCallRealMethod();
  }

  @Test
  public void assertDeploymentStartedTrue() throws DeploymentException {
    application.data.lastReportedStatus = "STARTED";
    application.data.desiredStatus = "STARTED";
    verification.assertDeployment(deployment); // Should pass without throwing exception
  }

  @Test
  public void assertDeploymentStartedFalse() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    application.data.desiredStatus = "UPDATED";
    deployment.setDeploymentTimeout(1000L);
    verification.assertDeployment(deployment);
  }

  @Test
  public void assertDeploymentFailed() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    expectedException.expectMessage("Deployment has failed");
    application.data.lastReportedStatus = "FAILED";
    application.data.desiredStatus = "STARTED";
    verification.assertDeployment(deployment);
  }
}

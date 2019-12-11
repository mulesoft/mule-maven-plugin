/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.verification.cloudhub;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CloudHubDeploymentVerificationTest {

  private static final String APP_NAME = "app";
  private CloudHubClient clientMock;
  private Application application;
  private CloudHubDeploymentVerification verification;
  private Deployment deployment;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    clientMock = mock(CloudHubClient.class);
    application = new Application();
    deployment = new CloudHubDeployment();
    deployment.setApplicationName(APP_NAME);
    verification = new CloudHubDeploymentVerification(clientMock);
    when(clientMock.getApplications(anyString())).thenReturn(application);
  }

  @Test
  public void assertDeploymentStartedTrue() throws DeploymentException {
    application.setStatus("STARTED");
    verification.assertDeployment(deployment); // Should pass without throwing exception
  }

  @Test
  public void assertReDeploymentStartedTrue() throws DeploymentException {
    application.setStatus("STARTED");
    verification.assertDeployment(deployment); // Should pass without throwing exception
  }

  @Test
  public void assertReDeploymentStartedFalse() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    expectedException.expectMessage("Validation timed out waiting for application to start. " +
        "Please consider increasing the deploymentTimeout property.");
    application.setStatus("STARTED");
    application.setDeploymentUpdateStatus("DEPLOYING");
    deployment.setDeploymentTimeout(1000L);
    verification.assertDeployment(deployment); // Should pass without throwing exception
  }

  @Test
  public void assertDeploymentStartedFalse() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    expectedException.expectMessage("Validation timed out waiting for application to start. " +
        "Please consider increasing the deploymentTimeout property.");
    application.setStatus("DEPLOYING");
    deployment.setDeploymentTimeout(1000L);
    verification.assertDeployment(deployment);
  }

  @Test
  public void assertDeploymentFailed() throws DeploymentException {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("Deployment failed");
    application.setStatus("FAILED");
    verification.assertDeployment(deployment);
  }
}

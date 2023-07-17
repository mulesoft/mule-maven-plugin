/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.verification.cloudhub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CloudHubDeploymentVerificationTest {

  private static final String APP_NAME = "app";
  private CloudHubClient clientMock;
  private Application application;
  private CloudHubDeploymentVerification verification;
  private Deployment deployment;



  @BeforeEach
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
  public void assertDeploymentStartedFalse() {
    assertThatThrownBy(() -> {
      application.setStatus("DEPLOYING");
      deployment.setDeploymentTimeout(1000L);
      verification.assertDeployment(deployment);
    }).isExactlyInstanceOf(DeploymentException.class)
            .hasMessageContaining("Validation timed out waiting for application to start. " +
                    "Please consider increasing the deploymentTimeout property.");
  }

  @Test
  public void assertDeploymentFailed() {
    assertThatThrownBy(() -> {
      application.setStatus("FAILED");
      verification.assertDeployment(deployment);
    }).isExactlyInstanceOf(DeploymentException.class)
            .hasMessageContaining("Deployment has failed");
  }

  @Test
  public void assertDeploymentSucessAfterFailure() {
    assertThatThrownBy(() -> {
      application.setStatus("FAILED");
      application.setDeploymentUpdateStatus("DEPLOYING");
      deployment.setDeploymentTimeout(1000L);
      verification.assertDeployment(deployment);
    }).isExactlyInstanceOf(DeploymentException.class)
            .hasMessageContaining("Validation timed out waiting for application to start. " +
                    "Please consider increasing the deploymentTimeout property.");
  }
}

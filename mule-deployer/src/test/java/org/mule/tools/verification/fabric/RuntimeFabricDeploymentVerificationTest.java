/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.verification.fabric;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.fabric.RuntimeFabricClient;
import org.mule.tools.client.fabric.model.DeploymentDetailedResponse;
import org.mule.tools.client.fabric.model.DeploymentGenericResponse;
import org.mule.tools.client.fabric.model.Deployments;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.RuntimeFabricDeployment;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuntimeFabricDeploymentVerificationTest {

  private static final String APP_NAME = "app";
  private RuntimeFabricClient clientMock;
  private RuntimeFabricDeploymentVerification verification;
  private Deployment deployment;
  private Deployments deployments;
  private DeploymentGenericResponse deploymentGenericResponse;
  private DeploymentDetailedResponse deploymentDetailedResponse;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    clientMock = mock(RuntimeFabricClient.class);
    deploymentDetailedResponse = new DeploymentDetailedResponse();
    deployment = new RuntimeFabricDeployment();
    deployment.setApplicationName(APP_NAME);
    deploymentGenericResponse = new DeploymentGenericResponse();
    deploymentGenericResponse.name = APP_NAME;
    deploymentGenericResponse.id = "1";
    deployments = new Deployments();
    deployments.items = newArrayList(deploymentGenericResponse);
    verification = new RuntimeFabricDeploymentVerification(clientMock);
    when(clientMock.getDeployment(anyString())).thenReturn(deploymentDetailedResponse);
    when(clientMock.getDeployments()).thenReturn(deployments);
  }

  @Test
  public void assertDeploymentAppliedTrue() throws DeploymentException {
    deploymentDetailedResponse.status = "APPLIED";
    verification.assertDeployment(deployment); // Should pass without throwing exception
  }

  @Test
  public void assertDeploymentStartedTrue() throws DeploymentException {
    deploymentDetailedResponse.status = "STARTED";
    verification.assertDeployment(deployment); // Should pass without throwing exception
  }

  @Test
  public void assertDeploymentStartedFalse() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    expectedException.expectMessage("Validation timed out waiting for application to start. " +
        "Please consider increasing the deploymentTimeout property.");
    deploymentDetailedResponse.status = "DEPLOYING";
    deployment.setDeploymentTimeout(1000L);
    verification.assertDeployment(deployment);
  }

  @Test
  public void assertDeploymentFailed() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    expectedException.expectMessage("Deployment has failed");
    deploymentDetailedResponse.status = "FAILED";
    verification.assertDeployment(deployment);
  }

}

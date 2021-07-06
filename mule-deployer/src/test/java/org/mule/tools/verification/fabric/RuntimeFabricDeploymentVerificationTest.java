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
import org.mule.tools.client.fabric.model.Target;
import org.mule.tools.model.anypoint.RuntimeFabricDeployment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuntimeFabricDeploymentVerificationTest {

  private static final String APP_NAME = "app";
  private RuntimeFabricClient clientMock;
  private RuntimeFabricDeploymentVerification verification;
  private RuntimeFabricDeployment deployment;
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
    deployment.setTarget("rtf-local-4");
    deploymentGenericResponse = new DeploymentGenericResponse();
    deploymentGenericResponse.name = APP_NAME;
    deploymentGenericResponse.id = "1";
    deploymentGenericResponse.target = new Target();
    deploymentGenericResponse.target.setTargetId("8d7959fc-5405-43e9-9339-6df8d1c8c1d7");
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

  @Test
  public void assertDeploymentWithTargetTrue() throws DeploymentException {
    String content =
        "{\"id\":\"8d7959fc-5405-43e9-9339-6df8d1c8c1d7\",\"region\":\"us-east-1\",\"name\":\"rtf-local-4\",\"organizationId\":\"96adae7f-fe4d-4cef-9bea-fa9004a47459\",\"status\":\"Active\",\"agentVersion\":\"1.8.50\",\"desiredAgentVersion\":\"1.8.50\",\"nodes\":[{\"uid\":\"a284920e-64d4-41e6-8a18-f237e5af2e54\",\"name\":\"kind-control-plane\",\"kubeletVersion\":\"v1.20.2\",\"dockerVersion\":\"containerd://1.4.0-106-gce4439a8\",\"role\":\"worker\",\"status\":{\"isHealthy\":true,\"isReady\":true,\"isSchedulable\":true},\"capacity\":{\"cpu\":8,\"cpuMillis\":8000,\"memory\":\"5948Mi\",\"memoryMi\":5948,\"pods\":110},\"allocatedRequestCapacity\":{\"cpu\":1,\"cpuMillis\":1250,\"memory\":\"690Mi\",\"memoryMi\":690,\"pods\":12},\"allocatedLimitCapacity\":{\"cpu\":2,\"cpuMillis\":2150,\"memory\":\"1740Mi\",\"memoryMi\":1740,\"pods\":12}}],\"secondsSinceHeartbeat\":0}";
    JsonParser jsonParser = new JsonParser();
    JsonObject jsonObject = (JsonObject) jsonParser.parse(content);
    JsonArray jsonArray = new JsonArray();
    jsonArray.add(jsonObject);
    when(clientMock.getTargets()).thenReturn(jsonArray);
    deploymentDetailedResponse.status = "STARTED";
    verification.assertDeployment(deployment);
  }

}

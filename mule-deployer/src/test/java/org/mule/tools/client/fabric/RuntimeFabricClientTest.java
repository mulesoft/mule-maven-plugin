/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.fabric;

import com.google.common.collect.Maps;
import com.google.common.net.MediaType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mule.tools.client.fabric.model.DeploymentDetailedResponse;
import org.mule.tools.client.fabric.model.DeploymentGenericResponse;
import org.mule.tools.client.fabric.model.Deployments;
import org.mule.tools.model.anypoint.RuntimeFabricDeployment;
import org.mule.tools.model.anypoint.RuntimeFabricOnPremiseDeployment;
import org.mule.tools.utils.DeployerLog;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mule.tools.client.fabric.RuntimeFabricClient.DEPLOYMENTS_PATH;

class RuntimeFabricClientTest {

  public static final String BASE_URI = "http://localhost:";
  public static final String GET = "GET";
  public static final String FAKE_ENV = "fakeEnv";
  public static final String FAKE_USER = "fakeUser";
  public static final String FAKE_PASSWORD = "fakePassword";
  public static final int DEFAULT_PORT = 0;
  public static final String DEPLOYMENTS_JSON = "deployments.json";
  public static final String DEPLOYMENT_ID = "7096496b-9407-4e67-9a38-8963c3727735";

  private ClientAndServer mockServer;

  private static final String ORG_ID = "abcdef";
  private static final String ENV_ID = "ghijkl";
  private int port = DEFAULT_PORT;

  @BeforeEach
  void startServer() {
    port = getFreePort();
    mockServer = startClientAndServer(port);
  }

  @AfterEach
  void stopServer() {
    mockServer.stop();
  }

  @Test
  void getDeployments() throws IOException {
    File deploymentsJson = new File(getClass().getClassLoader().getResource(
                                                                            DEPLOYMENTS_JSON)
        .getFile());
    List<String> contents = Files.readAllLines(deploymentsJson.toPath());

    mockServer.when(request().withMethod(GET).withPath(format(DEPLOYMENTS_PATH, ORG_ID, ENV_ID)))
        .respond(response().withStatusCode(200).withBody(String.join(System.lineSeparator(), contents), MediaType.JSON_UTF_8));

    RuntimeFabricClient client = buildClientSpy();

    Deployments deployments = client.getDeployments();

    Set<String> verifiedIds = newHashSet("73894e6e-d9c5-11e6-bf27-cec0c932ce01", "73894e6e-d9c5-11e6-bf27-cec0c932ce02");

    for (DeploymentGenericResponse response : deployments.items) {
      verifiedIds.remove(response.id);
    }

    assertThat(verifiedIds).as("Verified ids should be empty").isEmpty();
  }

  @Test
  @Disabled
  void getSingleDeploymentTest() throws IOException {
    validateDeployment("single-deployment.json", deploymentDetailedResponse -> {
      assertThat(deploymentDetailedResponse.application.configuration).isNotNull();
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.properties)
          .isNotNull();
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.properties.size())
          .isEqualTo(6);
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.secureProperties)
          .isNotNull();
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.secureProperties
          .size()).isEqualTo(7);
    });
  }

  @Test
  @Disabled
  void getSingleDeploymentTestWithEmptyPropertiesObject() throws IOException {
    validateDeployment("single-deployment-000.json", deploymentDetailedResponse -> {
      assertThat(deploymentDetailedResponse.application.configuration).isNotNull();
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.properties)
          .isNotNull();
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.properties.size())
          .isEqualTo(0);
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.secureProperties)
          .isNotNull();
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.secureProperties
          .size()).isEqualTo(0);
    });
  }

  @Test
  @Disabled
  void getSingleDeploymentTestWithNullProperties() throws IOException {
    // can't see how to differentiate null from {}
    validateDeployment("single-deployment-001.json", deploymentDetailedResponse -> {
      assertThat(deploymentDetailedResponse.application.configuration).isNotNull();
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.properties)
          .isNotNull();
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.properties.size())
          .isEqualTo(0);
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.secureProperties)
          .isNotNull();
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.secureProperties
          .size()).isEqualTo(0);
    });
  }

  @Test
  @Disabled
  void getSingleDeploymentTestWithoutProperties() throws IOException {
    validateDeployment("single-deployment-002.json", deploymentDetailedResponse -> {
      assertThat(deploymentDetailedResponse.application.configuration).isNotNull();
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.properties).isNull();
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.secureProperties)
          .isNull();
    });
  }

  @Test
  @Disabled
  void getSingleDeploymentTestWithInvalidProperties() throws IOException {
    // best effort unmarshalling, not validate invalid values. in adapter node value is null and text content retrieves traversal string chunks
    validateDeployment("single-deployment-003.json", deploymentDetailedResponse -> {
      assertThat(deploymentDetailedResponse.application.configuration).isNotNull();
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.properties)
          .isNotNull();
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.properties.size())
          .isEqualTo(0);
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.secureProperties)
          .isNotNull();
      assertThat(deploymentDetailedResponse.application.configuration.muleAgentApplicationPropertiesService.secureProperties
          .size()).isEqualTo(0);
    });
  }


  private void validateDeployment(String testCase, Consumer<DeploymentDetailedResponse> consumer) throws IOException {
    File deploymentJsonFile = new File(getClass().getClassLoader().getResource(
                                                                               testCase)
        .getFile());
    List<String> lines = Files.readAllLines(deploymentJsonFile.toPath());
    String deploymentJsonContent = String.join(System.lineSeparator(), lines);

    String singleDeploymentUrl = format(DEPLOYMENTS_PATH + "/%s", ORG_ID, ENV_ID, DEPLOYMENT_ID);

    mockServer.when(request().withMethod(GET).withPath(singleDeploymentUrl))
        .respond(response().withStatusCode(200).withBody(deploymentJsonContent, MediaType.JSON_UTF_8));

    RuntimeFabricClient client = buildClientSpy();

    DeploymentDetailedResponse response = client.getDeployment(DEPLOYMENT_ID);

    consumer.accept(response);
  }

  private RuntimeFabricClient buildClientSpy() {
    RuntimeFabricDeployment deployment = buildDeployment();
    RuntimeFabricClient client = spy(new RuntimeFabricClient(deployment, mock(DeployerLog.class)));
    doReturn(ORG_ID).when(client).getOrgId();
    doReturn(ENV_ID).when(client).getEnvId();
    doNothing().when(client).initialize();
    return client;
  }

  private RuntimeFabricDeployment buildDeployment() {
    RuntimeFabricDeployment deployment = new RuntimeFabricOnPremiseDeployment();
    deployment.setEnvironment(FAKE_ENV);
    deployment.setUsername(FAKE_USER);
    deployment.setPassword(FAKE_PASSWORD);
    deployment.setUri(BASE_URI + port);
    return deployment;
  }

  public int getFreePort() {
    try (ServerSocket socket = new ServerSocket(DEFAULT_PORT)) {
      socket.setReuseAddress(true);
      return socket.getLocalPort();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return DEFAULT_PORT;
  }
}


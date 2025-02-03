/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.fabric;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mule.tools.TestCase;
import org.mule.tools.client.core.exception.ClientException;
import org.mule.tools.client.fabric.model.DeploymentDetailedResponse;
import org.mule.tools.client.fabric.model.DeploymentModify;
import org.mule.tools.client.fabric.model.DeploymentRequest;
import org.mule.tools.model.anypoint.RuntimeFabricDeployment;
import org.mule.tools.model.anypoint.RuntimeFabricOnPremiseDeployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mule.tools.client.fabric.RuntimeFabricClient.DEPLOYMENTS_PATH;
import static org.mule.tools.client.fabric.RuntimeFabricClient.RUNTIME_FABRIC_DOMAIN_INFO;
import static org.mule.tools.client.fabric.RuntimeFabricClient.RUNTIME_FABRIC_TARGETS;
import static org.mule.tools.client.fabric.RuntimeFabricClient.RUNTIME_FABRIC_TARGET_INFO;

public class RuntimeFabricClientTestCase extends TestCase {

  protected static RuntimeFabricDeployment createDeployment() {
    RuntimeFabricDeployment deployment = new RuntimeFabricOnPremiseDeployment();
    deployment.setUri(getURI());
    deployment.setUsername(USERNAME);
    deployment.setPassword(PASSWORD);
    deployment.setEnvironment(ENVIRONMENT);
    deployment.setBusinessGroup(ORGANIZATION_NAME);
    return deployment;
  }

  protected static RuntimeFabricClient createClient(RuntimeFabricDeployment deployment) {
    return new RuntimeFabricClient(deployment, DEPLOYER_LOG);
  }

  private RuntimeFabricClient client;

  @BeforeEach
  void setup() {
    serverSetup();
    client = createClient(createDeployment());
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void deployTest(String connector) {
    setupConnector(connector);
    String url = String.format(DEPLOYMENTS_PATH, ".+", ".+");

    // OK
    setStringResponse(POST, String.format("{\"id\":\"%s\"}", APPLICATION_ID), url, 202);
    DeploymentDetailedResponse detailedResponse = client.deploy(getDeploymentRequest());
    assertThat(detailedResponse).isNotNull();
    assertThat(detailedResponse.id).isEqualTo(APPLICATION_ID);

    // 404
    clearRequest(POST, url);
    assertThatThrownBy(() -> client.deploy(getDeploymentRequest())).isInstanceOf(ClientException.class)
        .hasMessageContaining("404 Not Found");
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void redeployTest(String connector) {
    setupConnector(connector);
    String url = String.format(DEPLOYMENTS_PATH, ".+", ".+") + "/.+";

    // OK
    setStringResponse(PATCH, String.format("{\"id\":\"%s\"}", APPLICATION_ID), url);
    DeploymentDetailedResponse detailedResponse = client.redeploy(new DeploymentModify(), APPLICATION_ID);
    assertThat(detailedResponse).isNotNull();
    assertThat(detailedResponse.id).isEqualTo(APPLICATION_ID);

    // 404
    clearRequest(PATCH, url);
    assertThatThrownBy(() -> client.redeploy(new DeploymentModify(), APPLICATION_ID)).isInstanceOf(ClientException.class)
        .hasMessageContaining("404 Not Found");
  }

  @Disabled("Check why this method expects a 204 and at the same time reads an entity")
  @ParameterizedTest
  @MethodSource("connectors")
  void deleteTest(String connector) {
    setupConnector(connector);
    String url = String.format(DEPLOYMENTS_PATH, ".+", ".+") + "/.+";

    // OK
    setStringResponse(DELETE, String.format("{\"id\":\"%s\"}", APPLICATION_ID), url, 204);
    DeploymentDetailedResponse detailedResponse = client.deleteDeployment(APPLICATION_ID);
    assertThat(detailedResponse).isNotNull();
    assertThat(detailedResponse.id).isEqualTo(APPLICATION_ID);

    // 404
    clearRequest(DELETE, url);
    assertThatThrownBy(() -> client.deleteDeployment(APPLICATION_ID)).isInstanceOf(ClientException.class)
        .hasMessageContaining("404 Not Found");
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void getTargetsTests(String connector) {
    setupConnector(connector);
    String url = "/" + String.format(RUNTIME_FABRIC_TARGETS, ".+");
    // OK
    setStringResponse(GET, "[{},{}]", url);
    JsonArray targets = client.getTargets();

    assertThat(targets).isNotNull();
    assertThat(targets.size()).isEqualTo(2);
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void getTargetInfoTest(String connector) {
    setupConnector(connector);
    String url = "/" + String.format(RUNTIME_FABRIC_TARGET_INFO, ".+", ".+");
    // OK
    setStringResponse(GET, String.format("{\"id\":\"%s\"}", APPLICATION_ID), url);
    JsonObject targetInfo = client.getTargetInfo(APPLICATION_ID);

    assertThat(targetInfo).isNotNull();
    assertThat(targetInfo.has("id")).isTrue();
    assertThat(targetInfo.get("id").getAsString()).isEqualTo(APPLICATION_ID);
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void getDomainInfo(String connector) {
    setupConnector(connector);
    String url = "/" + String.format(RUNTIME_FABRIC_DOMAIN_INFO, ".+", ".+", ".+");
    // OK
    setStringResponse(GET, "[{},{}]", url);
    JsonArray targets = client.getDomainInfo(APPLICATION_ID);

    assertThat(targets).isNotNull();
    assertThat(targets.size()).isEqualTo(2);
  }

  protected DeploymentRequest getDeploymentRequest() {
    DeploymentRequest request = new DeploymentRequest();
    request.setName(APPLICATION_NAME);
    return request;
  }

  protected static void serverSetup() {
    TestCase.serverSetup();
  }
}

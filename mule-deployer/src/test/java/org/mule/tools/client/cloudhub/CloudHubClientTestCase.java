/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.cloudhub;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mule.tools.TestCase;
import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.client.cloudhub.model.DeploymentLogRequest;
import org.mule.tools.model.anypoint.CloudHubDeployment;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mule.tools.client.authentication.AuthenticationServiceClient.ME;
import static org.mule.tools.client.cloudhub.CloudHubClient.APPLICATIONS_DOMAINS_PATH;
import static org.mule.tools.client.cloudhub.CloudHubClient.APPLICATION_ENVIRONMENT;
import static org.mule.tools.client.cloudhub.CloudHubClient.A_APPLICATION_LOGS;
import static org.mule.tools.client.cloudhub.CloudHubClient.A_APPLICATION_PATH;
import static org.mule.tools.client.cloudhub.CloudHubClient.APPLICATION_STATUS;
import static org.mule.tools.client.cloudhub.CloudHubClient.A_INSTANCE_LOGS;
import static org.mule.tools.client.cloudhub.CloudHubClient.DEPLOYMENTS_PATH;
import static org.mule.tools.client.cloudhub.CloudHubClient.SUPPORTED_VERSIONS_PATH;

class CloudHubClientTestCase extends TestCase {

  private static final String APPLICATION_DOMAIN = UUID.randomUUID().toString();

  protected static CloudHubDeployment createDeployment() {
    CloudHubDeployment deployment = new CloudHubDeployment();
    deployment.setUri(getURI());
    deployment.setUsername(USERNAME);
    deployment.setPassword(PASSWORD);
    deployment.setEnvironment(ENVIRONMENT);
    deployment.setBusinessGroup(ORGANIZATION_NAME);
    return deployment;
  }

  protected static CloudHubClient createClient(CloudHubDeployment deployment) {
    return new CloudHubClient(deployment, DEPLOYER_LOG);
  }

  private CloudHubClient client;

  @BeforeEach
  void setup() {
    serverSetup();
    client = createClient(createDeployment());
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void getApplicationsByDomainTest(String connector) {
    setupConnector(connector);
    String url = String.format(A_APPLICATION_PATH, ".+");

    // OK
    setStringResponse(GET, "{}", url);
    assertThat(client.getApplications(APPLICATION_DOMAIN)).isNotNull();

    // 404
    clearRequest(GET, url);
    assertThat(client.getApplications(APPLICATION_DOMAIN)).isNull();
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void startApplicationsTest(String connector) {
    setupConnector(connector);
    // OK
    setVoidResponse(POST, String.format(APPLICATION_STATUS, ".+"));
    client.startApplications(APPLICATION_DOMAIN);
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void stopApplicationsTest(String connector) {
    setupConnector(connector);
    // OK
    setVoidResponse(POST, String.format(APPLICATION_STATUS, ".+"));
    client.stopApplications(APPLICATION_DOMAIN);
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void isDomainAvailableTest(String connector) {
    setupConnector(connector);
    // OK
    setStringResponse(GET, "{ \"available\": true}", String.format(APPLICATIONS_DOMAINS_PATH, ".+"));
    assertThat(client.isDomainAvailable(APPLICATION_DOMAIN)).isTrue();
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void getSupportedMuleVersionsTest(String connector) {
    setupConnector(connector);
    // OK
    int total = 5;
    HashMap<String, Object> pageData = new HashMap<>();
    pageData.put("TOTAL", total);
    pageData.put("DATA", "[" +
        IntStream.range(0, total).mapToObj(index -> {
          HashMap<String, Object> data = new HashMap<>();
          data.put("VERSION", "version-" + index);
          return template("data/cloudhub/supported-version.json", data);
        }).collect(Collectors.joining(",")) + "]");
    setTemplateResponse(GET, "data/cloudhub/page.json", SUPPORTED_VERSIONS_PATH, pageData);
    assertThat(client.getSupportedMuleVersions()).isNotEmpty().hasSize(total);
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void getDeploymentsTest(String connector) {
    setupConnector(connector);
    // OK
    Application application = new Application();
    application.setDomain(APPLICATION_DOMAIN);
    int total = 7;
    HashMap<String, Object> pageData = new HashMap<>();
    pageData.put("TOTAL", total);
    pageData.put("DATA", "[" +
        IntStream.range(0, total).mapToObj(index -> {
          HashMap<String, Object> data = new HashMap<>();
          data.put("ID", "ID-" + index);
          return template("data/cloudhub/deployment.json", data);
        }).collect(Collectors.joining(",")) + "]");
    setTemplateResponse(GET, "data/cloudhub/page.json", String.format(DEPLOYMENTS_PATH, application.getDomain()), pageData);
    assertThat(client.getDeployments(application)).isNotEmpty().hasSize(total);
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void getEnvironmentTest(String connector) {
    setupConnector(connector);
    setStringResponse(GET, "{}", APPLICATION_ENVIRONMENT);
    assertThat(client.getEnvironment()).isNotNull();
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void getLogsTest(String connector) {
    setupConnector(connector);
    // OK
    Application application = new Application();
    application.setDomain(APPLICATION_DOMAIN);
    int total = 7;
    String records = "[" +
        IntStream.range(0, total).mapToObj(index -> {
          HashMap<String, Object> data = new HashMap<>();
          data.put("ID", "ID-" + index);
          return template("data/cloudhub/deployment.json", data);
        }).collect(Collectors.joining(",")) + "]";

    setStringResponse(POST, records, String.format(A_APPLICATION_LOGS, ".+"));
    assertThat(client.getLogs(application, new DeploymentLogRequest())).isNotNull();
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void getEntireLogs(String connector) {
    setupConnector(connector);
    // OK
    Application application = new Application();
    application.setDomain(APPLICATION_DOMAIN);
    int total = 7;
    String records = "[" +
        IntStream.range(0, total).mapToObj(index -> {
          HashMap<String, Object> data = new HashMap<>();
          data.put("ID", "ID-" + index);
          return template("data/cloudhub/deployment.json", data);
        }).collect(Collectors.joining(",")) + "]";

    setStringResponse(GET, records, String.format(A_INSTANCE_LOGS, ".+", ".+"));

    assertThat(client.getEntireLogs(application, UUID.randomUUID().toString())).isNotNull();
  }

  @Test
  void getSuborganizationIdsTest() {
    JsonObject object = new JsonObject();
    JsonArray jsonArray = new JsonArray();
    object.add("subOrganizationIds", jsonArray);
    // EMPTY
    assertThat(client.getSuborganizationIds(object)).isNotNull().isEmpty();
    // WITH VALUES
    int total = 7;
    IntStream.range(0, total).forEach(index -> jsonArray.add("id-" + index));
    assertThat(client.getSuborganizationIds(object)).isNotNull().hasSize(total);
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void getBusinessGroupIdByBusinessGroupPathTest(String connector) {
    setupConnector(connector);
    CloudHubDeployment deployment = createDeployment();
    deployment.setBusinessGroup(ORGANIZATION_NAME + "-NESTED");
    CloudHubClient client0 = createClient(deployment);

    setResponse(GET, "data/cloudhub/me.json", ME, "plain/text");

    // INVALID GROUP
    assertThatThrownBy(client0::getBusinessGroupIdByBusinessGroupPath).isInstanceOf(ArrayIndexOutOfBoundsException.class)
        .hasMessageContaining("Cannot find business group");
    // OK
    deployment.setBusinessGroup("NESTED");
    CloudHubClient client01 = createClient(deployment);
    client01.getBusinessGroupIdByBusinessGroupPath();
  }

  protected static void serverSetup() {
    TestCase.serverSetup();
  }
}

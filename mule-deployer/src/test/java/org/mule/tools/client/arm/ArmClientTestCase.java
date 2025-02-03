/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.arm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mule.tools.TestCase;
import org.mule.tools.client.arm.model.Applications;
import org.mule.tools.client.core.exception.ClientException;
import org.mule.tools.client.model.TargetType;
import org.mule.tools.model.anypoint.AnypointDeployment;
import org.mule.tools.model.anypoint.ArmDeployment;

import javax.ws.rs.NotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArmClientTestCase extends TestCase {

  private static final String TOKEN = UUID.randomUUID().toString();
  private static final String DELETED = "DELETED";
  private static final int SERVER_ID = 666;
  private static final int SERVER_GROUP_ID = 333;
  private static final int APPLICATION_ID = 8908;

  private static final String API_VERSION = "v1";
  private static final String BASE_HYBRID_API_PATH = "/hybrid/api";
  private static final String HYBRID_API_V1 = BASE_HYBRID_API_PATH + "/" + API_VERSION;
  private static final String CLUSTERS = HYBRID_API_V1 + "/clusters";
  private static final String APPLICATIONS = HYBRID_API_V1 + "/applications";
  private static final String SERVER_GROUPS = HYBRID_API_V1 + "/serverGroups";

  private static final String SERVERS = HYBRID_API_V1 + "/servers";
  private static final String REGISTRATION = HYBRID_API_V1 + "/servers/registrationToken";

  protected static AnypointDeployment createDeployment() {
    ArmDeployment deployment = new ArmDeployment();
    deployment.setUri(getURI());
    deployment.setUsername(USERNAME);
    deployment.setPassword(PASSWORD);
    deployment.setEnvironment(ENVIRONMENT);
    deployment.setBusinessGroup(ORGANIZATION_NAME);
    deployment.setArmInsecure(true);
    return deployment;
  }

  protected static ArmClient createClient(AnypointDeployment deployment) {
    return new ArmClient(deployment, DEPLOYER_LOG);
  }

  private ArmClient client;

  @BeforeEach
  void setup() {
    serverSetup();
    client = createClient(createDeployment());
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void getRegistrationTokenTest(String connector) {
    setupConnector(connector);
    setStringResponse(GET, String.format("{\"data\": \"%s\"}", TOKEN), REGISTRATION);
    //
    assertThat(TOKEN).isEqualTo(client.getRegistrationToken());
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void getApplicationTest(String connector) {
    setupConnector(connector);
    Applications applications = client.getApplications();
    //
    assertThat(applications).isNotNull();
    assertThat(applications.data).hasSize(19);
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void isStartedTest(String connector) {
    setupConnector(connector);
    //
    assertThat(client.isStarted(0)).isFalse();
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void undeployApplicationTest(String connector) {
    setupConnector(connector);
    //
    assertThat(client.undeployApplication(APPLICATION_ID)).isEqualTo(DELETED);
    assertThatThrownBy(() -> client.undeployApplication(-1)).isInstanceOf(ClientException.class);
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void getServersTest(String connector) {
    setupConnector(connector);
    assertThat(client.getServers()).isNotEmpty().hasSize(3);
    assertThat(client.getServerGroup(SERVER_GROUP_ID).data).isNotEmpty().hasSize(3);
    assertThat(client.getServer(SERVER_ID)).isNotNull();
    client.deleteServer(SERVER_ID);
  }

  @ParameterizedTest
  @MethodSource("connectorsWithTargetType")
  void undeployApplicationWithMetadataTest(String connector, TargetType targetType) {
    Map<TargetType, String> map = new HashMap<>();
    map.put(TargetType.cluster, CLUSTERS);
    map.put(TargetType.server, SERVERS);
    map.put(TargetType.serverGroup, SERVER_GROUPS);

    setupConnector(connector);
    ApplicationMetadata metadata = metadata(targetType);
    // OK
    client.undeployApplication(metadata);

    // NULL DATA
    setStringResponse(GET, "{}", map.get(targetType));
    assertThatThrownBy(() -> client.undeployApplication(metadata)).isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Couldn't find target named");

    // INVALID NAME
    setStringResponse(GET, "{ \"data\": [ {\"name\": \"XXX\"} ]}", map.get(targetType));
    assertThatThrownBy(() -> client.undeployApplication(metadata)).isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Couldn't find target named");

    // NULL APPLICATION
    setStringResponse(GET, "{}", APPLICATIONS);
    assertThatThrownBy(() -> client.undeployApplication(metadata)).isInstanceOf(NotFoundException.class)
        .hasMessageContaining("does not exist.");
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void deployApplicationTest(String connector) {
    setupConnector(connector);
    ApplicationMetadata metadata = metadata(TargetType.server);
    assertThat(APPLICATION_ID).isEqualTo(client.deployApplication(metadata).data.id);
  }

  @ParameterizedTest
  @MethodSource("connectors")
  void redeployApplicationTest(String connector) {
    setupConnector(connector);
    ApplicationMetadata metadata = metadata(TargetType.server);
    assertThat(APPLICATION_ID).isEqualTo(client.redeployApplication(APPLICATION_ID, metadata).data.id);
  }

  protected static ApplicationMetadata metadata(TargetType targetType) {
    ApplicationMetadata metadata = mock(ApplicationMetadata.class);
    when(metadata.getTarget()).thenReturn(APPLICATION_NAME);
    when(metadata.getName()).thenReturn(APPLICATION_NAME);
    when(metadata.getTargetType()).thenReturn(targetType);
    when(metadata.getFile()).thenReturn(APPLICATION_FILE);
    when(metadata.getProperties()).thenReturn(new HashMap<>());
    return metadata;
  }

  protected static void serverSetup() {
    Map<String, Object> data = new java.util.HashMap<>();
    data.put("APPLICATION_ID", APPLICATION_ID);
    data.put("APPLICATION_NAME", APPLICATION_NAME);
    TestCase.serverSetup();
    setTemplateResponse(GET, "data/arm/applications.json", APPLICATIONS, data);
    setStringResponse(DELETE, DELETED, APPLICATIONS + "/\\d+");
    setTemplateResponse(GET, "data/arm/targets.json", CLUSTERS, data);
    setTemplateResponse(GET, "data/arm/targets.json", SERVERS, data);
    setTemplateResponse(GET, "data/arm/targets.json", SERVER_GROUPS, data);
    setTemplateResponse(GET, "data/arm/targets.json", SERVER_GROUPS + "/\\d+", data);
    setStringResponse(DELETE, DELETED, SERVERS + "/\\d+");
    setStringResponse(GET, "{}", SERVERS + "/\\d+");
    setStringResponse(POST, String.format("{ \"data\": {\"id\" : %d}}", APPLICATION_ID), APPLICATIONS);
    setStringResponse(PATCH, String.format("{ \"data\": {\"id\" : %d}}", APPLICATION_ID), APPLICATIONS + "/\\d+");
  }
}

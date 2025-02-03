/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.fabric;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mule.tools.TestBase;
import org.mule.tools.client.core.exception.ClientException;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.fabric.RuntimeFabricClient;
import org.mule.tools.client.fabric.model.DeploymentDetailedResponse;
import org.mule.tools.client.fabric.model.DeploymentRequest;
import org.mule.tools.client.fabric.model.Deployments;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.RuntimeFabricOnPremiseDeployment;
import org.mule.tools.model.anypoint.RuntimeFabricOnPremiseDeploymentSettings;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.verification.DeploymentVerification;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

class RuntimeFabricArtifactDeployerTest extends TestBase {

  private static final String DOMAIN_TEST = "*.mydomain.com";
  private static final String MULE_VERSION = generateVersion();
  private static final String MULE_VERSION_TAG = "v" + VERSION;
  private static final String TARGET_ID = "target_" + generateVersion();
  private static final String DEPLOYMENT_ID = UUID.randomUUID().toString();

  private final RuntimeFabricClient client = mock(RuntimeFabricClient.class);
  private final RuntimeFabricOnPremiseDeployment deployment = new RuntimeFabricOnPremiseDeployment();
  private final DeployerLog deployerLog = mock(DeployerLog.class);

  @BeforeEach
  public void setUp() throws DeploymentException {
    HashMap<String, Object> data = new HashMap<>();
    data.put("BASE_VERSION", MULE_VERSION);
    data.put("TAG", MULE_VERSION_TAG);
    data.put("DEPLOYMENT_ID", DEPLOYMENT_ID);
    data.put("DEPLOYMENT_NAME", APPLICATION_NAME);
    data.put("APPLICATION_NAME", APPLICATION_NAME);
    data.put("TARGET_ID", TARGET_ID);
    //
    reset(client);
    when(client.getDomainInfo(any()))
        .thenReturn(toJsonTree(Collections.singletonList(DOMAIN_TEST)).getAsJsonArray());
    when(client.getTargetInfo(any())).thenReturn(fromJson(template("data/rtf/target.json", data), JsonObject.class));
    when(client.getTargets()).thenReturn(fromJson(template("data/rtf/targets.json", data), JsonArray.class));
    when(client.getDeployments()).thenReturn(fromJson(template("data/rtf/deployments.json", data), Deployments.class));
    when(client.getDeployment(anyString())).thenReturn(fromJson("{\"status\":\"STARTED\"}", DeploymentDetailedResponse.class));
    //
    deployment.setGroupId(GROUP_ID);
    deployment.setArtifactId(ARTIFACT_ID);
    deployment.setVersion(VERSION);
    deployment.setMuleVersion(MULE_VERSION);
    deployment.setApplicationName(APPLICATION_NAME);
    deployment.setTarget(APPLICATION_NAME);
    deployment.setProvider("MC");
    deployment.setUri("http://localhost");
    deployment.setProperties(new HashMap<>());
    deployment.setDeploymentTimeout(100L);
    deployment.setDeploymentSettings(new RuntimeFabricOnPremiseDeploymentSettings());
  }

  @Test
  void createRequestBuilderTest() {
    try (MockedConstruction<RuntimeFabricClient> constructor = mockConstruction(RuntimeFabricClient.class, (mock, context) -> {
    })) {
      RuntimeFabricArtifactDeployer deployer =
          new RuntimeFabricArtifactDeployer(new RuntimeFabricOnPremiseDeployment(), deployerLog);
      assertThat(deployer.createRequestBuilder()).isNotNull().isInstanceOf(RequestBuilder.class);
    }
  }

  @Test
  void deployApplicationTest() throws DeploymentException {
    HashMap<String, Object> data = new HashMap<>();
    data.put("BASE_VERSION", MULE_VERSION);
    data.put("TAG", MULE_VERSION_TAG);
    data.put("DEPLOYMENT_ID", DEPLOYMENT_ID);
    data.put("DEPLOYMENT_NAME", APPLICATION_NAME);
    data.put("APPLICATION_NAME", APPLICATION_NAME);
    data.put("TARGET_ID", UUID.randomUUID().toString());
    RuntimeFabricArtifactDeployer deployer = new RuntimeFabricArtifactDeployer(deployment, client, deployerLog);
    // redeploy
    deployer.deployApplication();
    // deploy
    when(client.getDeployments()).thenReturn(fromJson(template("data/rtf/deployments.json", data), Deployments.class));
    deployer.deployApplication();
    // Exception
    when(client.deploy(nullable(DeploymentRequest.class))).thenThrow(ClientException.class);
    assertThatThrownBy(deployer::deployApplication).isInstanceOf(DeploymentException.class)
        .hasMessageContaining("Could not deploy application.");
  }

  @Test
  void undeployApplicationTest() throws DeploymentException {
    RuntimeFabricArtifactDeployer deployer = new RuntimeFabricArtifactDeployer(deployment, client, deployerLog);
    deployer.deployApplication();
    deployer.undeployApplication();
    //
    when(client.deleteDeployment(anyString())).thenThrow(ClientException.class);
    assertThatThrownBy(deployer::undeployApplication).isInstanceOf(DeploymentException.class)
        .hasMessageContaining("Could not undeploy application.");
  }

  @Test
  void notImplementedTest() {
    String message = "of domains to Runtime Fabric is not supported";
    RuntimeFabricArtifactDeployer deployer = new RuntimeFabricArtifactDeployer(deployment, client, deployerLog);
    //
    assertThatThrownBy(deployer::deployDomain).isInstanceOf(DeploymentException.class).hasMessageContaining(message);
    assertThatThrownBy(deployer::undeployDomain).isInstanceOf(DeploymentException.class).hasMessageContaining(message);
  }

  @Test
  void setDeploymentVerificationTest() throws DeploymentException {
    DeploymentVerification deploymentVerification = mock(DeploymentVerification.class);
    doNothing().when(deploymentVerification).assertDeployment(any(Deployment.class));
    //
    RuntimeFabricArtifactDeployer deployer = new RuntimeFabricArtifactDeployer(deployment, client, deployerLog);
    deployer.setRequestBuilder(deployer.createRequestBuilder());
    deployer.setDeploymentVerification(deploymentVerification);
    //
    deployer.deployApplication();
  }
}

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
import org.mule.tools.TestBase;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.fabric.RuntimeFabricClient;
import org.mule.tools.client.fabric.model.DeploymentModify;
import org.mule.tools.client.fabric.model.DeploymentRequest;
import org.mule.tools.client.fabric.model.Deployments;
import org.mule.tools.client.fabric.model.Target;
import org.mule.tools.model.anypoint.RuntimeFabricOnPremiseDeployment;
import org.mule.tools.model.anypoint.RuntimeFabricOnPremiseDeploymentSettings;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class RequestBuilderTest extends TestBase {

  private static final String DOMAIN_TEST = "*.mydomain.com";
  private static final String MULE_VERSION = generateVersion();
  private static final String MULE_VERSION_TAG = "v" + VERSION;
  private static final String TARGET_ID = "target_" + generateVersion();
  private static final String DEPLOYMENT_ID = UUID.randomUUID().toString();

  private final RuntimeFabricClient client = mock(RuntimeFabricClient.class);
  private final RuntimeFabricOnPremiseDeployment deployment = new RuntimeFabricOnPremiseDeployment();
  private final RequestBuilder requestBuilder = new RequestBuilder(deployment, client);

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
    //
    deployment.setGroupId(GROUP_ID);
    deployment.setArtifactId(ARTIFACT_ID);
    deployment.setVersion(VERSION);
    deployment.setMuleVersion(MULE_VERSION);
    deployment.setApplicationName(APPLICATION_NAME);
    deployment.setTarget(APPLICATION_NAME);
    deployment.setProvider("MC");
    deployment.setProperties(new HashMap<>());
    deployment.setDeploymentSettings(new RuntimeFabricOnPremiseDeploymentSettings());
  }

  @Test
  public void autoGenerateUrl() throws Exception {
    String finalUrl = DOMAIN_TEST.replace("*", deployment.getApplicationName());
    Target target = requestBuilder.buildTarget();
    assertThat(target.deploymentSettings.getHttp().getInbound().getPublicUrl()).describedAs("publicUrl is not the expected")
        .isEqualTo(finalUrl);
  }

  @Test
  public void useDefinedUrl() throws Exception {
    String definedUrl = "myapp.test.com";
    RuntimeFabricOnPremiseDeploymentSettings deploymentSettings = new RuntimeFabricOnPremiseDeploymentSettings();
    deploymentSettings.getHttp().getInbound().setPublicUrl(definedUrl);
    deployment.setDeploymentSettings(deploymentSettings);
    //
    Target target = requestBuilder.buildTarget();
    assertThat(target.deploymentSettings.getHttp().getInbound().getPublicUrl()).describedAs("publicUrl is not the expected")
        .isEqualTo(definedUrl);
  }

  @Test
  void buildDeploymentRequestTest() throws Exception {
    DeploymentRequest deploymentRequest = requestBuilder.buildDeploymentRequest();
    assertThat(deploymentRequest).isNotNull();
    assertThat(deploymentRequest.name).isEqualTo(APPLICATION_NAME);
    assertThat(deploymentRequest.application.ref.groupId).isEqualTo(GROUP_ID);
    assertThat(deploymentRequest.application.ref.artifactId).isEqualTo(ARTIFACT_ID);
    assertThat(deploymentRequest.application.ref.version).isEqualTo(VERSION);
  }

  @Test
  void getTargetIdTest() {
    assertThatThrownBy(() -> RequestBuilder.getTargetId(fromJson("[]", JsonArray.class), ""))
        .isInstanceOf(DeploymentException.class).hasMessageContaining("Could not find target ");
  }

  @Test
  void resolveTagTest() {
    assertThat(requestBuilder.resolveTag(UUID.randomUUID().toString(), "6.2.0")).isNull();
    assertThat(requestBuilder.resolveTag(UUID.randomUUID().toString(), MULE_VERSION)).isEqualTo(MULE_VERSION_TAG);
    //
    when(client.getTargetInfo(any())).thenReturn(fromJson("{}", JsonObject.class));
    assertThat(requestBuilder.resolveTag(UUID.randomUUID().toString(), MULE_VERSION)).isNull();
    //
    when(client.getTargetInfo(any())).thenReturn(fromJson("{\"runtimes\":[]}", JsonObject.class));
    assertThat(requestBuilder.resolveTag(UUID.randomUUID().toString(), MULE_VERSION)).isNull();
  }

  @Test
  void getDeploymentId() {
    String message =
        "Could not find deployment ID. Please check if there is an application with the same name under a different environment.";
    Target target = new Target();
    // OK
    target.setTargetId(TARGET_ID);
    assertThat(requestBuilder.getDeploymentId(target)).isEqualTo(DEPLOYMENT_ID);
    // DIFFERENT TARGET ID
    target.setTargetId(UUID.randomUUID().toString());
    assertThatThrownBy(() -> requestBuilder.getDeploymentId(target)).isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(message);
    // DIFFERENT APP NAME
    deployment.setApplicationName(UUID.randomUUID().toString());
    assertThatThrownBy(() -> requestBuilder.getDeploymentId(target)).isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(message);
  }

  @Test
  void buildDeploymentModify() throws Exception {
    DeploymentModify deploymentRequest = requestBuilder.buildDeploymentModify();
    assertThat(deploymentRequest).isNotNull();
    assertThat(deploymentRequest.application.ref.groupId).isEqualTo(GROUP_ID);
    assertThat(deploymentRequest.application.ref.artifactId).isEqualTo(ARTIFACT_ID);
    assertThat(deploymentRequest.application.ref.version).isEqualTo(VERSION);
  }
}

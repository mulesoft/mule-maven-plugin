/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.cloudhub2;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.fabric.RuntimeFabricClient;
import org.mule.tools.model.anypoint.Cloudhub2Deployment;
import org.mule.tools.model.anypoint.Cloudhub2DeploymentSettings;
import org.mule.tools.model.anypoint.Integration;
import org.mule.tools.model.anypoint.LogLevel;
import org.mule.tools.model.anypoint.ObjectStoreV2;
import org.mule.tools.model.anypoint.ScopeLoggingConfiguration;
import org.mule.tools.model.anypoint.Service;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestBuilderTest {

  private static final String DOMAIN_TEST = "*.mydomain.com";
  private static final String TARGETS_RESPONSE = "[\n" +
      "   {\n" +
      "      \"id\":\"sampleId\",      \n" +
      "         \"name\":\"fabric1\",\n" +
      "         \"organizationId\":\"orgId\",\n" +
      "         \"status\":\"Connected\"         \n" +
      "   }   \n" +
      "]";
  private static final String TARGET_RESPONSE = "{\n" +
      "   \"id\":\"sampleId\",\n" +
      "   \"name\":\"fabric1\",\n" +
      "   \"runtimes\":[\n" +
      "      {\n" +
      "         \"type\":\"mule\",\n" +
      "         \"versions\":[\n" +
      "            {\n" +
      "               \"baseVersion\":\"4.2.0\",\n" +
      "               \"tag\":\"v1.2.28\"\n" +
      "            }\n" +
      "         ]\n" +
      "      }\n" +
      "   ]\n" +
      "}";
  private static final String DEPLOY_REQUEST =
      "{\"name\":\"test-app\",\"application\":{\"ref\":{\"packaging\":\"jar\"},\"desiredState\":\"STARTED\",\"configuration\":{\"mule.agent.application.properties.service\":{\"applicationName\":\"test-app\"},\"mule.agent.logging.service\":{\"artifactName\":\"test-app\",\"scopeLoggingConfigurations\":[{\"scope\":\"com.pkg.debug\",\"logLevel\":\"INFO\"}]}},\"vCores\":\"0.5\",\"integrations\":{\"services\":{\"objectStoreV2\":{\"enabled\":true}}}},\"target\":{\"targetId\":\"sampleId\",\"provider\":\"MC\",\"deploymentSettings\":{\"runtimeVersion\":\"4.2.0:v1.2.28\",\"lastMileSecurity\":false,\"clustered\":false,\"enforceDeployingReplicasAcrossNodes\":false,\"http\":{\"inbound\":{\"publicUrl\":\"test-app.mydomain.com\"}},\"forwardSslSession\":false,\"disableAmLogForwarding\":false,\"generateDefaultPublicUrl\":false,\"runtime\":{\"version\":\"4.2.0:v1.2.28\"}}}}";
  private static final String DEPLOY_REQUEST_WITH_INSTANCE_TYPE =
      "{\"name\":\"test-app\",\"application\":{\"ref\":{\"packaging\":\"jar\"},\"desiredState\":\"STARTED\",\"configuration\":{\"mule.agent.application.properties.service\":{\"applicationName\":\"test-app\"},\"mule.agent.logging.service\":{\"artifactName\":\"test-app\",\"scopeLoggingConfigurations\":[{\"scope\":\"com.pkg.debug\",\"logLevel\":\"INFO\"}]}},\"integrations\":{\"services\":{\"objectStoreV2\":{\"enabled\":true}}}},\"target\":{\"targetId\":\"sampleId\",\"provider\":\"MC\",\"deploymentSettings\":{\"instanceType\":\"instanceValue\",\"runtimeVersion\":\"4.2.0:v1.2.28\",\"lastMileSecurity\":false,\"clustered\":false,\"enforceDeployingReplicasAcrossNodes\":false,\"http\":{\"inbound\":{\"publicUrl\":\"test-app.mydomain.com\"}},\"forwardSslSession\":false,\"disableAmLogForwarding\":false,\"generateDefaultPublicUrl\":false,\"runtime\":{\"version\":\"4.2.0:v1.2.28\"}}}}";
  private static final String DEPLOY_REQUEST_WITH_PUBLIC_URL =
      "{\"name\":\"test-app\",\"application\":{\"ref\":{\"packaging\":\"jar\"},\"desiredState\":\"STARTED\",\"configuration\":{\"mule.agent.application.properties.service\":{\"applicationName\":\"test-app\"},\"mule.agent.logging.service\":{\"artifactName\":\"test-app\",\"scopeLoggingConfigurations\":[{\"scope\":\"com.pkg.debug\",\"logLevel\":\"INFO\"}]}},\"vCores\":\"0.5\",\"integrations\":{\"services\":{\"objectStoreV2\":{\"enabled\":true}}}},\"target\":{\"targetId\":\"sampleId\",\"provider\":\"MC\",\"deploymentSettings\":{\"runtimeVersion\":\"4.2.0:v1.2.28\",\"lastMileSecurity\":false,\"clustered\":false,\"enforceDeployingReplicasAcrossNodes\":false,\"http\":{\"inbound\":{\"publicUrl\":\"test-app.mydomain.com\"}},\"forwardSslSession\":false,\"disableAmLogForwarding\":false,\"generateDefaultPublicUrl\":true,\"runtime\":{\"version\":\"4.2.0:v1.2.28\"}}}}";
  private RequestBuilderCh2 requestBuilder;
  private RuntimeFabricClient runtimeFabricClientMock;
  private Cloudhub2Deployment cloudhub2Deployment;

  public void setUp(Cloudhub2DeploymentSettings settings) throws DeploymentException {
    runtimeFabricClientMock = mock(RuntimeFabricClient.class);
    cloudhub2Deployment = new Cloudhub2Deployment();
    cloudhub2Deployment.setArtifact((String) null);
    cloudhub2Deployment.setMuleVersion("4.2.0:v1.2.28");
    cloudhub2Deployment.setApplicationName("test-app");
    cloudhub2Deployment.setTarget("fabric1");
    cloudhub2Deployment.setProvider("MC");

    Integration integrations = new Integration();
    Service service = new Service();
    ObjectStoreV2 os2 = new ObjectStoreV2();
    os2.setEnabled(true);
    service.setObjectStoreV2(os2);
    integrations.setServices(service);
    cloudhub2Deployment.setIntegrations(integrations);
    if (settings != null) {
      cloudhub2Deployment.setDeploymentSettings(new Cloudhub2DeploymentSettings(settings));
    } else {
      cloudhub2Deployment.setDeploymentSettings(new Cloudhub2DeploymentSettings());
    }
    cloudhub2Deployment.setvCores("0.5");
    List<ScopeLoggingConfiguration> ScopeLoggingConfigs = new ArrayList<ScopeLoggingConfiguration>();
    ScopeLoggingConfiguration configuration = new ScopeLoggingConfiguration();
    configuration.setScope("com.pkg.debug");
    configuration.setLogLevel(LogLevel.INFO);
    ScopeLoggingConfigs.add(configuration);
    cloudhub2Deployment.setScopeLoggingConfigurations(ScopeLoggingConfigs);
    requestBuilder = new RequestBuilderCh2(cloudhub2Deployment, runtimeFabricClientMock);

    ArrayList<String> domains = newArrayList(DOMAIN_TEST);

    when(runtimeFabricClientMock.getDomainInfo(any())).thenReturn(new Gson().toJsonTree(domains).getAsJsonArray());
    when(runtimeFabricClientMock.getTargetInfo(any())).thenReturn(new Gson().fromJson(TARGET_RESPONSE, JsonObject.class));
    when(runtimeFabricClientMock.getTargets()).thenReturn(new Gson().fromJson(TARGETS_RESPONSE, JsonArray.class));
  }

  @Test
  public void requestBuildTest() throws Exception {
    setUp(null);
    String request = new Gson().toJson(requestBuilder.buildDeploymentRequest());
    assertThat(DEPLOY_REQUEST).describedAs("request is not the expected").isEqualTo(request);
  }

  @Test
  public void requestBuildTestWthPublicURL() throws Exception {
    Cloudhub2DeploymentSettings settings = new Cloudhub2DeploymentSettings();
    settings.setGenerateDefaultPublicUrl(true);
    setUp(settings);
    String request = new Gson().toJson(requestBuilder.buildDeploymentRequest());
    assertThat(DEPLOY_REQUEST_WITH_PUBLIC_URL).describedAs("request is not the expected").isEqualTo(request);
  }

  @Test
  public void requestBuildWithResourcesAndVCoresTest() throws Exception {
    setUp(null);
    ((Cloudhub2DeploymentSettings) cloudhub2Deployment.getDeploymentSettings()).setInstanceType("instanceValue");

    assertThatThrownBy(() -> {
      requestBuilder.buildDeploymentRequest();
    }).isExactlyInstanceOf(DeploymentException.class);

  }

  @Test
  public void requestBuildWithInstanceTypeTest() throws Exception {
    setUp(null);
    cloudhub2Deployment.setvCores(null);
    ((Cloudhub2DeploymentSettings) cloudhub2Deployment.getDeploymentSettings()).setInstanceType("instanceValue");
    String request = new Gson().toJson(requestBuilder.buildDeploymentRequest());
    assertThat(DEPLOY_REQUEST_WITH_INSTANCE_TYPE).describedAs("request is not the expected").isEqualTo(request);
  }
}

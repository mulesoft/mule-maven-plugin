/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.fabric;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.client.fabric.RuntimeFabricClient;
import org.mule.tools.client.fabric.model.Target;
import org.mule.tools.model.anypoint.RuntimeFabricDeployment;
import org.mule.tools.model.anypoint.RuntimeFabricDeploymentSettings;

import java.util.ArrayList;
import java.util.Arrays;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestBuilderTest {

  private static final String DOMAIN_TEST = "*.mydomain.com";
  private static final String TARGETS_RESPONSE = "[\n" +
      "   {\n" +
      "      \"id\":\"sampleId\",      \n" +
      "      \"agentInfo\":{\n" +
      "         \"name\":\"fabric1\",\n" +
      "         \"organizationId\":\"orgId\",\n" +
      "         \"status\":\"Connected\"         \n" +
      "      }\n" +
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

  private RequestBuilder requestBuilder;
  private RuntimeFabricClient runtimeFabricClientMock;
  private RuntimeFabricDeployment runtimeFabricDeployment;

  @Before
  public void setUp() {
    runtimeFabricClientMock = mock(RuntimeFabricClient.class);
    runtimeFabricDeployment = new RuntimeFabricDeployment();
    runtimeFabricDeployment.setMuleVersion("4.2.0");
    runtimeFabricDeployment.setApplicationName("test-app");
    runtimeFabricDeployment.setTarget("fabric1");
    runtimeFabricDeployment.setDeploymentSettings(new RuntimeFabricDeploymentSettings());
    requestBuilder = new RequestBuilder(runtimeFabricDeployment, runtimeFabricClientMock);

    ArrayList<String> domains = newArrayList(DOMAIN_TEST);

    when(runtimeFabricClientMock.getDomainInfo(any())).thenReturn(new Gson().toJsonTree(domains).getAsJsonArray());
    when(runtimeFabricClientMock.getTargetInfo(any())).thenReturn(new Gson().fromJson(TARGET_RESPONSE, JsonObject.class));
    when(runtimeFabricClientMock.getTargets()).thenReturn(new Gson().fromJson(TARGETS_RESPONSE, JsonArray.class));
  }

  @Test
  public void autoGenerateUrl() throws Exception {
    Target target = requestBuilder.buildTarget();
    String finalUrl = DOMAIN_TEST.replace("*", runtimeFabricDeployment.getApplicationName());

    assertThat("publicUrl is not the expected", target.deploymentSettings.getPublicUrl(), equalTo(finalUrl));
  }

  @Test
  public void useDefinedUrl() throws Exception {
    String definedUrl = "myapp.test.com";
    RuntimeFabricDeploymentSettings deploymentSettings = new RuntimeFabricDeploymentSettings();
    deploymentSettings.setPublicUrl(definedUrl);
    runtimeFabricDeployment.setDeploymentSettings(deploymentSettings);
    Target target = requestBuilder.buildTarget();

    assertThat("publicUrl is not the expected", target.deploymentSettings.getPublicUrl(), equalTo(definedUrl));
  }
}

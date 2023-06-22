/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client;

import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.client.arm.model.AuthorizationResponse;
import org.mule.tools.client.arm.model.Environment;
import org.mule.tools.client.arm.model.Environments;
import org.mule.tools.client.arm.model.UserInfo;
import org.mule.tools.client.authentication.AuthenticationServiceClient;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.google.gson.Gson;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Invocation;

class AbstractMuleClientTestCase {

  public static final int DEFAULT_PORT = 8080;
  public static final String BASE_URI = "http://localhost";

  private static final String USER_TOKEN = "808e932e808e932e";
  private static final String BEARER_USER_TOKEN = "bearer " + USER_TOKEN;

  private static final String NEW_USER_TOKEN = "808e932e808e123123";
  private static final String BEARER_NEW_USER_TOKEN = "bearer " + NEW_USER_TOKEN;

  private static final String USER_ID = "32e808808e9e932e";
  private static final String USER_ORG_ID = "932e808e932e808e";
  private static final String USER_ORG_NAME = "Mulesoft";

  private static ClientAndServer mockServer;

  private AbstractMuleClient client;
  private CloudHubDeployment cloudHubDeployment;

  private AbstractMuleClient createClient(String businessgroup) {
    cloudHubDeployment = new CloudHubDeployment();
    cloudHubDeployment.setUri(format("%s:%s", BASE_URI, DEFAULT_PORT));
    cloudHubDeployment.setUsername(null);
    cloudHubDeployment.setPassword(null);
    cloudHubDeployment.setEnvironment(null);
    cloudHubDeployment.setBusinessGroup(businessgroup);
    return new AbstractMuleClient(cloudHubDeployment, null) {};
  }

  @Test
  void emptyBusinessGroup() {
    client = createClient("");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length).isEqualTo(0);
  }

  @Test
  void nullBusinessGroup() {
    client = createClient(null);
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length).isEqualTo(0);
  }

  @Test
  void simpleBusinessGroup() {
    client = createClient("my-business-group");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length).isEqualTo(1);
    assertThat(result[0]).isEqualTo("my-business-group");
  }

  @Test
  void groupWithOneBackslash() {
    client = createClient("my\\\\business\\\\group");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length).isEqualTo(1);
    assertThat(result[0]).isEqualTo("my\\business\\group");
  }

  @Test
  void oneBackslashAtEnd() {
    client = createClient("root\\\\");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length).isEqualTo(1);
    assertThat(result[0]).isEqualTo("root\\");
  }

  @Test
  void twoBackslashAtEnd() {
    client = createClient("root\\\\\\\\");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length).isEqualTo(1);
    assertThat(result[0]).isEqualTo("root\\\\");
  }

  @Test
  void groupWithTwoBackslash() {
    client = createClient("my\\\\\\\\group");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length).isEqualTo(1);
    assertThat(result[0]).isEqualTo("my\\\\group");
  }

  @Test
  void hierarchicalBusinessGroup() {
    client = createClient("root\\leaf");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length).isEqualTo(2);
    assertThat(result[0]).isEqualTo("root");
    assertThat(result[1]).isEqualTo("leaf");
  }

  @Test
  void findEnvironmentByNameNoBusinessGroupAndNotPartOfMaster() {
    client = spy(createClient(EMPTY));
    doReturn(new Environments()).when(client).getEnvironments();
    assertThatThrownBy(() -> client.findEnvironmentByName("Production"))
        .isExactlyInstanceOf(RuntimeException.class)
        .hasMessageContaining("Please set the businessGroup in the plugin configuration in case your user have access only within a business unit.");
  }

  @Test
  void findEnvironmentByNameNoBusinessGroup() {
    client = spy(createClient(EMPTY));
    doReturn(null).when(client).getEnvironments();
    assertThatThrownBy(() -> client.findEnvironmentByName("Production"))
        .isExactlyInstanceOf(RuntimeException.class)
        .hasMessageContaining("Please set the businessGroup in the plugin configuration in case your user have access only within a business unit.");
  }


  @Test
  void configureRequestWithTokenExtension() {
    ArmDeployment armDeployment = new ArmDeployment();
    armDeployment.setUri(BASE_URI);
    armDeployment.setAuthToken("dummyToken");
    armDeployment.setEnvironment("dummyEnv");
    armDeployment.setBusinessGroupId("dummyGroupId");
    armDeployment.setArmInsecure(false);
    AbstractMuleClient client = spy(new ArmClient(armDeployment, null));
    Invocation.Builder builder = mock(Invocation.Builder.class);
    doReturn(new Environment()).when(client).findEnvironmentByName("dummyEnv");
    client.init();
    client.configureRequest(builder);
    verify(builder).header("x-anypoint-session-extend", true);
  }

  @Test
  void renewToken() {
    mockServer = startClientAndServer(DEFAULT_PORT);

    client = createClient("");

    AuthorizationResponse authResponse = new AuthorizationResponse();
    authResponse.access_token = USER_TOKEN;

    mockServer
        .when(request().withMethod(HttpMethod.POST).withPath(AuthenticationServiceClient.LOGIN))
        .respond(response().withStatusCode(OK.getStatusCode()).withBody(new Gson().toJson(authResponse), MediaType.JSON_UTF_8));

    mockServer
        .when(request().withMethod(HttpMethod.GET).withPath(AuthenticationServiceClient.ME).withHeader(HttpHeaders.AUTHORIZATION,
                                                                                                       BEARER_USER_TOKEN))
        .respond(response().withStatusCode(UNAUTHORIZED.getStatusCode()).withBody(AbstractMuleClient.UNAUTHORIZED));

    try {
      client.getMe();
    } catch (RuntimeException httpException) {
      assertThat(httpException.getMessage()).isEqualTo("Unauthorized Access. Please verify that authToken is valid.");
    }

    mockServer.reset();

    String json = "{\n"
        + "  \"user\": {\n"
        + "    \"id\": \"" + USER_ID + "\",\n"
        + "    \"organizationId\": \"" + USER_ORG_ID + "\",\n"
        + "    \"username\": \"muleruntime\",\n"
        + "    \"enabled\": true,\n"
        + "    \"organization\": {\n"
        + "      \"name\": \"" + USER_ORG_NAME + "\",\n"
        + "      \"id\": \"" + USER_ORG_ID + "\"\n"
        + "    },\n"
        + "    \"memberOfOrganizations\": [\n"
        + "      {\n"
        + "        \"name\": \"" + USER_ORG_NAME + "\",\n"
        + "        \"id\": \"" + USER_ORG_ID + "\",\n"
        + "        \"subOrganizationIds\": []\n"
        + "      }\n"
        + "    ]\n"
        + "  }\n"
        + "}";

    mockServer
        .when(request().withMethod(HttpMethod.GET).withPath(AuthenticationServiceClient.ME)
            .withHeader(AuthenticationServiceClient.AUTHORIZATION_HEADER, BEARER_NEW_USER_TOKEN))
        .respond(response().withStatusCode(OK.getStatusCode()).withBody(json, MediaType.JSON_UTF_8));

    AuthorizationResponse newAuthResponse = new AuthorizationResponse();
    newAuthResponse.access_token = NEW_USER_TOKEN;

    mockServer
        .when(request().withMethod(HttpMethod.POST).withPath(AuthenticationServiceClient.LOGIN))
        .respond(response().withStatusCode(OK.getStatusCode()).withBody(new Gson().toJson(newAuthResponse),
                                                                        MediaType.JSON_UTF_8));

    client.renewToken();

    UserInfo userInfo = client.getMe();

    assertThat(userInfo.user.id).isEqualTo(USER_ID);
    assertThat(userInfo.user.organization.id).isEqualTo(USER_ORG_ID);

    mockServer.stop();
  }
}

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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
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

import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.integration.ClientAndServer;


public class AbstractMuleClientTestCase {

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

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

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
  public void emptyBusinessGroup() {
    client = createClient("");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length, equalTo(0));
  }

  @Test
  public void nullBusinessGroup() {
    client = createClient(null);
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length, equalTo(0));
  }

  @Test
  public void simpleBusinessGroup() {
    client = createClient("my-business-group");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length, equalTo(1));
    assertThat(result[0], equalTo("my-business-group"));
  }

  @Test
  public void groupWithOneBackslash() {
    client = createClient("my\\\\business\\\\group");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length, equalTo(1));
    assertThat(result[0], equalTo("my\\business\\group"));
  }

  @Test
  public void oneBackslashAtEnd() {
    client = createClient("root\\\\");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length, equalTo(1));
    assertThat(result[0], equalTo("root\\"));
  }

  @Test
  public void twoBackslashAtEnd() {
    client = createClient("root\\\\\\\\");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length, equalTo(1));
    assertThat(result[0], equalTo("root\\\\"));
  }

  @Test
  public void groupWithTwoBackslash() {
    client = createClient("my\\\\\\\\group");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length, equalTo(1));
    assertThat(result[0], equalTo("my\\\\group"));
  }

  @Test
  public void hierarchicalBusinessGroup() {
    client = createClient("root\\leaf");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length, equalTo(2));
    assertThat(result[0], equalTo("root"));
    assertThat(result[1], equalTo("leaf"));
  }

  @Test
  public void findEnvironmentByNameNoBusinessGroupAndNotPartOfMaster() {
    expectedException.expect(RuntimeException.class);
    expectedException
        .expectMessage("Please set the businessGroup in the plugin configuration in case your user have access only within a business unit.");
    client = spy(createClient(EMPTY));
    doReturn(new Environments()).when(client).getEnvironments();
    client.findEnvironmentByName("Production");
  }

  @Test
  public void findEnvironmentByNameNoBusinessGroup() {
    expectedException.expect(RuntimeException.class);
    expectedException
        .expectMessage("Please set the businessGroup in the plugin configuration in case your user have access only within a business unit.");
    client = spy(createClient(EMPTY));
    doReturn(null).when(client).getEnvironments();
    client.findEnvironmentByName("Production");
  }


  @Test
  public void configureRequestWithTokenExtension() {
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
  public void renewToken() {
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
      MatcherAssert.assertThat(httpException.getMessage(),
                               equalTo("Unauthorized Access. Please verify that authToken is valid."));
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

    MatcherAssert.assertThat(userInfo.user.id, equalTo(USER_ID));
    MatcherAssert.assertThat(userInfo.user.organization.id, equalTo(USER_ORG_ID));

    mockServer.stop();
  }
}

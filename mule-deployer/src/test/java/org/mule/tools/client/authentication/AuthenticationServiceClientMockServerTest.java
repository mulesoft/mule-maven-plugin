/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.authentication;

import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import org.mule.tools.client.arm.model.AuthorizationResponse;
import org.mule.tools.client.arm.model.Environment;
import org.mule.tools.client.arm.model.Environments;
import org.mule.tools.client.arm.model.Organization;
import org.mule.tools.client.arm.model.User;
import org.mule.tools.client.arm.model.UserInfo;
import org.mule.tools.client.authentication.model.Credentials;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.google.gson.Gson;

import java.util.List;
import java.util.Vector;

import javax.ws.rs.HttpMethod;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;

/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class AuthenticationServiceClientMockServerTest {

  private static final String USERNAME = "username";
  private static final String PASSWORD = "password";
  private static final String BASE_URI = "http://localhost";

  private static final String USER_TOKEN = "808e932e808e932e";
  private static final String BEARER_USER_TOKEN = "Bearer " + USER_TOKEN;

  private static final String USER_ID = "32e808808e9e932e";

  private static final String USER_ORG1_ID = "932e808e932e808e";
  private static final String USER_ORG1_NAME = "Mulesoft1";

  private static final String USER_ORG2_ID = "932e808e932e08ea";
  private static final String USER_ORG2_NAME = "Mulesoft2";

  private static final String USER_ENVIRONMENT1_ID = "932e808e932e1dfa";
  private static final String USER_ENVIRONMENT1_NAME = "ENV1";
  private static final boolean USER_ENVIRONMENT1_PRODUCTION = true;
  private static final String USER_ENVIRONMENT1_ORG_ID = USER_ORG1_ID;

  private static final String USER_ENVIRONMENT2_ID = "932e808eaa2e1dfa";
  private static final String USER_ENVIRONMENT2_NAME = "ENV2";
  private static final boolean USER_ENVIRONMENT2_PRODUCTION = false;
  private static final String USER_ENVIRONMENT2_ORG_ID = USER_ORG1_ID;

  private Credentials credentials;
  private AuthenticationServiceClient client;

  public static final int DEFAULT_PORT = 8080;
  private static ClientAndServer mockServer;

  @BeforeClass
  public static void before() {
    mockServer = startClientAndServer(DEFAULT_PORT);
  }

  @AfterClass
  public static void after() {
    mockServer.stop();
  }

  @Before
  public void setup() {
    mockServer.reset();

    AuthorizationResponse authResponse = new AuthorizationResponse();
    authResponse.access_token = USER_TOKEN;

    mockServer
        .when(request().withMethod(HttpMethod.POST).withPath(AuthenticationServiceClient.LOGIN))
        .respond(response().withStatusCode(OK.getStatusCode()).withBody(new Gson().toJson(authResponse), MediaType.JSON_UTF_8));

    credentials = new Credentials(USERNAME, PASSWORD);
    client = new AuthenticationServiceClient(format("%s:%s", BASE_URI, DEFAULT_PORT), true);
  }

  @Test
  public void getMe() {
    UserInfo userInfoResponse = new UserInfo();
    userInfoResponse.user = new User();
    userInfoResponse.user.id = USER_ID;
    userInfoResponse.user.organization = new Organization();
    userInfoResponse.user.organization.id = USER_ORG1_ID;

    mockServer
        .when(request().withMethod(HttpMethod.GET).withPath(AuthenticationServiceClient.ME).withHeader(HttpHeaders.AUTHORIZATION,
                                                                                                       BEARER_USER_TOKEN))
        .respond(response().withStatusCode(OK.getStatusCode()).withBody(new Gson().toJson(userInfoResponse),
                                                                        MediaType.JSON_UTF_8));

    client.getBearerToken(credentials);

    UserInfo userInfo = client.getMe();

    assertThat(userInfo.user.id, equalTo(USER_ID));
    assertThat(userInfo.user.organization.id, equalTo(USER_ORG1_ID));
  }

  @Test
  public void getOrganizations() {
    client.getBearerToken(credentials);

    Organization organization1 = new Organization();
    organization1.id = USER_ORG1_ID;
    organization1.name = USER_ORG1_NAME;

    Organization organization2 = new Organization();
    organization2.id = USER_ORG2_ID;
    organization2.name = USER_ORG2_NAME;

    List<Organization> organizations = new Vector<>();
    organizations.add(organization1);
    organizations.add(organization2);

    mockServer
        .when(request().withMethod(HttpMethod.GET).withPath(AuthenticationServiceClient.ORGANIZATIONS)
            .withHeader(HttpHeaders.AUTHORIZATION, BEARER_USER_TOKEN))
        .respond(response().withStatusCode(OK.getStatusCode()).withBody(new Gson().toJson(organizations), MediaType.JSON_UTF_8));

    List<Organization> organizationsResponse = client.getOrganizations();

    assertThat(organizationsResponse.size(), equalTo(2));
    assertThat(organizationsResponse.get(0).id, equalTo(USER_ORG1_ID));
    assertThat(organizationsResponse.get(0).name, equalTo(USER_ORG1_NAME));

    assertThat(organizationsResponse.get(1).id, equalTo(USER_ORG2_ID));
    assertThat(organizationsResponse.get(1).name, equalTo(USER_ORG2_NAME));
  }

  @Test
  public void getEnvironments() {
    client.getBearerToken(credentials);

    UserInfo userInfoResponse = new UserInfo();
    userInfoResponse.user = new User();
    userInfoResponse.user.id = USER_ID;
    userInfoResponse.user.organization = new Organization();
    userInfoResponse.user.organization.id = USER_ORG1_ID;

    mockServer
        .when(request().withMethod(HttpMethod.GET).withPath(AuthenticationServiceClient.ME).withHeader(HttpHeaders.AUTHORIZATION,
                                                                                                       BEARER_USER_TOKEN))
        .respond(response().withStatusCode(OK.getStatusCode()).withBody(new Gson().toJson(userInfoResponse),
                                                                        MediaType.JSON_UTF_8));


    Environment environment1 = new Environment();
    environment1.id = USER_ENVIRONMENT1_ID;
    environment1.isProduction = USER_ENVIRONMENT1_PRODUCTION;
    environment1.name = USER_ENVIRONMENT1_NAME;
    environment1.organizationId = USER_ENVIRONMENT1_ORG_ID;

    Environment environment2 = new Environment();
    environment2.id = USER_ENVIRONMENT2_ID;
    environment2.isProduction = USER_ENVIRONMENT2_PRODUCTION;
    environment2.name = USER_ENVIRONMENT2_NAME;
    environment2.organizationId = USER_ENVIRONMENT2_ORG_ID;

    Environments environments = new Environments();
    environments.data = new Environment[] {environment1, environment2};

    mockServer
        .when(request().withMethod(HttpMethod.GET).withPath(String.format(AuthenticationServiceClient.ENVIRONMENTS, USER_ORG1_ID))
            .withHeader(HttpHeaders.AUTHORIZATION, BEARER_USER_TOKEN))
        .respond(response().withStatusCode(OK.getStatusCode()).withBody(new Gson().toJson(environments), MediaType.JSON_UTF_8));

    UserInfo userInfo = client.getMe();

    List<Environment> environmentsResponse = client.getEnvironments(userInfo.user.organization.id);
    assertThat(environmentsResponse.size(), equalTo(2));

    assertThat(environmentsResponse.get(0).id, equalTo(USER_ENVIRONMENT1_ID));
    assertThat(environmentsResponse.get(0).name, equalTo(USER_ENVIRONMENT1_NAME));
    assertThat(environmentsResponse.get(0).isProduction, equalTo(USER_ENVIRONMENT1_PRODUCTION));
    assertThat(environmentsResponse.get(0).organizationId, equalTo(USER_ENVIRONMENT1_ORG_ID));

    assertThat(environmentsResponse.get(1).id, equalTo(USER_ENVIRONMENT2_ID));
    assertThat(environmentsResponse.get(1).name, equalTo(USER_ENVIRONMENT2_NAME));
    assertThat(environmentsResponse.get(1).isProduction, equalTo(USER_ENVIRONMENT2_PRODUCTION));
    assertThat(environmentsResponse.get(1).organizationId, equalTo(USER_ENVIRONMENT2_ORG_ID));
  }
}

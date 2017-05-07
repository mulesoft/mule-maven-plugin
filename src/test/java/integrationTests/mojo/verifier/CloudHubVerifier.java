/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integrationTests.mojo.verifier;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.xml.ws.BindingProvider.USERNAME_PROPERTY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class CloudHubVerifier {

  private static final String URI = "https://anypoint.mulesoft.com";
  private static final String ME = "/accounts/api/me";
  private static final String LOGIN = "/accounts/login";
  private static final String APPLICATION = "/cloudhub/api/applications/maven-plugin-cloudhub-deploy-test";
  private static final String ENVIRONMENTS_PATH = "/accounts/api/organizations/%s/environments";
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String ENV_ID_HEADER = "X-ANYPNT-ENV-ID";
  private static final String ORG_ID_HEADER = "X-ANYPNT-ORG-ID";
  private static final ClientBuilder clientBuilder = ClientBuilder.newBuilder();
  private static final Client client = clientBuilder.newClient();
  private static final int ATTEMPTS = 300;
  private static final String DATA = "data";
  private static final String ENVIRONMENT_NAME_KEY = "name";
  private static final String BEARER_FIELD = "bearer ";
  private static final String ENVIRONMENT_ID_KEY = "id";
  private static final String PRODUCTION_ENVIROMENT = "Production";
  private static final String USER_KEY = "user";
  private static final String ORGANIZATION_KEY = "organization";
  private static final String ORGANIZATION_ID_KEY = "id";
  private static final String USERNAME_ENVIRONMENT_VARIABLE = "username";
  private static final String PASSWORD_ENVIRONMENT_VARIABLE = "password";
  private static final String ACCESS_TOKEN_KEY = "access_token";
  private WebTarget target;
  private String response;
  private JSONObject responseJson;
  private IDs ids = new IDs();
  private String bearerToken;

  public void verify() {
    login();
    obtainOrganizationId();
    obtainEnvironmentId();
    boolean wasDeleted = deleteApplication();
    assertThat("Application was not deployed", wasDeleted, is(true));
  }

  private boolean deleteApplication() {
    boolean hasReceivedDeleteConfirmation = false;
    target = client.target(URI).path(APPLICATION);
    Response deleteResponse =
        target.request(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_HEADER, BEARER_FIELD + bearerToken)
            .header(ENV_ID_HEADER, ids.getEnvironmentId()).header(ORG_ID_HEADER, ids.getOrganizationId()).delete();
    hasReceivedDeleteConfirmation = deleteResponse.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL;
    int repeat = ATTEMPTS;
    boolean deleted = false;
    while (repeat > 0 && !deleted) {
      target = ClientBuilder.newClient().target(URI).path(APPLICATION);
      Response response = target.request(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_HEADER, BEARER_FIELD + bearerToken)
          .header(ENV_ID_HEADER, ids.getEnvironmentId()).header(ORG_ID_HEADER, ids.getOrganizationId()).get();
      deleted = response.getStatus() == Response.Status.NOT_FOUND.getStatusCode();
    }
    return hasReceivedDeleteConfirmation && deleted;
  }

  private void obtainEnvironmentId() {
    target = client.target(URI).path(String.format(ENVIRONMENTS_PATH, ids.getOrganizationId()));
    response =
        target.request(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_HEADER, BEARER_FIELD + bearerToken)
            .get(String.class);
    responseJson = new JSONObject(response);
    JSONArray environments = responseJson.getJSONArray(DATA);
    for (int i = 0; i < environments.length(); ++i) {
      JSONObject environment = environments.getJSONObject(i);
      if (PRODUCTION_ENVIROMENT.equals(environment.getString(ENVIRONMENT_NAME_KEY))) {
        ids.setEnvironmentId(environment.getString(ENVIRONMENT_ID_KEY));
        break;
      }
    }
  }

  private void obtainOrganizationId() {
    target = client.target(URI).path(ME);
    response =
        target.request(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_HEADER, BEARER_FIELD + bearerToken)
            .get(String.class);
    responseJson = new JSONObject(response);
    ids.setOrganizationId(responseJson.getJSONObject(USER_KEY).getJSONObject(ORGANIZATION_KEY).getString(ORGANIZATION_ID_KEY));
  }

  private void login() {
    WebTarget target = client.target(URI).path(LOGIN);;
    String username = System.getProperty(USERNAME_ENVIRONMENT_VARIABLE);
    String password = System.getProperty(PASSWORD_ENVIRONMENT_VARIABLE);
    Entity requestJson = Entity.json("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}");
    String response = target.request(MediaType.APPLICATION_JSON_TYPE).post(requestJson, String.class);
    JSONObject responseJson = new JSONObject(response);
    bearerToken = responseJson.getString(ACCESS_TOKEN_KEY);
  }

  private class IDs {

    private String organizationId;
    private String environmentId;

    public String getOrganizationId() {
      return organizationId;
    }

    public void setOrganizationId(String organizationId) {
      this.organizationId = organizationId;
    }

    public String getEnvironmentId() {
      return environmentId;
    }

    public void setEnvironmentId(String environmentId) {
      this.environmentId = environmentId;
    }
  }
}

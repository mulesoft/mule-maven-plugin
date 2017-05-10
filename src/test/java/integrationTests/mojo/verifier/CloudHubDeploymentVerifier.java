/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integrationTests.mojo.verifier;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class CloudHubDeploymentVerifier {

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
  private static final int ATTEMPTS = 10;
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
  private static final String APPLICATIONS = "/cloudhub/api/applications";
  private static final String APPLICATION_NAME = "maven-plugin-cloudhub-deploy-test";
  private static final long SLEEP_TIME = 30000;
  public static final String DEPLOYED_STATUS = "STARTED";
  public static final String UNDEPLOYED_STATUS = "UNDEPLOYED";
  private static final String STATUS_JSON_KEY = "status";
  private WebTarget target;
  private String response;
  private JSONObject responseJson;
  private IDs ids = new IDs();
  private String bearerToken;
  private Logger log = LoggerFactory.getLogger(this.getClass());


  public void verifyIsDeployed() throws InterruptedException, TimeoutException {
    loginAndGetNecessaryIds();
    assertThat("Application was not deployed", validateStatus(DEPLOYED_STATUS), is(true));
    deleteApplication();
  }

  public boolean validateStatus(String status) throws InterruptedException, TimeoutException {
    log.info("Checking application " + APPLICATION_NAME + " for status " + status + "...");
    int repeat = ATTEMPTS;
    boolean keepValidating = false;
    while (repeat > 0 && !keepValidating) {
      JSONObject application = getApplication(getApplications());
      keepValidating = !isExpectedStatus(status, application);
      if (keepValidating) {
        Thread.sleep(SLEEP_TIME);
      }
      repeat--;
    }
    if (repeat == 0 && !keepValidating) {
      throw new TimeoutException("Validating status " + status + " for application " + APPLICATION_NAME
          + " has exceed the maximum number of attempts.");
    }
    return keepValidating;
  }

  private boolean isExpectedStatus(String status, JSONObject application) {
    if (DEPLOYED_STATUS.equals(status) && application != null) {
      return status.equals(application.getString(STATUS_JSON_KEY));
    }
    if (UNDEPLOYED_STATUS.equals(status)) {
      return application == null || UNDEPLOYED_STATUS.equals(application.getString(STATUS_JSON_KEY));
    }
    return false;
  }

  public void verifyIsUndeployed() throws InterruptedException, TimeoutException {
    loginAndGetNecessaryIds();
    assertThat("Application was not undeployed", validateStatus(UNDEPLOYED_STATUS), is(true));
    deleteApplication();
  }

  private void loginAndGetNecessaryIds() {
    login();
    obtainOrganizationId();
    obtainEnvironmentId();
  }

  private void deleteApplication() {
    target = client.target(URI).path(APPLICATION);
    Response deleteResponse = delete(target);
    assertThat("Application was not deleted", deleteResponse.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL);
  }

  private Response delete(WebTarget target) {
    log.info("Deleting application " + target.getUri());
    return target.request(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_HEADER, BEARER_FIELD + bearerToken)
        .header(ENV_ID_HEADER, ids.getEnvironmentId()).header(ORG_ID_HEADER, ids.getOrganizationId()).delete();
  }

  private void obtainEnvironmentId() {
    log.info("Getting environment ID...");
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
    log.info("Getting organization ID...");
    target = client.target(URI).path(ME);
    response =
        target.request(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_HEADER, BEARER_FIELD + bearerToken)
            .get(String.class);
    responseJson = new JSONObject(response);
    ids.setOrganizationId(responseJson.getJSONObject(USER_KEY).getJSONObject(ORGANIZATION_KEY).getString(ORGANIZATION_ID_KEY));
  }

  private void login() {
    log.info("Trying to log in to Anypoint...");
    WebTarget target = client.target(URI).path(LOGIN);;
    String username = System.getProperty(USERNAME_ENVIRONMENT_VARIABLE);
    String password = System.getProperty(PASSWORD_ENVIRONMENT_VARIABLE);
    Entity requestJson = Entity.json("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}");
    String response = target.request(MediaType.APPLICATION_JSON_TYPE).post(requestJson, String.class);
    JSONObject responseJson = new JSONObject(response);
    bearerToken = responseJson.getString(ACCESS_TOKEN_KEY);
    log.info("Login is successful, bearer token is " + bearerToken);
  }

  public JSONObject getApplication(JSONArray applications) {
    log.info("Trying to find application " + APPLICATION_NAME + " within all existent applications in CloudHub account...");
    for (int i = 0; i < applications.length(); ++i) {
      if (APPLICATION_NAME.equals(applications.getJSONObject(i).getString("domain"))) {
        log.info("The application " + APPLICATION_NAME + " was found.");
        return applications.getJSONObject(i);
      }
    }
    log.info("The application " + APPLICATION_NAME + " could not be found.");
    return null;
  }

  public JSONArray getApplications() {
    log.info("Fetching all applications from CloudHub account...");
    target = ClientBuilder.newClient().target(URI).path(APPLICATIONS);
    response = target.request(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_HEADER, "bearer " + bearerToken)
        .header(ENV_ID_HEADER, ids.getEnvironmentId()).header(ORG_ID_HEADER, ids.getOrganizationId()).get(String.class);
    log.info("Applications fetched.");
    return new JSONArray(response);
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

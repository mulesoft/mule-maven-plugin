/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package integrationTests.mojo.environment.verifier;

import integrationTests.mojo.environment.ID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArmDeploymentVerifier {

  private static final String uri = "https://anypoint.mulesoft.com";
  private static final String ME = "/accounts/api/me";
  private static final String LOGIN = "/accounts/login";
  private static final String ENVIRONMENTS_PATH = "/accounts/api/organizations/%s/environments";
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String ENV_ID_HEADER = "X-ANYPNT-ENV-ID";
  private static final String ORG_ID_HEADER = "X-ANYPNT-ORG-ID";
  private static final ClientBuilder clientBuilder = ClientBuilder.newBuilder();
  private static final Client client = clientBuilder.newClient();
  private static final String DATA = "data";
  private static final String SERVERS = "/hybrid/api/v1/servers";
  private static final String ENVIRONMENT_NAME_KEY = "name";
  private static final String BEARER_FIELD = "bearer ";
  private static final String ENVIRONMENT_ID_KEY = "id";
  private static final String PRODUCTION_ENVIROMENT = "Production";
  private static final String USER_KEY = "user";
  private static final String ORGANIZATION_KEY = "organization";
  private static final String ORGANIZATION_ID_KEY = "id";
  private static final String USERNAME_ENVIRONMENT_VARIABLE = "username";
  private static final String PASSWORD_ENVIRONMENT_VARIABLE = "password";
  private static final String STARTED_STATUS = "STARTED";
  private static final String APPLICATIONS = "/hybrid/api/v1/applications";



  private static final String ACCESS_TOKEN_KEY = "access_token";
  public static final int ATTEMPTS = 30;
  private static final long SLEEP_TIME = 30000;
  private static final String DESIRED_STATUS = "desiredStatus";
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private WebTarget target;
  private String response;
  private JSONObject responseJson;
  private ID ids = new ID();
  private String bearerToken;
  private static Runtime runtime = Runtime.getRuntime();


  public void verifyIsDeployed(String applicationName) throws InterruptedException, TimeoutException {
    loginAndGetNecessaryIds();
    assertThat("Application was not deployed", validateStatus(applicationName, STARTED_STATUS), is(true));
  }

  public boolean validateStatus(String applicationName, String status) throws InterruptedException, TimeoutException {
    int repeat = ATTEMPTS;
    boolean keepValidating = true;
    while (repeat > 0 && keepValidating) {
      JSONObject application = getApplication(applicationName, getApplications());
      keepValidating = !isExpectedStatus(status, application);
      if (keepValidating) {
        Thread.sleep(SLEEP_TIME);
      }
      repeat--;
    }
    if (repeat == 0 && keepValidating) {
      throw new TimeoutException("Validating status " + status + " for application " + applicationName
          + " has exceed the maximum number of attempts.");
    }
    return !keepValidating;
  }

  private boolean isExpectedStatus(String status, JSONObject application) {
    return status.equals(application.getString(DESIRED_STATUS));
  }

  private void loginAndGetNecessaryIds() {
    login();
    obtainOrganizationId();
    obtainEnvironmentId();
  }

  private void obtainEnvironmentId() {
    target = client.target(uri).path(String.format(ENVIRONMENTS_PATH, ids.getOrganizationId()));
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
    target = client.target(uri).path(ME);
    response =
        target.request(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_HEADER, BEARER_FIELD + bearerToken)
            .get(String.class);
    responseJson = new JSONObject(response);
    ids.setOrganizationId(responseJson.getJSONObject(USER_KEY).getJSONObject(ORGANIZATION_KEY).getString(ORGANIZATION_ID_KEY));
  }

  private void login() {
    WebTarget target = client.target(uri).path(LOGIN);;
    String username = System.getProperty(USERNAME_ENVIRONMENT_VARIABLE);
    String password = System.getProperty(PASSWORD_ENVIRONMENT_VARIABLE);
    Entity requestJson = Entity.json("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}");
    String response = target.request(MediaType.APPLICATION_JSON_TYPE).post(requestJson, String.class);
    JSONObject responseJson = new JSONObject(response);
    bearerToken = responseJson.getString(ACCESS_TOKEN_KEY);
  }

  public JSONObject getApplication(String applicationName, JSONArray applications) {
    for (int i = 0; i < applications.length(); ++i) {
      if ("arm-deploy".equals(applications.getJSONObject(i).getJSONObject("artifact").getString("name"))) {
        return applications.getJSONObject(i);
      }
    }
    return null;
  }

  public JSONArray getApplications() {
    target = ClientBuilder.newClient().target(uri).path(APPLICATIONS);
    response = target.request(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_HEADER, "bearer " + bearerToken)
        .header(ENV_ID_HEADER, ids.getEnvironmentId()).header(ORG_ID_HEADER, ids.getOrganizationId()).get(String.class);
    responseJson = new JSONObject(response);
    return responseJson.getJSONArray(DATA);

  }

  public void killMuleProcesses() throws IOException {
    String[] commands =
        {"ps", "-ax", "|", "grep", "mule", "|", "grep", "wrapper", "|", "cut", "-c", "1-5", "|", "xargs", "kill", "-9"};
    runtime.exec(commands);
  }

}

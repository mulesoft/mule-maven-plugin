/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integrationTests.mojo.environment.setup;


import integrationTests.mojo.environment.ID;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkState;
import static org.junit.Assert.fail;

public class ArmEnvironment {

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
  private static final String ENVIRONMENT_NAME_KEY = "name";
  private static final String BEARER_FIELD = "bearer ";
  private static final String ENVIRONMENT_ID_KEY = "id";
  public static final String PRODUCTION_ENVIROMENT = "Production";
  private static final String USER_KEY = "user";
  private static final String ORGANIZATION_KEY = "organization";
  private static final String ORGANIZATION_ID_KEY = "id";
  private static final String USERNAME_ENVIRONMENT_VARIABLE = "username";
  private static final String PASSWORD_ENVIRONMENT_VARIABLE = "password";
  private static final String NAME = "name";

  private static final String ACCESS_TOKEN_KEY = "access_token";
  private static final String REGISTRATION = "/hybrid/api/v1/servers/registrationToken";
  public static final int ATTEMPTS = 30;
  private static final String SERVER_ID_KEY = "id";
  private final String instanceName;
  private Logger log;
  private static final int ONE_MINUTE = 60000;
  private static final String MULE_HOME_FOLDER_PREFIX = "/mule-enterprise-standalone-";
  private static String muleVersion;
  private static final String AGENT_JKS_RELATIVE_PATH = "/conf/mule-agent.jks";
  private static final String AGENT_YMS_RELATIVE_PATH = "/conf/mule-agent.yml";
  private static final String EXECUTABLE_FOLDER_RELATIVE_PATH = "/bin/mule";
  private static final String AMC_SETUP_RELATIVE_FOLDER = "/bin/amc_setup";
  private static final String ARM_CONFIGURATION_OPTION = "-H";
  private static final int NORMAL_TERMINATION = 0;
  private static final String START_AGENT_COMMAND = "start";
  private static final String STOP_AGENT_COMMAND = "stop";
  private static final String SERVERS = "/hybrid/api/v1/servers";
  private static String muleHome;
  private WebTarget target;
  private String response;
  private JSONObject responseJson;
  private ID ids = new ID();
  private String bearerToken;
  private static Runtime runtime = Runtime.getRuntime();
  private static Process applicationProcess;

  public ArmEnvironment(String muleVersion, String instanceName) {
    log = LoggerFactory.getLogger(this.getClass());
    this.instanceName = instanceName;
    this.muleVersion = muleVersion;
  }

  public void start() throws InterruptedException, TimeoutException, IOException {
    login();
    obtainOrganizationId();
    obtainEnvironmentId();
    registerArmServer();
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

  private void obtainOrganizationId() {
    WebTarget target = client.target(uri).path(ME);
    response =
        target.request(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_HEADER, BEARER_FIELD + bearerToken)
            .get(String.class);
    responseJson = new JSONObject(response);
    ids.setOrganizationId(responseJson.getJSONObject(USER_KEY).getJSONObject(ORGANIZATION_KEY).getString(ORGANIZATION_ID_KEY));
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

  private void registerArmServer() throws IOException, InterruptedException, TimeoutException {
    target = client.target(uri).path(REGISTRATION);
    response = target.request(MediaType.APPLICATION_JSON_TYPE)
        .header(AUTHORIZATION_HEADER, BEARER_FIELD + bearerToken)
        .header(ENV_ID_HEADER, ids.getEnvironmentId())
        .header(ORG_ID_HEADER, ids.getOrganizationId())
        .get(String.class);
    responseJson = new JSONObject(response);
    String token = responseJson.getString(DATA);

    Path currentRelativePath = Paths.get("");
    String targetFolder = currentRelativePath.toAbsolutePath().toString() + File.separator + "target";
    muleHome = targetFolder + MULE_HOME_FOLDER_PREFIX + muleVersion;
    deleteFile(muleHome + AGENT_JKS_RELATIVE_PATH);
    deleteFile(muleHome + AGENT_YMS_RELATIVE_PATH);
    registerServer(token);
    startMule();
    Integer serverId = assertServerExists();
    assertServerIsRunning(serverId, 4 * ONE_MINUTE);
  }

  private void deleteFile(String pathname) {
    File file = new File(pathname);
    if (file.exists()) {
      file.delete();
    }
  }

  private void executeAction(Runnable onRestart, String action, String[] commands) throws IOException, InterruptedException {
    log.info("Trying to " + action + " mule...");
    int tries = 0;
    do {
      if (tries != 0) {
        log.info("Failed to " + action + " mule. Trying to " + action + " again...");
        onRestart.run();
      }
      applicationProcess = runtime.exec(commands);
      applicationProcess.waitFor();
      tries++;
      if (tries == ATTEMPTS) {
        fail("Could not " + action + " mule");
      }
    } while (applicationProcess.exitValue() != NORMAL_TERMINATION);
    log.info("Mule successfully stopped.");
  }

  private void registerServer(String token) throws IOException, InterruptedException {
    String amcExecutable = muleHome + AMC_SETUP_RELATIVE_FOLDER;
    String[] commands = {amcExecutable, ARM_CONFIGURATION_OPTION, token, instanceName};
    executeAction(() -> {
    }, "register server on", commands);
  }

  private void startMule() throws InterruptedException, IOException {
    String[] commands = {getMuleExecutable(), START_AGENT_COMMAND};
    executeAction(this::stopMule, "start", commands);
  }

  private void stopMule() {
    String[] commands = {getMuleExecutable(), STOP_AGENT_COMMAND};
    try {
      executeAction(() -> {
      }, "start", commands);
    } catch (IOException | InterruptedException e) {
      log.error("Failed to stop mule");
    }
  }

  private Integer assertServerExists() {
    target = client.target(uri).path(SERVERS);
    response = target.request(MediaType.APPLICATION_JSON_TYPE)
        .header(AUTHORIZATION_HEADER, BEARER_FIELD + bearerToken)
        .header(ENV_ID_HEADER, ids.getEnvironmentId())
        .header(ORG_ID_HEADER, ids.getOrganizationId())
        .get(String.class);
    responseJson = new JSONObject(response);
    JSONArray instances = responseJson.getJSONArray(DATA);
    Integer serverId = null;
    for (int i = 0; i < instances.length(); ++i) {
      JSONObject instance = instances.getJSONObject(i);
      if (instanceName.equals(instance.getString(NAME))) {
        serverId = (Integer) instance.get(SERVER_ID_KEY);
      }
    }
    checkState(serverId != null, "Server not found");
    return serverId;
  }

  private void assertServerIsRunning(Integer serverId, int timeoutInSeconds) throws TimeoutException, InterruptedException {
    int oneSecond = 1000;
    int count = 0;
    while (count != timeoutInSeconds) {
      Thread.sleep(oneSecond);
      count++;
      if (isRunning(serverId)) {
        return;
      }
    }
    throw new TimeoutException("Waiting for Standalone to accept deployments has timeout.");
  }

  private boolean isRunning(Integer serverId) {
    target = client.target(uri).path(SERVERS + "/" + serverId);
    response = target.request(MediaType.APPLICATION_JSON_TYPE)
        .header(AUTHORIZATION_HEADER, BEARER_FIELD + bearerToken)
        .header(ENV_ID_HEADER, ids.getEnvironmentId())
        .header(ORG_ID_HEADER, ids.getOrganizationId())
        .get(String.class);
    responseJson = new JSONObject(response);
    return StringUtils.equals(responseJson.getJSONObject(DATA).getString("status"), "RUNNING");
  }

  private String getMuleExecutable() {
    return muleHome + EXECUTABLE_FOLDER_RELATIVE_PATH;
  }
}

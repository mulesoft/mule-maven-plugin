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


import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;

import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.client.arm.model.Environment;
import org.mule.tools.client.arm.model.Servers;
import org.mule.tools.client.arm.model.Target;
import org.mule.tools.client.arm.model.UserInfo;
import org.mule.tools.client.authentication.AuthenticationServiceClient;
import org.mule.tools.client.authentication.model.Credentials;
import org.mule.tools.model.anypoint.ArmDeployment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integrationTests.mojo.environment.ID;

public class ArmEnvironment {

  // TODO this should be part of the AbstractMuleClient
  private static final String BASE_URL = "https://anypoint.mulesoft.com";

  // TODO all this should go to a command line utils
  private static final String STOP_AGENT_COMMAND = "stop";
  private static final String START_AGENT_COMMAND = "start";
  private static final String ARM_CONFIGURATION_OPTION = "-H";
  private static final String AMC_SETUP_RELATIVE_FOLDER = "/bin/amc_setup";
  private static final String EXECUTABLE_FOLDER_RELATIVE_PATH = "/bin/mule";
  private static final String MULE_HOME_FOLDER_PREFIX = "/mule-enterprise-standalone-";

  private static final String AGENT_YMS_RELATIVE_PATH = "/conf/mule-agent.yml";
  private static final String AGENT_JKS_RELATIVE_PATH = "/conf/mule-agent.jks";


  public static final String PRODUCTION_ENVIRONMENT = "Production";
  private static final String USERNAME_ENVIRONMENT_VARIABLE = "username";
  private static final String PASSWORD_ENVIRONMENT_VARIABLE = "password";

  public static final int ATTEMPTS = 30;
  private static final int ONE_MINUTE = 60000;
  private static final int NORMAL_TERMINATION = 0;


  private static String muleHome;
  private static String muleVersion;
  private static Process applicationProcess;
  private static Runtime runtime = Runtime.getRuntime();

  private final String instanceName;


  private Logger log;
  private ID ids = new ID();

  private String username;
  private String password;

  private AuthenticationServiceClient authenticationServiceClient;


  public ArmEnvironment(String muleVersion, String instanceName) {
    log = LoggerFactory.getLogger(this.getClass());

    this.muleVersion = muleVersion;
    this.instanceName = instanceName;
    this.username = System.getProperty(USERNAME_ENVIRONMENT_VARIABLE);
    this.password = System.getProperty(PASSWORD_ENVIRONMENT_VARIABLE);
    this.authenticationServiceClient = new AuthenticationServiceClient(BASE_URL, true);
  }

  public void start() throws InterruptedException, TimeoutException, IOException {
    login();
    registerArmServer();
  }

  private void login() {
    authenticationServiceClient.getBearerToken(new Credentials(username, password));
    UserInfo me = authenticationServiceClient.getMe();

    ids.setOrganizationId(me.user.organization.id);

    for (Environment e : authenticationServiceClient.getEnvironments(me.user.organization.id)) {
      if (PRODUCTION_ENVIRONMENT.equals(e.name)) {
        ids.setEnvironmentId(e.id);
        break;
      }
    }
  }

  private void registerArmServer() throws IOException, InterruptedException, TimeoutException {
    ArmDeployment armDeployment = new ArmDeployment();
    armDeployment.setUsername(username);
    armDeployment.setPassword(password);

    armDeployment.setUri(BASE_URL);
    armDeployment.setEnvironment(PRODUCTION_ENVIRONMENT);
    armDeployment.setArmInsecure(false);

    ArmClient armClient = new ArmClient(armDeployment, null);
    armClient.init();
    String token = armClient.getRegistrationToken();

    // TODO this should be part of StandAloneUtils
    // ***********************************************************************************************
    Path currentRelativePath = Paths.get("");
    String targetFolder = currentRelativePath.toAbsolutePath().toString() + File.separator + "target";
    muleHome = targetFolder + MULE_HOME_FOLDER_PREFIX + muleVersion;

    deleteFile(muleHome + AGENT_JKS_RELATIVE_PATH);
    deleteFile(muleHome + AGENT_YMS_RELATIVE_PATH);
    // ***********************************************************************************************

    registerServer(token);

    startMule();

    Target server = armClient.findServerByName(instanceName);

    assertServerIsRunning(Integer.valueOf(server.id), 4 * ONE_MINUTE, armClient);
  }

  private void deleteFile(String pathname) {
    File file = new File(pathname);
    if (file.exists()) {
      file.delete();
    }
  }

  private void assertServerIsRunning(Integer serverId, int timeoutInSeconds, ArmClient armClient)
      throws TimeoutException, InterruptedException {
    int oneSecond = 1000;
    int count = 0;
    while (count != timeoutInSeconds) {
      Thread.sleep(oneSecond);
      count++;

      Servers server = armClient.getServer(serverId);
      if (StringUtils.equals(server.data[0].status, "RUNNING")) {
        return;
      }
    }
    throw new TimeoutException("Waiting for Standalone to accept deployments has timeout.");
  }

  // TODO all this should go to a command line utils
  // ************************************************************************************************************************
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

  private String getMuleExecutable() {
    return muleHome + EXECUTABLE_FOLDER_RELATIVE_PATH;
  }
  // ************************************************************************************************************************

}

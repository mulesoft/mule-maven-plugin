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

import org.apache.maven.it.VerificationException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.shared.utils.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.client.agent.AgentClient;
import org.mule.tools.client.standalone.controller.MuleProcessController;
import org.mule.tools.maven.mojo.deploy.logging.MavenDeployerLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class StandaloneEnvironment {

  public static final int ATTEMPTS = 30;
  private static final String STATUS_COMMAND = "status";
  private Logger log;
  private static final long TEN_SECONDS = 10000;
  private static final String MULE_HOME_FOLDER_PREFIX = "/mule-enterprise-standalone-";
  private static String muleVersion;
  private static final String AGENT_JKS_RELATIVE_PATH = "/conf/mule-agent.jks";
  private static final String AGENT_YMS_RELATIVE_PATH = "/conf/mule-agent.yml";
  private static final String EXECUTABLE_FOLDER_RELATIVE_PATH = "/bin/mule";
  private static final String AMC_SETUP_RELATIVE_FOLDER = "/bin/amc_setup";
  private static final String UNENCRYPTED_CONNECTION_OPTION = "-I";
  private static final int NORMAL_TERMINATION = 0;
  private static final String START_AGENT_COMMAND = "start";
  private static final String ANCHOR_FILE_RELATIVE_PATH = "/apps";
  private static final String STOP_AGENT_COMMAND = "stop";
  private static List<String> commands = new ArrayList<>();
  private static String muleHome;
  private static Runtime runtime = Runtime.getRuntime();
  private static Process applicationProcess;
  private static MuleProcessController controller;

  public StandaloneEnvironment(String muleVersion) {
    log = LoggerFactory.getLogger(this.getClass());
    this.muleVersion = muleVersion;
  }

  public void start() {
    Path currentRelativePath = Paths.get("");
    String targetFolder = currentRelativePath.toAbsolutePath().toString() + File.separator + "target";

    muleHome = targetFolder + MULE_HOME_FOLDER_PREFIX + muleVersion;
    controller = new MuleProcessController(muleHome);
    deleteFile(muleHome + AGENT_JKS_RELATIVE_PATH);
    deleteFile(muleHome + AGENT_YMS_RELATIVE_PATH);

  }

  public void runAgent() throws IOException, InterruptedException {
    unpackAgent();
    startMule();
    //checkAgentIsAcceptingDeployments();
  }

  public void checkStandaloneStatus(String status) throws InterruptedException, IOException {
    commands.clear();
    commands.add(getMuleExecutable());
    commands.add(STATUS_COMMAND);
    log.info("Checking mule status...");
    applicationProcess = runtime.exec(commands.toArray(new String[0]));
    applicationProcess.waitFor();
    BufferedReader output = new BufferedReader(new InputStreamReader(applicationProcess.getInputStream()));
    String line;
    Boolean containsStatus = false;
    while ((line = output.readLine()) != null) {
      if (line.contains(status)) {
        containsStatus = true;
        break;
      }
    }
    assertThat("Standalone status " + status + " was not found in process output", containsStatus, is(true));
  }

  public void stop() throws IOException, InterruptedException {
    stopMule();
    checkStandaloneStatus("Mule Enterprise Edition is not running.");
    killMuleProcesses();
  }

  private void checkAgentIsAcceptingDeployments() throws InterruptedException, IOException {
    int tries = 0;
    boolean acceptingDeployments;
    TemporaryFolder folder = new TemporaryFolder();
    folder.create();
    File dummyFile = folder.newFile("dummy.jar");
    AgentClient agentClient = new AgentClient(new MavenDeployerLog(new SystemStreamLog()), "http://localhost:9999/");
    do {
      try {
        log.info("Checking if agent is accepting deployments...");
        agentClient.deployApplication("dummy", dummyFile);
        acceptingDeployments = true;
      } catch (Exception e) {
        log.info("Agent is not accepting deployments yet. Trying again...");
        log.error("Cause: " + e.getLocalizedMessage());
        Thread.sleep(TEN_SECONDS);
        acceptingDeployments = false;
        tries++;
      }
      if (tries == ATTEMPTS) { // Trying for approximately 30 X 10 s = 300 s = 5 minutes
        fail("Could not have agent accepting deployments");
      }
    } while (!acceptingDeployments);
    log.info("Agent is accepting deployments.");
  }

  private void startMule() throws InterruptedException, IOException {
    int tries = 0;
    do {
      if (tries != 0) {
        log.info("Failed to start mule. Trying to start again...");
        stopMule();
      }
      commands.clear();
      commands.add(getMuleExecutable());
      commands.add(START_AGENT_COMMAND);
      log.info("Starting mule...");
      applicationProcess = runtime.exec(commands.toArray(new String[0]));
      applicationProcess.waitFor();
      tries++;
      if (tries == ATTEMPTS) {
        fail("Could not have mule running");
      }
    } while (applicationProcess.exitValue() != NORMAL_TERMINATION);
    log.info("Mule successfully started.");
  }

  private void unpackAgent() throws InterruptedException, IOException {
    String amcExecutable = muleHome + AMC_SETUP_RELATIVE_FOLDER;
    int tries = 0;
    do {
      if (tries != 0) {
        log.info("Failed to unpack agent. Trying to unpack again...");
      }
      commands.clear();
      commands.add(amcExecutable);
      commands.add(UNENCRYPTED_CONNECTION_OPTION);
      log.info("Unpacking agent...");
      applicationProcess = runtime.exec(commands.toArray(new String[0]));
      applicationProcess.waitFor();
      tries++;
      if (tries == ATTEMPTS) {
        fail("Could not unpack agent");
      }
    } while (applicationProcess.exitValue() != NORMAL_TERMINATION);
    log.info("Agent successfully unpacked.");
  }

  private void deleteFile(String pathname) {
    File file = new File(pathname);
    if (file.exists()) {
      file.delete();
    }
  }

  private void stopMule() throws InterruptedException, IOException {
    log.info("Stopping mule...");
    int tries = 0;
    do {
      if (tries != 0) {
        log.info("Failed to stop mule. Trying to stop again...");
      }
      commands.clear();
      commands.add(getMuleExecutable());
      commands.add(STOP_AGENT_COMMAND);
      applicationProcess = runtime.exec(commands.toArray(new String[0]));
      applicationProcess.waitFor();
      tries++;
      if (tries == ATTEMPTS) {
        fail("Could not stop mule");
      }
    } while (applicationProcess.exitValue() != NORMAL_TERMINATION);
    log.info("Mule successfully stopped.");
  }

  public String getAnchorFilePath(String anchorFileName) {
    return muleHome + ANCHOR_FILE_RELATIVE_PATH + File.separator + anchorFileName;
  }



  public void killMuleProcesses() throws IOException {
    commands.clear();
    commands.add("ps");
    commands.add("-ax");
    commands.add("|");
    commands.add("grep");
    commands.add("mule");
    commands.add("|");
    commands.add("grep");
    commands.add("wrapper");
    commands.add("|");
    commands.add("cut");
    commands.add("-c");
    commands.add("1-5");
    commands.add("|");
    commands.add("xargs");
    commands.add("kill");
    commands.add("-9");
    runtime.exec(commands.toArray(new String[0]));
  }

  public void verifyDeployment(boolean isDeployed, String anchorFileName)
      throws InterruptedException, IOException, VerificationException {
    log.info("Verifying deployment status...");
    boolean deployed = false;
    for (int i = 0; i < 10 && !deployed; ++i, Thread.sleep(1000)) {
      deployed = FileUtils.fileExists(getAnchorFilePath(anchorFileName));
    }
    assertThat("Failed to deploy, could not find anchor file", deployed, is(isDeployed));

    stop();
  }

  public void setMuleHome(String muleHome) {
    this.muleHome = muleHome;
  }

  public String getMuleExecutable() {
    return muleHome + EXECUTABLE_FOLDER_RELATIVE_PATH;
  }

  public void runStandalone() throws IOException, InterruptedException, TimeoutException {
    startMule();
    checkStandaloneIsAcceptingDeployments(30);
  }

  public void checkStandaloneIsAcceptingDeployments(int timeoutInSeconds) throws InterruptedException, TimeoutException {
    int oneSecond = 1000;
    int count = 0;
    while (count != timeoutInSeconds) {
      Thread.sleep(oneSecond);
      count++;
      if (controller.isRunning()) {
        return;
      }
    }
    throw new TimeoutException("Waiting for Standalone to accept deployments has timeout.");
  }
}

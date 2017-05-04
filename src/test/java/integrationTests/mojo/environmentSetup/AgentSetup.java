/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integrationTests.mojo.environmentSetup;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.client.agent.AgentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.fail;

public class AgentSetup {

  private Logger log;
  private static final long TEN_SECONDS = 10000;
  private static final String MULE_HOME_FOLDER_PREFIX = "/mule-enterprise-standalone-";
  private static final String MULE_VERSION = "3.8.1";
  private static final String AGENT_JKS_RELATIVE_PATH = "/conf/mule-agent.jks";
  private static final String AGENT_YMS_RELATIVE_PATH = "/conf/mule-agent.yml";
  private static final String EXECUTABLE_FOLDER_RELATIVE_PATH = "/bin/mule";
  private static final String AMC_SETUP_RELATIVE_FOLDER = "/bin/amc_setup";
  private static final String UNENCRYPTED_CONNECTION_OPTION = "-I";
  private static final int NORMAL_TERMINATION = 0;
  private static final String START_AGENT_COMMAND = "start";
  private static final String ANCHOR_FILE_RELATIVE_PATH = "/apps/agent-anchor.txt";
  private static final String STOP_AGENT_COMMAND = "stop";
  private static String muleExecutable;
  private static String[] commands;
  private static String muleHome;
  private static Runtime runtime;
  private static Process applicationProcess;

  public AgentSetup() {
    log = LoggerFactory.getLogger(this.getClass());
  }


  public void start() throws IOException, InterruptedException {
    Path currentRelativePath = Paths.get("");
    String targetFolder = currentRelativePath.toAbsolutePath().toString() + File.separator + "target";

    muleHome = targetFolder + MULE_HOME_FOLDER_PREFIX + MULE_VERSION;

    deleteFile(muleHome + AGENT_JKS_RELATIVE_PATH);
    deleteFile(muleHome + AGENT_YMS_RELATIVE_PATH);

    muleExecutable = muleHome + EXECUTABLE_FOLDER_RELATIVE_PATH;

    runtime = Runtime.getRuntime();
    commands = new String[2];

    unpackAgent();
    startMule();
    checkAgentIsAcceptingDeployments();
  }

  public void stop() throws IOException, InterruptedException {
    stopMule();
    killMuleProcesses();
  }

  private void checkAgentIsAcceptingDeployments() throws InterruptedException, IOException {
    int tries = 0;
    boolean acceptingDeployments;
    do {
      TemporaryFolder folder = new TemporaryFolder();
      File dummyFile = folder.newFile("dummy.zip");
      AgentClient agentClient = new AgentClient(new SystemStreamLog(), "http://localhost:9999/");
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
      if (tries == 30) { // Trying for approximately 30 X 10 s = 300 s = 5 minutes
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
      commands[0] = muleExecutable;
      commands[1] = START_AGENT_COMMAND;
      log.info("Starting mule...");
      applicationProcess = runtime.exec(commands);
      applicationProcess.waitFor();
      tries++;
      if (tries == 30) {
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
      commands[0] = amcExecutable;
      commands[1] = UNENCRYPTED_CONNECTION_OPTION;
      log.info("Unpacking agent...");
      applicationProcess = runtime.exec(commands);
      applicationProcess.waitFor();
      tries++;
      if (tries == 30) {
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
      commands[0] = muleExecutable;
      commands[1] = STOP_AGENT_COMMAND;
      applicationProcess = runtime.exec(commands);
      applicationProcess.waitFor();
      tries++;
      if (tries == 30) {
        fail("Could not stop mule");
      }
    } while (applicationProcess.exitValue() != NORMAL_TERMINATION);
    log.info("Mule successfully stopped.");
  }

  public String getAnchorFilePath() {
    return muleHome + ANCHOR_FILE_RELATIVE_PATH;
  }



  private void killMuleProcesses() throws IOException {
    commands = new String[16];
    commands[0] = "ps";
    commands[1] = "-ax";
    commands[2] = "|";
    commands[3] = "grep";
    commands[4] = "mule";
    commands[5] = "|";
    commands[6] = "grep";
    commands[7] = "wrapper";
    commands[8] = "|";
    commands[9] = "cut";
    commands[10] = "-c";
    commands[11] = "1-5";
    commands[12] = "|";
    commands[13] = "xargs";
    commands[14] = "kill";
    commands[15] = "-9";
    runtime.exec(commands);
  }
}

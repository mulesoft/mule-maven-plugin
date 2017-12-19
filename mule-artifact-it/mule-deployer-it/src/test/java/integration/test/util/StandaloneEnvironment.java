/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.util;

import static org.junit.Assert.fail;
import static org.mule.tools.api.util.FileUtils.copyDirectoryRecursively;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.mule.tools.client.standalone.controller.MuleProcessController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandaloneEnvironment {

  private static final int NORMAL_TERMINATION = 0;

  public static final int UNPACK_AGENT_MAX_ATTEMPTS = 30;
  public static final int REGISTER_SERVER_MAX_ATTEMPTS = 30;
  public static final int CHECK_RUNTIME_STARTED_MAX_ATTEMPTS = 30;

  private static final String ARM_CONFIGURATION_OPTION = "-H";
  private static final String UNENCRYPTED_CONNECTION_OPTION = "-I";

  private static final String AMC_SETUP_RELATIVE_FOLDER = "/bin/amc_setup";
  private static final String AGENT_JKS_RELATIVE_PATH = "/conf/mule-agent.jks";
  private static final String AGENT_YMS_RELATIVE_PATH = "/conf/mule-agent.yml";

  private static final String MULE_HOME_FOLDER_PREFIX = "mule-enterprise-standalone-";

  protected Logger log;

  private Path muleHome;
  private static MuleProcessController controller;


  // TODO Be careful with this if we copy we'll copy for each test
  public StandaloneEnvironment(File workingDir, String muleVersion) throws IOException {
    String muleDistributionFolderName = MULE_HOME_FOLDER_PREFIX + muleVersion;

    this.muleHome = workingDir.toPath().resolve(muleDistributionFolderName);
    this.muleHome.toFile().mkdirs();

    createMuleDistributionWorkingDirectory(muleHome, muleDistributionFolderName);
    intiStandAloneEnvironment();

    this.log = LoggerFactory.getLogger(this.getClass());

    this.controller = new MuleProcessController(muleHome.toFile().getAbsolutePath());
  }

  public String getMuleHome() {
    return muleHome.toFile().getAbsolutePath();
  }

  public void start(Boolean agent) throws IOException, InterruptedException, TimeoutException {
    if (agent) {
      unpackAgent();
    }
    controller.start();
    checkIfStarted(CHECK_RUNTIME_STARTED_MAX_ATTEMPTS);
  }

  public void stop() throws IOException, InterruptedException {
    controller.stop();
    // killMuleProcesses();
  }

  public Boolean isRunning() {
    return controller.isRunning();
  }

  public Boolean isDeployed(String applicationName) {
    return controller.isDeployed(applicationName);
  }

  public Boolean isDomainDeployed(String domainName) {
    return controller.isDomainDeployed(domainName);
  }

  private void killMuleProcesses() throws IOException {
    List<String> commands = new ArrayList<>();

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
    Runtime.getRuntime().exec(commands.toArray(new String[0]));

    Runtime.getRuntime().exec("pkill -f \"mule\"");
  }

  public void register(String token, String instanceName) throws IOException, InterruptedException {
    String amcExecutable = getMuleHome() + AMC_SETUP_RELATIVE_FOLDER;
    String[] commands = {amcExecutable, ARM_CONFIGURATION_OPTION, token, instanceName};

    executeAction(() -> {
    }, "register server on", REGISTER_SERVER_MAX_ATTEMPTS, commands);
  }

  private void unpackAgent() throws InterruptedException, IOException {
    List<String> commands = new ArrayList<>();

    String amcExecutable = muleHome + AMC_SETUP_RELATIVE_FOLDER;

    int tries = 0;
    Process applicationProcess;
    do {
      if (tries != 0) {
        log.info("Failed to unpack agent. Trying to unpack again...");
      }
      commands.clear();
      commands.add(amcExecutable);
      commands.add(UNENCRYPTED_CONNECTION_OPTION);
      log.info("Unpacking agent...");
      applicationProcess = Runtime.getRuntime().exec(commands.toArray(new String[0]));
      applicationProcess.waitFor();
      tries++;
      if (tries == UNPACK_AGENT_MAX_ATTEMPTS) {
        fail("Could not unpack agent");
      }
    } while (applicationProcess.exitValue() != NORMAL_TERMINATION);
    log.info("Agent successfully unpacked.");
  }

  private void checkIfStarted(int attempts) throws InterruptedException, TimeoutException {
    int count = 0;
    while (count <= attempts) {
      if (controller.isRunning()) {
        return;
      }
      Thread.sleep(1000);
      count++;
    }
    throw new TimeoutException("Waiting for Standalone to accept deployments has timeout.");
  }

  private void intiStandAloneEnvironment() {
    deleteFile(muleHome + AGENT_JKS_RELATIVE_PATH);
    deleteFile(muleHome + AGENT_YMS_RELATIVE_PATH);
  }

  private void deleteFile(String pathname) {
    File file = new File(pathname);
    if (file.exists()) {
      file.delete();
    }
  }

  private void createMuleDistributionWorkingDirectory(Path workingDirPath, String muleDistributionFolderName) throws IOException {
    Path targetPath = Paths.get("").resolve("target");
    Path originalMuleHomeFolderPath = targetPath.resolve(muleDistributionFolderName);
    copyDirectoryRecursively(originalMuleHomeFolderPath.toFile(), workingDirPath.toFile());
  }


  private void executeAction(Runnable onRestart, String action, Integer attempts, String[] commands)
      throws IOException, InterruptedException {
    log.info("Trying to " + action + " mule...");

    int tries = 0;
    Process applicationProcess;
    do {
      if (tries != 0) {
        log.info("Failed to " + action + " mule. Trying to " + action + " again...");
        onRestart.run();
      }
      applicationProcess = Runtime.getRuntime().exec(commands);
      applicationProcess.waitFor();
      tries++;
      if (tries == attempts) {
        fail("Could not " + action + " mule");
      }
    } while (applicationProcess.exitValue() != NORMAL_TERMINATION);
    log.info("Mule successfully stopped.");
  }
}

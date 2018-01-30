/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.util.environment;

import static integration.test.util.FileUtils.copyDirectoryRecursively;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.client.standalone.controller.MuleProcessController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandaloneEnvironment extends ExternalResource {

  public static final int UNPACK_AGENT_MAX_ATTEMPTS = 30;
  public static final int REGISTER_SERVER_MAX_ATTEMPTS = 30;
  public static final int CHECK_RUNTIME_STARTED_MAX_ATTEMPTS = 30;

  protected static final int NORMAL_TERMINATION = 0;
  protected static final String AMC_SETUP_RELATIVE_FOLDER = "/bin/amc_setup";
  protected Logger log;
  protected TemporaryFolder muleHome = new TemporaryFolder();

  private static final String AGENT_JKS_RELATIVE_PATH = "/conf/mule-agent.jks";
  private static final String AGENT_YMS_RELATIVE_PATH = "/conf/mule-agent.yml";
  private static final String MULE_HOME_FOLDER_PREFIX = "mule-enterprise-standalone-";
  private static MuleProcessController controller;


  @Override
  protected void before() throws Throwable {
    this.start();
  }

  @Override
  protected void after() {
    try {
      this.stop();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    } finally {
      muleHome.delete();
    }
  }

  // TODO Be careful with this if we copy we'll copy for each test
  public StandaloneEnvironment(String muleVersion) {
    String muleDistributionFolderName = MULE_HOME_FOLDER_PREFIX + muleVersion;

    try {
      muleHome.create();
      muleHome.newFolder(muleDistributionFolderName).mkdir();
      createMuleDistributionWorkingDirectory(muleHome.getRoot().toPath(), muleDistributionFolderName);
    } catch (IOException e) {
      e.printStackTrace();
    }

    intiStandAloneEnvironment();

    this.log = LoggerFactory.getLogger(this.getClass());

    controller = new MuleProcessController(getMuleHome());
  }

  public String getMuleHome() {
    return muleHome.getRoot().getAbsolutePath();
  }

  protected void start() throws IOException, InterruptedException, TimeoutException {
    controller.start();
    checkIfStarted(CHECK_RUNTIME_STARTED_MAX_ATTEMPTS);
  }

  private void stop() throws IOException, InterruptedException {
    controller.stop();
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

}

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integrationTests.mojo;

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.util.ResourceExtractor;
import org.apache.maven.shared.utils.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AgentDeploymentTest extends MojoTest {

  private static final String MULE_DEPLOY = "mule:deploy";
  private static final String MULE_UNDEPLOY = "mule:undeploy";
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
  private static File agentJks;
  private static File agentYml;
  private static Runtime runtime;
  private static Process applicationProcess;

  public AgentDeploymentTest() {
    this.goal = MULE_DEPLOY;
  }

  @Before
  public void before() throws VerificationException, InterruptedException, IOException {
    verifier.executeGoal(INSTALL);

    String targetFolder = ResourceExtractor.simpleExtractResources(this.getClass(), "/").getParent();

    muleHome = targetFolder + MULE_HOME_FOLDER_PREFIX + MULE_VERSION;

    agentJks = new File(muleHome + AGENT_JKS_RELATIVE_PATH);
    if (agentJks.exists()) {
      agentJks.delete();
    }

    agentYml = new File(muleHome + AGENT_YMS_RELATIVE_PATH);
    if (agentYml.exists()) {
      agentYml.delete();
    }

    muleExecutable = muleHome + EXECUTABLE_FOLDER_RELATIVE_PATH;
    String amcExecutable = muleHome + AMC_SETUP_RELATIVE_FOLDER;

    runtime = Runtime.getRuntime();
    commands = new String[2];
    commands[0] = amcExecutable;
    commands[1] = UNENCRYPTED_CONNECTION_OPTION;
    applicationProcess = runtime.exec(commands);
    applicationProcess.waitFor();
    assertThat("Failed to unpack agent", applicationProcess.exitValue(), equalTo(NORMAL_TERMINATION));

    commands[0] = muleExecutable;
    commands[1] = START_AGENT_COMMAND;
    applicationProcess = runtime.exec(commands);
    applicationProcess.waitFor();
    assertThat("Failed to start mule", applicationProcess.exitValue(), equalTo(NORMAL_TERMINATION));
  }

  @Test
  public void testAgentDeploy() throws IOException, VerificationException, InterruptedException {
    verifier.executeGoal(MULE_DEPLOY);
    verifyDeployment(true);
  }

  @Test
  public void testAgentDeployUndeploy() throws IOException, VerificationException, InterruptedException {
    verifier.executeGoal(MULE_DEPLOY);

    verifier.executeGoal(MULE_UNDEPLOY);

    verifyDeployment(false);
  }

  private void verifyDeployment(boolean isDeployed) throws InterruptedException, IOException, VerificationException {
    boolean deployed = false;
    for (int i = 0; i < 10 && !deployed; ++i, Thread.sleep(1000)) {
      deployed = FileUtils.fileExists(muleHome + ANCHOR_FILE_RELATIVE_PATH);
    }
    commands[0] = muleExecutable;
    commands[1] = STOP_AGENT_COMMAND;
    applicationProcess = runtime.exec(commands);
    applicationProcess.waitFor();

    assertThat("Failed to deploy", deployed, is(false));

    verifier.verifyErrorFreeLog();
  }
}

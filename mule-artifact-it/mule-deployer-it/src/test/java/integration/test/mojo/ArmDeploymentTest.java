/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package integration.test.mojo;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.mule.tools.client.AbstractMuleClient.DEFAULT_BASE_URL;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.client.arm.model.Servers;
import org.mule.tools.client.arm.model.Target;
import org.mule.tools.model.anypoint.ArmDeployment;

import org.slf4j.LoggerFactory;

import integration.test.mojo.environment.verifier.ArmDeploymentVerifier;
import integration.test.util.StandaloneEnvironment;

public class ArmDeploymentTest extends AbstractDeploymentTest {

  private static final int APPLICATION_NAME_LENGTH = 10;
  private static final String APPLICATION = "empty-mule-deploy-arm-project";
  private static final String INSTANCE_NAME = APPLICATION + randomAlphabetic(APPLICATION_NAME_LENGTH).toLowerCase();

  @Rule
  public TemporaryFolder environmentWorkingDir = new TemporaryFolder();

  private Verifier verifier;
  private ArmClient armClient;

  public String getApplication() {
    return APPLICATION;
  }

  @Before
  public void before() throws VerificationException, InterruptedException, IOException, TimeoutException {
    log = LoggerFactory.getLogger(this.getClass());
    log.info("Initializing context...");

    armClient = getArmClient();
    standaloneEnvironment = new StandaloneEnvironment(environmentWorkingDir.getRoot(), getMuleVersion());

    standaloneEnvironment.register(armClient.getRegistrationToken(), INSTANCE_NAME);
    standaloneEnvironment.start(false);
    assertServerIsRunningInArm(INSTANCE_NAME, 4 * 60000, armClient);

    verifier = buildBaseVerifier();
    verifier.setEnvironmentVariable("username", username);
    verifier.setEnvironmentVariable("password", password);
    verifier.setEnvironmentVariable("target", INSTANCE_NAME);
    verifier.setEnvironmentVariable("target.type", "server");
    verifier.setEnvironmentVariable("environment", PRODUCTION_ENVIRONMENT);
    verifier.setEnvironmentVariable("arm.application.name", APPLICATION);
  }

  @Test
  public void testArmDeploy() throws VerificationException, InterruptedException, TimeoutException {
    log.info("Executing mule:deploy goal...");
    verifier.addCliOption("-DmuleDeploy");
    verifier.executeGoal(DEPLOY_GOAL);

    ArmDeploymentVerifier armDeploymentVerifier = new ArmDeploymentVerifier();
    armDeploymentVerifier.verifyIsDeployed(INSTANCE_NAME);
    verifier.verifyErrorFreeLog();
  }

  @After
  public void after() throws IOException, InterruptedException {
    standaloneEnvironment.stop();
    verifier.resetStreams();
    environmentWorkingDir.delete();
  }

  private ArmClient getArmClient() {
    ArmDeployment armDeployment = new ArmDeployment();
    armDeployment.setUsername(username);
    armDeployment.setPassword(password);

    armDeployment.setUri(DEFAULT_BASE_URL);
    armDeployment.setEnvironment(PRODUCTION_ENVIRONMENT);
    armDeployment.setArmInsecure(false);

    ArmClient armClient = new ArmClient(armDeployment, null);
    armClient.init();
    return armClient;
  }

  // TODO this should probably go to the arm deployment verifier
  private void assertServerIsRunningInArm(String instanceName, int timeoutInSeconds, ArmClient armClient)
      throws TimeoutException, InterruptedException {

    Target target = armClient.findServerByName(instanceName);
    Integer serverId = Integer.valueOf(target.id);

    int count = 0;
    int oneSecond = 1000;
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
}

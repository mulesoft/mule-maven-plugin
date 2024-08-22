/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mule.tools.client.AbstractMuleClient.DEFAULT_BASE_URL;

import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;

import org.apache.maven.shared.verifier.Verifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.client.arm.model.Applications;
import org.mule.tools.client.arm.model.Data;
import org.mule.tools.client.arm.model.Servers;
import org.mule.tools.client.arm.model.Target;
import org.mule.tools.model.anypoint.ArmDeployment;

import integration.test.util.StandaloneEnvironment;

@DisabledOnOs(OS.WINDOWS)
@DisabledOnJre(JRE.JAVA_8)
public class ArmDeploymentTest extends AbstractDeploymentTest {

  private static final int APPLICATION_NAME_LENGTH = 10;

  private static final String APPLICATION_ARTIFACT_ID = "arm-deploy";
  private static final String APPLICATION = "empty-mule-deploy-arm-project";
  private static final String ARM_INSTANCE_NAME = APPLICATION + randomAlphabetic(APPLICATION_NAME_LENGTH).toLowerCase();

  private static final int ATTEMPTS = 30;
  private static final long SLEEP_TIME = 30000;
  private static final String STARTED_STATUS = "STARTED";
  public static final String RUNNING_STATUS = "RUNNING";
  private static StandaloneEnvironment standaloneEnvironment;

  private Verifier verifier;
  private ArmClient armClient;

  public String getApplication() {
    return APPLICATION;
  }

  @TempDir
  public Path environmentWorkingDir;

  @BeforeEach
  public void before() throws Exception {
    LOG.info("Initializing context...");

    armClient = getArmClient();
    for (Target t : armClient.getServers()) {
      if (t.name.startsWith(APPLICATION)) {
        armClient.deleteServer(Integer.valueOf(t.id));
      }
    }
    standaloneEnvironment = new StandaloneEnvironment(environmentWorkingDir.toFile(), getMuleVersion());

    standaloneEnvironment.register(armClient.getRegistrationToken(), ARM_INSTANCE_NAME);
    standaloneEnvironment.start(false);
    assertServerIsRunningInArm(ARM_INSTANCE_NAME, 4 * 60000, armClient);

    verifier = buildBaseVerifier();
    verifier.setSystemProperty("username", getUsername());
    verifier.setSystemProperty("password", getPassword());
    verifier.setSystemProperty("target", ARM_INSTANCE_NAME);
    verifier.setSystemProperty("target.type", "server");
    verifier.setSystemProperty("mule.version", getMuleVersion());
    verifier.setSystemProperty("environment", PRODUCTION_ENVIRONMENT);
  }

  @Test
  public void testArmDeploy() throws Exception {
    LOG.info("Executing mule:deploy goal...");
    verifier.addCliArguments(DEPLOY_GOAL, "-DmuleDeploy");
    verifier.execute();


    boolean status = getApplicationStatus(APPLICATION_ARTIFACT_ID, ARM_INSTANCE_NAME, STARTED_STATUS);
    assertThat(status).describedAs("Application was not deployed").isTrue();
    verifier.verifyErrorFreeLog();
  }

  @AfterEach
  public void after() throws InterruptedException, TimeoutException {
    standaloneEnvironment.stop();
    environmentWorkingDir.toFile().delete();

    Data application = getApplication(APPLICATION_ARTIFACT_ID, ARM_INSTANCE_NAME);
    armClient.deleteServer(Integer.valueOf(application.target.id));
  }

  private ArmClient getArmClient() {
    ArmDeployment armDeployment = new ArmDeployment();
    armDeployment.setUsername(getUsername());
    armDeployment.setPassword(getPassword());

    armDeployment.setUri(DEFAULT_BASE_URL);
    armDeployment.setEnvironment(PRODUCTION_ENVIRONMENT);
    armDeployment.setArmInsecure(false);

    return new ArmClient(armDeployment, null);
  }

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
      if (StringUtils.equals(server.data[0].status, RUNNING_STATUS)) {
        return;
      }
    }
    throw new TimeoutException("Waiting for Standalone to accept deployments has timeout.");
  }

  public boolean getApplicationStatus(String applicationName, String armInstanceName, String status)
      throws InterruptedException, TimeoutException {
    boolean keepValidating = true;

    int i = ATTEMPTS;
    while (i > 0 && keepValidating) {
      Data application = getApplication(applicationName, armInstanceName);
      keepValidating = !application.desiredStatus.equals(status);

      if (keepValidating) {
        Thread.sleep(SLEEP_TIME);
      }
      i--;
    }

    if (i == 0 && keepValidating) {
      throw new TimeoutException("Validating status " + status + " for application " + applicationName
          + " has exceed the maximum number of attempts.");
    }
    return !keepValidating;
  }

  public Data getApplication(String applicationName, String armInstanceName) {
    Applications applications = armClient.getApplications();
    for (Data d : applications.data) {
      if (d.artifact.name.equals(applicationName) && d.target.name.equals(armInstanceName)) {
        return d;
      }
    }

    return null;
  }
}

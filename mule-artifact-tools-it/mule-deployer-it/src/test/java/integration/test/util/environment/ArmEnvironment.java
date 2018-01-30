/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.util.environment;

import org.apache.commons.lang3.StringUtils;
import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.client.arm.model.Applications;
import org.mule.tools.client.arm.model.Data;
import org.mule.tools.client.arm.model.Servers;
import org.mule.tools.client.arm.model.Target;
import org.mule.tools.model.anypoint.ArmDeployment;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.fail;
import static org.mule.tools.client.AbstractMuleClient.DEFAULT_BASE_URL;

public class ArmEnvironment extends StandaloneEnvironment {

  private static final int APPLICATION_NAME_LENGTH = 10;
  private static final String STARTED_STATUS = "STARTED";
  private static final String ARM_CONFIGURATION_OPTION = "-H";
  private static final String RUNNING_STATUS = "RUNNING";
  private static final int ATTEMPTS = 30;
  private static final long SLEEP_TIME = 30000;
  private final ArmClient client;
  private final String armInstanceName;
  private final String applicationArtifactId;


  private Runnable noopRunnable = new Runnable() {

    @Override
    public void run() {

    }
  };

  public ArmEnvironment(String muleVersion, ArmClient armClient, String application, String applicationArtifactId) {
    super(muleVersion);
    client = armClient;
    armInstanceName = application + randomAlphabetic(APPLICATION_NAME_LENGTH).toLowerCase();
    this.applicationArtifactId = applicationArtifactId;
  }

  @Override
  public void start() throws IOException, InterruptedException, TimeoutException {
    super.start();
    register(client.getRegistrationToken(), armInstanceName);
    assertServerIsRunningInArm(4 * 60000);
  }

  @Override
  public void after() {
    Data application = getApplication(applicationArtifactId, getInstanceName());
    client.deleteServer(Integer.valueOf(application.target.id));
    super.after();
  }

  public void register(String token, String instanceName) throws IOException, InterruptedException {
    String amcExecutable = getMuleHome() + AMC_SETUP_RELATIVE_FOLDER;
    String[] commands = {amcExecutable, ARM_CONFIGURATION_OPTION, token, instanceName};
    executeAction(noopRunnable, "register server on", REGISTER_SERVER_MAX_ATTEMPTS, commands);
  }

  protected void executeAction(Runnable onRestart, String action, Integer attempts, String[] commands)
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

  public String getInstanceName() {
    return armInstanceName;
  }

  private void assertServerIsRunningInArm(int timeoutInSeconds)
      throws TimeoutException, InterruptedException {

    Target target = client.findServerByName(getInstanceName());
    Integer serverId = Integer.valueOf(target.id);

    int count = 0;
    int oneSecond = 1000;
    while (count != timeoutInSeconds) {
      Thread.sleep(oneSecond);
      count++;

      Servers server = client.getServer(serverId);
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
    Applications applications = client.getApplications();
    for (Data d : applications.data) {
      if (d.artifact.name.equals(applicationName) && d.target.name.equals(armInstanceName)) {
        return d;
      }
    }

    return null;
  }


  public boolean getApplicationStatus() throws TimeoutException, InterruptedException {
    return getApplicationStatus(applicationArtifactId, getInstanceName(), STARTED_STATUS);
  }
}

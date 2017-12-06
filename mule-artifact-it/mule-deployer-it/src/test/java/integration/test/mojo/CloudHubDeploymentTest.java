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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mule.tools.client.AbstractMuleClient.DEFAULT_BASE_URL;
import static org.mule.tools.client.cloudhub.CloudhubClient.STARTED_STATUS;
import static org.mule.tools.client.cloudhub.CloudhubClient.UNDEPLOYED_STATUS;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mule.tools.client.cloudhub.Application;
import org.mule.tools.client.cloudhub.CloudhubClient;
import org.mule.tools.model.anypoint.CloudHubDeployment;

public class CloudHubDeploymentTest extends AbstractDeploymentTest {

  private static final int APPLICATION_NAME_LENGTH = 10;
  private static final String APPLICATION = "empty-mule-deploy-cloudhub-project";
  private static final String APPLICATION_NAME = randomAlphabetic(APPLICATION_NAME_LENGTH).toLowerCase();

  private static final int ATTEMPTS = 10;
  private static final long SLEEP_TIME = 30000;


  private Verifier verifier;
  private CloudhubClient cloudhubClient;


  public String getApplication() {
    return APPLICATION;
  }

  @Before
  public void before() throws VerificationException, InterruptedException, IOException {
    log.info("Initializing context...");

    verifier = buildBaseVerifier();
    verifier.setEnvironmentVariable("username", username);
    verifier.setEnvironmentVariable("password", password);
    verifier.setEnvironmentVariable("environment", PRODUCTION_ENVIRONMENT);
    verifier.setEnvironmentVariable("mule.version", getMuleVersion());
    verifier.setEnvironmentVariable("cloudhub.application.name", APPLICATION_NAME);

    cloudhubClient = getCloudHubClient();
  }

  @Test
  public void testCloudHubDeploy() throws VerificationException, InterruptedException, TimeoutException {
    log.info("Executing mule:deploy goal...");
    verifier.addCliOption("-DmuleDeploy");
    verifier.executeGoal(DEPLOY_GOAL);

    // TODO check why we have this sleep here
    Thread.sleep(30000);

    boolean status = getApplicationStatus(APPLICATION_NAME, STARTED_STATUS);
    assertThat("Application was not deployed", status, is(true));

    verifier.verifyErrorFreeLog();
  }

  @After
  public void tearDown() {
    cloudhubClient.deleteApplication(APPLICATION_NAME);
  }

  private CloudhubClient getCloudHubClient() {
    CloudHubDeployment cloudHubDeployment = new CloudHubDeployment();
    cloudHubDeployment.setUsername(username);
    cloudHubDeployment.setPassword(password);

    cloudHubDeployment.setUri(DEFAULT_BASE_URL);
    cloudHubDeployment.setEnvironment(PRODUCTION_ENVIRONMENT);
    cloudHubDeployment.setBusinessGroup("");

    CloudhubClient cloudhubClient = new CloudhubClient(cloudHubDeployment, null);
    cloudhubClient.init();
    return cloudhubClient;
  }

  public boolean getApplicationStatus(String applicationName, String status)
      throws InterruptedException, TimeoutException {
    log.info("Checking application " + applicationName + " for status " + status + "...");

    int repeat = ATTEMPTS;
    boolean keepValidating = false;
    while (repeat > 0 && !keepValidating) {
      Application application = cloudhubClient.getApplication(applicationName);

      keepValidating = !isExpectedStatus(status, application);
      if (keepValidating) {
        Thread.sleep(SLEEP_TIME);
      }
      repeat--;
    }
    if (repeat == 0 && !keepValidating) {
      throw new TimeoutException("Validating status " + status + " for application " + applicationName
          + " has exceed the maximum number of attempts.");
    }
    return keepValidating;
  }

  private boolean isExpectedStatus(String status, Application application) {
    if (STARTED_STATUS.equals(status) && application != null) {
      return status.equals(application.status);
    }
    if (UNDEPLOYED_STATUS.equals(status)) {
      return application == null || UNDEPLOYED_STATUS.equals(application.status);
    }
    return false;
  }
}

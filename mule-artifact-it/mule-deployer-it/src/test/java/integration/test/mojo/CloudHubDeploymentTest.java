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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Test;

import integration.test.mojo.environment.verifier.CloudHubDeploymentVerifier;

public class CloudHubDeploymentTest extends AbstractDeploymentTest {

  private static final int APPLICATION_NAME_LENGTH = 10;
  private static final String APPLICATION = "empty-mule-deploy-cloudhub-project";
  private static final String APPLICATION_NAME = randomAlphabetic(APPLICATION_NAME_LENGTH).toLowerCase();

  private Verifier verifier;
  private CloudHubDeploymentVerifier cloudHubDeploymentVerifier;

  public String getApplication() {
    return APPLICATION;
  }

  @Before
  public void before() throws VerificationException, InterruptedException, IOException {
    log.info("Initializing context...");

    verifier = buildBaseVerifier();
    verifier.setEnvironmentVariable("username", username);
    verifier.setEnvironmentVariable("password", password);
    verifier.setEnvironmentVariable("environment", "Production");
    verifier.setEnvironmentVariable("mule.version", "4.0.0-FD"); // MMP-252
    verifier.setEnvironmentVariable("cloudhub.application.name", APPLICATION_NAME);

    // TODO find a way to fix this
    cloudHubDeploymentVerifier = new CloudHubDeploymentVerifier();
  }

  @Test
  public void testCloudHubDeploy() throws VerificationException, InterruptedException, TimeoutException {
    log.info("Executing mule:deploy goal...");
    verifier.addCliOption("-DmuleDeploy");
    verifier.executeGoal(DEPLOY_GOAL);

    // TODO check why we have this sleep here
    Thread.sleep(30000);

    cloudHubDeploymentVerifier.verifyIsDeployed(APPLICATION_NAME);
    verifier.verifyErrorFreeLog();
  }
}

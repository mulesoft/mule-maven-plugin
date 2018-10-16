/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mule.tools.client.AbstractMuleClient.DEFAULT_BASE_URL;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import integration.test.util.rules.environment.ArmEnvironment;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.*;

import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.model.anypoint.ArmDeployment;

import org.slf4j.LoggerFactory;

public class ArmDeploymentTest extends AbstractDeploymentTest {


  private static final String APPLICATION_ARTIFACT_ID = "arm-deploy";
  private static final String APPLICATION = "empty-mule-deploy-arm-project";

  @Rule
  public ArmEnvironment armEnvironment =
      new ArmEnvironment(getMuleVersion(), getArmClient(), APPLICATION, APPLICATION_ARTIFACT_ID);


  private Verifier verifier;

  public String getApplication() {
    return APPLICATION;
  }

  @Before
  public void before() throws VerificationException, IOException {
    log = LoggerFactory.getLogger(this.getClass());
    log.info("Initializing context...");

    verifier = buildBaseVerifier();
    verifier.setEnvironmentVariable("username", username);
    verifier.setEnvironmentVariable("password", password);
    verifier.setEnvironmentVariable("target", armEnvironment.getInstanceName());
    verifier.setEnvironmentVariable("target.type", "server");
    verifier.setEnvironmentVariable("mule.version", getMuleVersion());
    verifier.setEnvironmentVariable("environment", PRODUCTION_ENVIRONMENT);
  }

  @Test
  public void testArmDeploy() throws VerificationException, InterruptedException, TimeoutException {
    log.info("Deploying application...");
    verifier.addCliOption("-DmuleDeploy");
    verifier.executeGoal(DEPLOY_GOAL);

    assertThat("Application was not deployed", armEnvironment.getApplicationStatus(), is(true));
    verifier.verifyErrorFreeLog();
  }

  @After
  public void after() {
    verifier.resetStreams();
  }

  private ArmClient getArmClient() {
    ArmDeployment armDeployment = new ArmDeployment();
    armDeployment.setUsername(username);
    armDeployment.setPassword(password);

    armDeployment.setUri(DEFAULT_BASE_URL);
    armDeployment.setEnvironment(PRODUCTION_ENVIRONMENT);
    armDeployment.setArmInsecure(false);

    return new ArmClient(armDeployment, null);
  }


}

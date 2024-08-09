/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo.agent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

import integration.test.mojo.AbstractDeploymentTest;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;

import integration.test.util.StandaloneEnvironment;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public abstract class AgentDeploymentTest extends AbstractDeploymentTest {

  @TempDir
  public Path environmentWorkingDir;

  protected StandaloneEnvironment standaloneEnvironment;
  protected Verifier verifier;
  private String application;

  public AgentDeploymentTest(String application) {
    this.application = application;
  }

  public String getApplication() {
    return application;
  }

  @BeforeEach
  public void before() throws VerificationException, InterruptedException, IOException, TimeoutException {
    log.info("Initializing context...");

    standaloneEnvironment = new StandaloneEnvironment(environmentWorkingDir.toFile(), getMuleVersion());
    standaloneEnvironment.start(true);

    verifier = buildBaseVerifier();
  }

  @AfterEach
  public void after() throws InterruptedException, TimeoutException {
    standaloneEnvironment.stop();
    verifier.resetStreams();
    environmentWorkingDir.toFile().delete();
  }

  protected void deploy() throws VerificationException, InterruptedException {
    log.info("Executing mule:deploy goal...");

    // TODO check why we have this sleep here
    Thread.sleep(30000);

    verifier.setEnvironmentVariable("mule.version", getMuleVersion());
    verifier.setSystemProperty("applicationName", getApplication());
    verifier.addCliOption("-DmuleDeploy");
    verifier.executeGoal(DEPLOY_GOAL);
  }

  protected void assertAndVerify() throws VerificationException {
    assertThat("Standalone should be running ", standaloneEnvironment.isRunning(), is(true));
    assertDeployment();
    verifier.verifyErrorFreeLog();
  }

  public abstract void assertDeployment();

  @Test
  public void testAgentDeploy() throws IOException, VerificationException, InterruptedException {
    deploy();
    assertAndVerify();
  }
}

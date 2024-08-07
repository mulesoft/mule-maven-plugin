/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo.agent;

import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

import integration.test.mojo.AbstractDeploymentTest;

import integration.test.util.StandaloneEnvironment;
import org.apache.maven.shared.verifier.Verifier;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class AgentDeploymentTest extends AbstractDeploymentTest {

  @TempDir
  public Path environmentWorkingDir;

  protected StandaloneEnvironment standaloneEnvironment;
  protected Verifier verifier;
  private final String application;

  public AgentDeploymentTest(String application) {
    this.application = application;
  }

  public String getApplication() {
    return application;
  }

  @BeforeEach
  public void before() throws Exception {
    LOG.info("Initializing context...");

    standaloneEnvironment = new StandaloneEnvironment(environmentWorkingDir.toFile(), getMuleVersion());
    standaloneEnvironment.start(true);

    verifier = buildBaseVerifier();
  }

  @AfterEach
  public void after() throws InterruptedException, TimeoutException {
    standaloneEnvironment.stop();
    environmentWorkingDir.toFile().delete();
  }

  protected void deploy() throws Exception {
    LOG.info("Executing mule:deploy goal...");

    // TODO check why we have this sleep here
    Thread.sleep(30000);

    verifier.setSystemProperty("mule.version", getMuleVersion());
    verifier.setSystemProperty("applicationName", getApplication());
    verifier.addCliArguments(DEPLOY_GOAL, "-DmuleDeploy");
    verifier.execute();
  }

  protected void assertAndVerify() throws Exception {
    assertThat(standaloneEnvironment.isRunning()).describedAs("Standalone should be running ").isTrue();
    assertDeployment();
    verifier.verifyErrorFreeLog();
  }

  public abstract void assertDeployment();

  @Test
  public void testAgentDeploy() throws Exception {
    deploy();
    assertAndVerify();
  }
}

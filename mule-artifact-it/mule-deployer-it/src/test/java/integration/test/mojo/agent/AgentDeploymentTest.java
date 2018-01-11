/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo.agent;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import integration.test.mojo.AbstractDeploymentTest;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import integration.test.util.StandaloneEnvironment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public abstract class AgentDeploymentTest extends AbstractDeploymentTest {

  @Rule
  public TemporaryFolder environmentWorkingDir = new TemporaryFolder();

  protected static StandaloneEnvironment standaloneEnvironment;
  protected Verifier verifier;
  private String application;

  public AgentDeploymentTest(String application) {
    this.application = application;
  }

  public String getApplication() {
    return application;
  }

  @Before
  public void before() throws VerificationException, InterruptedException, IOException, TimeoutException {
    log.info("Initializing context...");

    standaloneEnvironment = new StandaloneEnvironment(environmentWorkingDir.getRoot(), getMuleVersion());
    standaloneEnvironment.start(true);

    verifier = buildBaseVerifier();
  }

  @After
  public void after() throws IOException, InterruptedException {
    standaloneEnvironment.stop();
    verifier.resetStreams();
    environmentWorkingDir.delete();
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

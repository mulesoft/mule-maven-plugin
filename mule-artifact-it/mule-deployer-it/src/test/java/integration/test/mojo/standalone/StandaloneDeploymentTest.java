/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo.standalone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

import integration.test.mojo.AbstractDeploymentTest;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import integration.test.util.StandaloneEnvironment;

public class StandaloneDeploymentTest extends AbstractDeploymentTest {

  public static final String VERIFIER_MULE_VERSION = "mule.version";
  public static final String VERIFIER_MULE_TIMEOUT = "mule.timeout";
  public static final String VERIFIER_MULE_HOME_TEST = "mule.home.test";
  protected static StandaloneEnvironment standaloneEnvironment;

  @TempDir
  public Path environmentWorkingDir;

  private Verifier verifier;
  private String application;

  public StandaloneDeploymentTest(String application) {
    this.application = application;
  }

  public String getApplication() {
    return application;
  }

  @BeforeEach
  public void before() throws VerificationException, InterruptedException, IOException, TimeoutException {
    log = LoggerFactory.getLogger(this.getClass());
    log.info("Initializing context...");

    standaloneEnvironment = new StandaloneEnvironment(environmentWorkingDir.toFile(), getMuleVersion());
    standaloneEnvironment.start(false);

    verifier = buildBaseVerifier();
    verifier.setEnvironmentVariable(VERIFIER_MULE_VERSION, getMuleVersion());
    verifier.setEnvironmentVariable(VERIFIER_MULE_TIMEOUT, System.getProperty("mule.timeout"));
    // TODO find out why we need this
    verifier.setEnvironmentVariable(VERIFIER_MULE_HOME_TEST, standaloneEnvironment.getMuleHome());
  }

  @AfterEach
  public void after() throws InterruptedException, TimeoutException {
    standaloneEnvironment.stop();
    verifier.resetStreams();
    environmentWorkingDir.toFile().delete();
  }

  protected void deploy() throws VerificationException {
    log.info("Executing mule:deploy goal...");
    verifier.addCliOption("-DmuleDeploy");
    verifier.executeGoal(DEPLOY_GOAL);

    assertThat("Standalone should be running ", standaloneEnvironment.isRunning(), is(true));
  }
}

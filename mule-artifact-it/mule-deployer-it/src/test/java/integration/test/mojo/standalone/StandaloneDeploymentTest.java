/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package integration.test.mojo.standalone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import integration.test.mojo.AbstractDeploymentTest;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;

import integration.test.util.StandaloneEnvironment;

public class StandaloneDeploymentTest extends AbstractDeploymentTest {

  public static final String VERIFIER_MULE_VERSION = "mule.version";
  public static final String VERIFIER_MULE_TIMEOUT = "mule.timeout";
  public static final String VERIFIER_MULE_HOME_TEST = "mule.home.test";
  protected static StandaloneEnvironment standaloneEnvironment;

  @Rule
  public TemporaryFolder environmentWorkingDir = new TemporaryFolder();

  private Verifier verifier;
  private String application;

  public StandaloneDeploymentTest(String application) {
    this.application = application;
  }

  public String getApplication() {
    return application;
  }

  @Before
  public void before() throws VerificationException, InterruptedException, IOException, TimeoutException {
    log = LoggerFactory.getLogger(this.getClass());
    log.info("Initializing context...");

    standaloneEnvironment = new StandaloneEnvironment(environmentWorkingDir.getRoot(), getMuleVersion());
    standaloneEnvironment.start(false);

    verifier = buildBaseVerifier();
    verifier.setEnvironmentVariable(VERIFIER_MULE_VERSION, getMuleVersion());
    verifier.setEnvironmentVariable(VERIFIER_MULE_TIMEOUT, System.getProperty("mule.timeout"));
    // TODO find out why we need this
    verifier.setEnvironmentVariable(VERIFIER_MULE_HOME_TEST, standaloneEnvironment.getMuleHome());
  }

  @After
  public void after() throws IOException, InterruptedException {
    standaloneEnvironment.stop();
    verifier.resetStreams();
    environmentWorkingDir.delete();
  }

  protected void deploy() throws VerificationException {
    log.info("Executing mule:deploy goal...");
    verifier.addCliOption("-DmuleDeploy");
    verifier.executeGoal(DEPLOY_GOAL);

    assertThat("Standalone should be running ", standaloneEnvironment.isRunning(), is(true));
  }
}

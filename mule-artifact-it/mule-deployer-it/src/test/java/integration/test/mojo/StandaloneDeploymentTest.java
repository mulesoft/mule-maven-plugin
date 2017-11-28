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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;

import integration.test.util.StandaloneEnvironment;

public class StandaloneDeploymentTest extends AbstractDeploymentTest {

  private static final String APPLICATION = "empty-mule-deploy-standalone-project";

  public static final String VERIFIER_MULE_VERSION = "mule.version";
  public static final String VERIFIER_MULE_TIMEOUT = "mule.timeout";
  public static final String VERIFIER_MULE_HOME_TEST = "mule.home.test";

  @Rule
  public TemporaryFolder environmentWorkingDir = new TemporaryFolder();

  private Verifier verifier;

  public String getApplication() {
    return APPLICATION;
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

  @Test(timeout = 60000)
  public void testStandaloneDeploy() throws IOException, VerificationException, InterruptedException {
    log.info("Executing mule:deploy goal...");
    verifier.addCliOption("-DmuleDeploy");
    verifier.executeGoal(DEPLOY_GOAL);

    assertThat("Standalone should be running ", standaloneEnvironment.isRunning(), is(true));
    assertThat("Failed to deploy: " + APPLICATION, standaloneEnvironment.isDeployed(APPLICATION), is(true));
  }
}

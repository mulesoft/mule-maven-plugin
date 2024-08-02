/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo.standalone;

import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

import integration.test.mojo.AbstractDeploymentTest;
import org.apache.maven.shared.verifier.VerificationException;
import org.apache.maven.shared.verifier.Verifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import integration.test.util.StandaloneEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

public class StandaloneDeploymentTest extends AbstractDeploymentTest {

  public static final String VERIFIER_MULE_VERSION = "mule.version";
  public static final String VERIFIER_MULE_TIMEOUT = "mule.timeout";
  public static final String VERIFIER_MULE_HOME_TEST = "mule.home.test";
  protected static StandaloneEnvironment standaloneEnvironment;

  @TempDir
  public Path environmentWorkingDir;

  private Verifier verifier;
  private final String application;

  public StandaloneDeploymentTest(String application) {
    this.application = application;
  }

  public String getApplication() {
    return application;
  }

  @BeforeEach
  public void before() throws Exception {
    LOG.info("Initializing context...");

    standaloneEnvironment = new StandaloneEnvironment(environmentWorkingDir.toFile(), getMuleVersion());
    standaloneEnvironment.start(false);

    verifier = buildBaseVerifier();
    verifier.setSystemProperty(VERIFIER_MULE_VERSION, getMuleVersion());
    verifier.setSystemProperty(VERIFIER_MULE_TIMEOUT, System.getProperty("mule.timeout"));
    // TODO find out why we need this
    verifier.setSystemProperty(VERIFIER_MULE_HOME_TEST, standaloneEnvironment.getMuleHome());
  }

  @AfterEach
  public void after() throws InterruptedException, TimeoutException {
    standaloneEnvironment.stop();
    environmentWorkingDir.toFile().delete();
  }

  protected void deploy() throws VerificationException {
    LOG.info("Executing mule:deploy goal...");
    verifier.addCliArguments(DEPLOY_GOAL, "-DmuleDeploy");
    verifier.execute();

    assertThat(standaloneEnvironment.isRunning())
        .describedAs("Standalone should be running ").isTrue();
  }
}

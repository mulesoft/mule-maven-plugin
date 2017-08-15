/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integrationTests.mojo;

import integrationTests.ProjectFactory;
import integrationTests.mojo.environmentSetup.StandaloneEnvironment;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class StandaloneDeploymentTest {

  private static final String STANDALONE_TEST_ANCHOR_FILENAME = "standalone-anchor.txt";
  private static final String MULE_DEPLOY = "mule:deploy";
  private static final String MULE_VERSION = "4.0.0-SNAPSHOT"; // System.getProperty("mule.version");
  private static final String STANDALONE_DIRECTORY_NAME = "mule-enterprise-standalone-" + MULE_VERSION;
  private static Logger log;
  private static Verifier verifier;
  private static File projectBaseDirectory;
  private static ProjectFactory builder;
  private static final String INSTALL = "install";
  private static StandaloneEnvironment environment = new StandaloneEnvironment(MULE_VERSION);

  public void initializeContext() throws IOException, VerificationException {
    builder = new ProjectFactory();
    projectBaseDirectory = builder.createProjectBaseDir("empty-mule-deploy-standalone-project", this.getClass());
    verifier = new Verifier(projectBaseDirectory.getAbsolutePath());
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.setMavenDebug(true);
  }

  @Before
  public void before() throws VerificationException, InterruptedException, IOException {
    log = LoggerFactory.getLogger(this.getClass());
    log.info("Initializing context...");
    initializeContext();
    String mavenSettings = System.getenv("MAVEN_SETTINGS");
    if (mavenSettings != null) {
      verifier.addCliOption("-s " + mavenSettings);
    }
    verifier.setEnvironmentVariable("mule.version", System.getProperty("mule.version"));
    verifier.setEnvironmentVariable("mule.timeout", System.getProperty("mule.timeout"));
    verifier.setEnvironmentVariable("mule.home.test",
                                    System.getProperty("mule.home.test") + File.separator + STANDALONE_DIRECTORY_NAME);
    environment.setMuleHome(verifier.getEnvironmentVariables().get("mule.home.test"));
    environment.start();
    environment.runStandalone();
  }

  @After
  public void after() {
    verifier.resetStreams();
  }

  @Test
  public void testStandaloneDeploy() throws IOException, VerificationException, InterruptedException {
    log.info("Executing mule:deploy goal...");
    verifier.executeGoal(INSTALL);
    verifier.executeGoal(MULE_DEPLOY);
    verifyDeployment();
  }

  private void verifyDeployment() throws IOException, InterruptedException, VerificationException {
    environment.checkStandaloneStatus("Mule Enterprise Edition is running");
    environment.verifyDeployment(true, STANDALONE_TEST_ANCHOR_FILENAME);
  }
}

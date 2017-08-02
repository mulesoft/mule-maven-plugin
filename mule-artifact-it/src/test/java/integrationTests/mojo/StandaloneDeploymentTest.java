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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StandaloneDeploymentTest {

  private static final String STANDALONE_TEST_ANCHOR_FILENAME = "standalone-1.0-anchor.txt";
  private static Logger log;
  private static Verifier verifier;
  private static File projectBaseDirectory;
  private static ProjectFactory builder;
  private static final String INSTALL = "install";

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
  }

  @After
  public void after() {
    verifier.resetStreams();
  }

  @Test
  public void testStandaloneDeploy() throws IOException, VerificationException, InterruptedException {
    log.info("Executing mule:deploy goal...");
    verifier.executeGoal(INSTALL);
    verifyDeployment();
  }

  private void verifyDeployment() throws IOException, InterruptedException, VerificationException {
    String muleVersion = System.getProperty("mule.version");
    StandaloneEnvironment standaloneEnvironment = new StandaloneEnvironment(muleVersion);
    String muleHome =
        projectBaseDirectory + File.separator + "target" + File.separator + "mule-enterprise-standalone-" + muleVersion;
    standaloneEnvironment.setMuleHome(muleHome);
    standaloneEnvironment.checkStandaloneStatus("Mule Enterprise Edition is running");
    standaloneEnvironment.verifyDeployment(true, STANDALONE_TEST_ANCHOR_FILENAME);
  }
}

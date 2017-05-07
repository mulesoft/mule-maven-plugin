/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integrationTests.mojo;

import integrationTests.ProjectFactory;
import integrationTests.mojo.verifier.CloudHubVerifier;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class CloudHubDeploymentTest {

  private static Logger log;
  private static Verifier verifier;
  private static File projectBaseDirectory;
  private static ProjectFactory builder;
  private static final String INSTALL = "install";
  private static final String MULE_DEPLOY = "mule:deploy";
  private static final CloudHubVerifier cloudHubVerifier = new CloudHubVerifier();

  public void initializeContext() throws IOException, VerificationException {
    builder = new ProjectFactory();
    projectBaseDirectory = builder.createProjectBaseDir("empty-mule-deploy-cloudhub-project", this.getClass());
    verifier = new Verifier(projectBaseDirectory.getAbsolutePath());
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.setMavenDebug(true);
  }

  @Before
  public void before() throws VerificationException, InterruptedException, IOException {
    log = LoggerFactory.getLogger(this.getClass());
    log.info("Initializing context...");
    initializeContext();
    verifier.executeGoal(INSTALL);
  }

  @Test
  public void testCloudHubDeploy() throws VerificationException {
    verifier.setEnvironmentVariable("username", System.getProperty("username"));
    verifier.setEnvironmentVariable("password", System.getProperty("password"));
    verifier.setEnvironmentVariable("environment", "Production");
    verifier.setEnvironmentVariable("mule.version", System.getProperty("mule.version"));
    verifier.executeGoal(MULE_DEPLOY);
    cloudHubVerifier.verify();
    verifier.verifyErrorFreeLog();
  }
}

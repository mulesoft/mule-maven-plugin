/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integrationTests.mojo;

import integrationTests.ProjectFactory;
import integrationTests.mojo.verifier.CloudHubDeploymentVerifier;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class CloudHubDeploymentTest {

  private static final String MULE_UNDEPLOY = "mule:undeploy";
  private static final String APPLICATION_NAME = "empty-mule-deploy-cloudhub-project";
  private static Logger log;
  private static Verifier verifier;
  private static File projectBaseDirectory;
  private static ProjectFactory builder;
  private static final String INSTALL = "install";
  private static final String MULE_DEPLOY = "mule:deploy";
  private static final CloudHubDeploymentVerifier cloudHubDeploymentVerifier = new CloudHubDeploymentVerifier();

  public void initializeContext() throws IOException, VerificationException {
    builder = new ProjectFactory();
    projectBaseDirectory = builder.createProjectBaseDir(APPLICATION_NAME, this.getClass());
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
    verifier.setEnvironmentVariable("username", System.getProperty("username"));
    verifier.setEnvironmentVariable("password", System.getProperty("password"));
    verifier.setEnvironmentVariable("environment", "Production");
    verifier.setEnvironmentVariable("mule.version", System.getProperty("mule.version"));
  }

  @Test
  public void testCloudHubDeploy() throws VerificationException, InterruptedException, TimeoutException {
    log.info("Executing mule:deploy goal...");
    verifier.executeGoal(MULE_DEPLOY);
    cloudHubDeploymentVerifier.verifyIsDeployed();
    log.info("Application " + APPLICATION_NAME + " successfully deployed to CloudHub.");
    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testCloudHubUndeploy() throws VerificationException, InterruptedException, TimeoutException {
    log.info("Executing mule:deploy goal...");
    verifier.executeGoal(MULE_DEPLOY);
    cloudHubDeploymentVerifier.validateStatus(cloudHubDeploymentVerifier.DEPLOYED_STATUS);
    log.info("Application successfully deployed to CloudHub.");
    log.info("Executing mule:undeploy goal...");
    verifier.executeGoal(MULE_UNDEPLOY);
    cloudHubDeploymentVerifier.verifyIsUndeployed();
    log.info("Application " + APPLICATION_NAME + " successfully undeployed from CloudHub.");
    verifier.verifyErrorFreeLog();
  }
}

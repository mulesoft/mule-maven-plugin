/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integrationTests.mojo;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integrationTests.ProjectFactory;
import integrationTests.mojo.environment.verifier.CloudHubDeploymentVerifier;

@Ignore
public class CloudHubDeploymentTest implements SettingsConfigurator {

  private static final String MULE_UNDEPLOY = "mule:undeploy";
  private static Logger log;
  private static Verifier verifier;
  private static File projectBaseDirectory;
  private static ProjectFactory builder;
  private static final String INSTALL = "install";
  private static final String MULE_DEPLOY = "mule:deploy";
  private static final CloudHubDeploymentVerifier cloudHubDeploymentVerifier = new CloudHubDeploymentVerifier();
  private static final int APPLICATION_NAME_LENGTH = 10;
  private static final String APPLICATION_NAME = RandomStringUtils.randomAlphabetic(APPLICATION_NAME_LENGTH).toLowerCase();

  public void initializeContext() throws IOException, VerificationException {
    builder = new ProjectFactory();
    projectBaseDirectory = builder.createProjectBaseDir("empty-mule-deploy-cloudhub-project", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
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
    verifier.setEnvironmentVariable("mule.version", "4.0.0-FD"); // MMP-252
    verifier.setEnvironmentVariable("cloudhub.application.name", APPLICATION_NAME);

  }

  @Test
  public void testCloudHubDeploy() throws VerificationException, InterruptedException, TimeoutException {
    log.info("Executing mule:deploy goal...");
    verifier.executeGoal(MULE_DEPLOY);
    cloudHubDeploymentVerifier.verifyIsDeployed(APPLICATION_NAME);
    log.info("Application " + APPLICATION_NAME + " successfully deployed to CloudHub.");
    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testCloudHubUndeploy() throws VerificationException, InterruptedException, TimeoutException {
    log.info("Executing mule:deploy goal...");
    verifier.executeGoal(MULE_DEPLOY);
    cloudHubDeploymentVerifier.validateStatus(APPLICATION_NAME, cloudHubDeploymentVerifier.DEPLOYED_STATUS);
    log.info("Application successfully deployed to CloudHub.");
    log.info("Executing mule:undeploy goal...");
    verifier.executeGoal(MULE_UNDEPLOY);
    cloudHubDeploymentVerifier.verifyIsUndeployed(APPLICATION_NAME);
    log.info("Application " + APPLICATION_NAME + " successfully undeployed from CloudHub.");
    verifier.verifyErrorFreeLog();
  }
}

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integrationTests.mojo;

import java.io.File;
import java.io.IOException;

import integrationTests.ProjectFactory;
import integrationTests.mojo.environmentSetup.StandaloneEnvironment;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AgentDeploymentTest {

  private static final String AGENT_TEST_ANCHOR_FILENAME = "agent-anchor.txt";
  private static Logger log;
  private static Verifier verifier;
  private static File projectBaseDirectory;
  private static ProjectFactory builder;
  private static final String INSTALL = "install";
  private static final String MULE_DEPLOY = "mule:deploy";
  private static final String MULE_UNDEPLOY = "mule:undeploy";
  private StandaloneEnvironment standaloneEnvironment;

  public void initializeContext() throws IOException, VerificationException {
    builder = new ProjectFactory();
    projectBaseDirectory = builder.createProjectBaseDir("empty-mule-deploy-agent-project", this.getClass());
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
    standaloneEnvironment = new StandaloneEnvironment("3.8.1");
    standaloneEnvironment.start();
    standaloneEnvironment.runAgent();
  }

  @After
  public void after() {
    verifier.resetStreams();
  }

  @Test
  public void testAgentDeploy() throws IOException, VerificationException, InterruptedException {
    log.info("Executing mule:deploy goal...");
    verifier.executeGoal(MULE_DEPLOY);
    standaloneEnvironment.verifyDeployment(true, AGENT_TEST_ANCHOR_FILENAME);
    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testAgentDeployUndeploy() throws IOException, VerificationException, InterruptedException {
    log.info("Executing mule:deploy goal...");
    verifier.executeGoal(MULE_DEPLOY);

    log.info("Executing mule:undeploy goal...");
    verifier.executeGoal(MULE_UNDEPLOY);
    standaloneEnvironment.verifyDeployment(false, AGENT_TEST_ANCHOR_FILENAME);
    verifier.verifyErrorFreeLog();
  }
}

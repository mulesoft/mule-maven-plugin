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

import integrationTests.ProjectFactory;
import integrationTests.mojo.environment.setup.StandaloneEnvironment;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentDeploymentTest implements SettingsConfigurator {

  private static final String AGENT_TEST_ANCHOR_FILENAME = "agent-anchor.txt";
  private static Logger log;
  private static Verifier verifier;
  private static File projectBaseDirectory;
  private static ProjectFactory builder;
  private static final String DEPLOY = "deploy";
  private StandaloneEnvironment standaloneEnvironment;

  public void initializeContext() throws IOException, VerificationException {
    builder = new ProjectFactory();
    projectBaseDirectory = builder.createProjectBaseDir("empty-mule-deploy-agent-project", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.setMavenDebug(true);
  }

  @Before
  public void before() throws VerificationException, InterruptedException, IOException {
    log = LoggerFactory.getLogger(this.getClass());
    log.info("Initializing context...");
    initializeContext();
    standaloneEnvironment = new StandaloneEnvironment(DEFAULT_MULE_VERSION);
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
    verifier.setSystemProperty("applicationName", "agent");
    verifier.addCliOption("-DmuleDeploy");
    Thread.sleep(30000);
    verifier.executeGoal(DEPLOY);
    standaloneEnvironment.verifyDeployment(true, AGENT_TEST_ANCHOR_FILENAME);
    verifier.verifyErrorFreeLog();
  }
}

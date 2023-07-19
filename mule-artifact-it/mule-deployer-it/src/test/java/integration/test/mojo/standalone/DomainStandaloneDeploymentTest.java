/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package integration.test.mojo.standalone;

import org.apache.maven.it.VerificationException;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DomainStandaloneDeploymentTest extends StandaloneDeploymentTest {

  private static final String DOMAIN = "empty-mule-deploy-standalone-domain-project";

  public DomainStandaloneDeploymentTest() {
    super(DOMAIN);
  }

  @Test(timeout = 60000)
  public void standaloneDomainDeployTest() throws IOException, VerificationException, InterruptedException {
    deploy();
    assertThat("Failed to deploy: " + DOMAIN, standaloneEnvironment.isDomainDeployed(DOMAIN), is(true));
  }
}

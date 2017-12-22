/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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

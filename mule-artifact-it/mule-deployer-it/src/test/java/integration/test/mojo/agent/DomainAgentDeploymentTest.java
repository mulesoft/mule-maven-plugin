/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo.agent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DomainAgentDeploymentTest extends AgentDeploymentTest {

  private static final String DOMAIN = "empty-mule-deploy-domain-agent-project";

  public DomainAgentDeploymentTest() {
    super(DOMAIN);
  }

  @Override
  public void assertDeployment() {
    assertThat("Failed to deploy: " + DOMAIN, standaloneEnvironment.isDomainDeployed(DOMAIN), is(true));
  }
}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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

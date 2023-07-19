/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package integration.test.mojo.agent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ApplicationAgentDeploymentTest extends AgentDeploymentTest {

  private static final String APPLICATION = "empty-mule-deploy-application-agent-project";

  public ApplicationAgentDeploymentTest() {
    super(APPLICATION);
  }

  @Override
  public void assertDeployment() {
    assertThat("Failed to deploy: " + APPLICATION, standaloneEnvironment.isDeployed(APPLICATION), is(true));
  }
}

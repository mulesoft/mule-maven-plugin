/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo.agent;

import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.Tag;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Tag("standalone")
@DisabledOnJre(JRE.JAVA_8)
@DisabledOnOs(OS.WINDOWS)
public class ApplicationAgentDeploymentTest extends AgentDeploymentTest {

  private static final String APPLICATION = "empty-mule-deploy-application-agent-project";

  public ApplicationAgentDeploymentTest() {
    super(APPLICATION);
  }

  @Override
  public void assertDeployment() {
    assertThat(standaloneEnvironment.isDeployed(APPLICATION)).describedAs("Failed to deploy: " + APPLICATION).isTrue();
  }
}

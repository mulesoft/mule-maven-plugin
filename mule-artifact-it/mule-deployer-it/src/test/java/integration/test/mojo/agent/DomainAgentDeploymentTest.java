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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisabledOnJre(JRE.JAVA_8)
@DisabledOnOs(OS.WINDOWS)
public class DomainAgentDeploymentTest extends AgentDeploymentTest {

  private static final String DOMAIN = "empty-mule-deploy-domain-agent-project";

  public DomainAgentDeploymentTest() {
    super(DOMAIN);
  }

  @Override
  public void assertDeployment() {
    assertThat(standaloneEnvironment.isDomainDeployed(DOMAIN)).describedAs("Failed to deploy: " + DOMAIN).isTrue();
  }
}

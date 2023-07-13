/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.core.exception.DeploymentException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

public class AgentDeploymentTest {

  private AgentDeployment deploymentSpy;

  @BeforeEach
  public void setUp() {
    deploymentSpy = spy(AgentDeployment.class);
  }

  @Test
  public void setAgentDeploymentValuesAnypointUriSetSystemPropertyTest() throws DeploymentException {
    String anypointUri = "www.lala.com";
    System.setProperty("anypoint.baseUri", anypointUri);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.getUri()).describedAs("The anypoint baseUri was not resolved by system property")
        .isEqualTo(anypointUri);
    System.clearProperty("anypoint.baseUri");
  }

  @Test
  public void setAgentDeploymentValuesAnypointUriNotSetTest() throws DeploymentException {
    String anypointUriDefaultValue = "https://anypoint.mulesoft.com";
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.getUri()).describedAs("The anypoint baseUri was not resolved to the default value")
        .isEqualTo(anypointUriDefaultValue);
  }
}

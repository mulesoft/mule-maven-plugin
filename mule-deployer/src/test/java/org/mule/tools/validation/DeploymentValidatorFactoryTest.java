/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.model.standalone.StandaloneDeployment;
import org.mule.tools.validation.agent.AgentDeploymentValidator;
import org.mule.tools.validation.arm.ArmDeploymentValidator;
import org.mule.tools.validation.cloudhub.CloudHubDeploymentValidator;
import org.mule.tools.validation.standalone.StandaloneDeploymentValidator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mule.tools.validation.DeploymentValidatorFactory.createDeploymentValidator;

public class DeploymentValidatorFactoryTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void createDeploymentValidatorToAgentTest() throws DeploymentException {
    assertThat("The deployment validator is not the expected", createDeploymentValidator(new AgentDeployment()),
               instanceOf(AgentDeploymentValidator.class));
  }

  @Test
  public void createDeploymentValidatorToStandaloneTest() throws DeploymentException {
    assertThat("The deployment validator is not the expected", createDeploymentValidator(new StandaloneDeployment()),
               instanceOf(StandaloneDeploymentValidator.class));
  }

  @Test
  public void createDeploymentValidatorToArmTest() throws DeploymentException {
    assertThat("The deployment validator is not the expected", createDeploymentValidator(new ArmDeployment()),
               instanceOf(ArmDeploymentValidator.class));
  }

  @Test
  public void createDeploymentValidatorToCloudHubTest() throws DeploymentException {
    assertThat("The deployment validator is not the expected", createDeploymentValidator(new CloudHubDeployment()),
               instanceOf(CloudHubDeploymentValidator.class));
  }

  @Test
  public void createDeploymentValidatorUnknownDeploymentExceptionTest() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    createDeploymentValidator(mock(Deployment.class));
  }
}

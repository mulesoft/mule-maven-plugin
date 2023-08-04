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

import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mule.tools.validation.DeploymentValidatorFactory.createDeploymentValidator;

public class DeploymentValidatorFactoryTest {

  @Test
  public void createDeploymentValidatorToAgentTest() throws DeploymentException {
    assertThat(createDeploymentValidator(new AgentDeployment())).describedAs("The deployment validator is not the expected")
        .isInstanceOf(AgentDeploymentValidator.class);
  }

  @Test
  public void createDeploymentValidatorToStandaloneTest() throws DeploymentException {
    assertThat(createDeploymentValidator(new StandaloneDeployment())).describedAs("The deployment validator is not the expected")
        .isInstanceOf(StandaloneDeploymentValidator.class);
  }

  @Test
  public void createDeploymentValidatorToArmTest() throws DeploymentException {
    assertThat(createDeploymentValidator(new ArmDeployment())).describedAs("The deployment validator is not the expected")
        .isInstanceOf(ArmDeploymentValidator.class);
  }

  @Test
  public void createDeploymentValidatorToCloudHubTest() throws DeploymentException {
    assertThat(createDeploymentValidator(new CloudHubDeployment())).describedAs("The deployment validator is not the expected")
        .isInstanceOf(CloudHubDeploymentValidator.class);
  }

  @Test
  public void createDeploymentValidatorUnknownDeploymentExceptionTest() {
    assertThatThrownBy(() -> createDeploymentValidator(mock(Deployment.class)))
        .isExactlyInstanceOf(DeploymentException.class);
  }
}

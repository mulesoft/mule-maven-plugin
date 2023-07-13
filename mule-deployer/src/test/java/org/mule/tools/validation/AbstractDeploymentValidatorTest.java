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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.standalone.exception.MuleControllerException;
import org.mule.tools.model.Deployment;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractDeploymentValidatorTest {

  private static final String CANNOT_RESOLVE_ENVIRONMENT_VERSION_MESSAGE = "Cannot resolve environment runtime version";
  private static final String MULE_VERSION = "4.1.0";
  private DeploymentValidatorMock validator;
  private Deployment deploymentMock;

  @Test
  public void validateMuleVersionAgainstEnvironmentMissingMuleVersionTest() {
    Exception exception = assertThrows(DeploymentException.class, () -> {
      deploymentMock = createDeploymentMock(null);
      validator = new DeploymentValidatorMock(deploymentMock);
      validator.validateMuleVersionAgainstEnvironment();
    });

    String expectedMessage = "muleVersion is not present in deployment configuration";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  private Deployment createDeploymentMock(String version) {
    Deployment deploymentMock = mock(Deployment.class);
    when(deploymentMock.getMuleVersion()).thenReturn(Optional.ofNullable(version));
    return deploymentMock;
  }

  @Test
  public void validateMuleVersionAgainstEnvironmentCannotResolveEnvironmentMuleVersionTest() {
    Exception exception = assertThrows(DeploymentException.class, () -> {
      Deployment deploymentMock = createDeploymentMock(MULE_VERSION);
      validator = new DeploymentValidatorMock(deploymentMock);
      validator.validateMuleVersionAgainstEnvironment();
    });

    String expectedMessage = CANNOT_RESOLVE_ENVIRONMENT_VERSION_MESSAGE;
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  class DeploymentValidatorMock extends AbstractDeploymentValidator {

    public DeploymentValidatorMock(Deployment deployment) {
      super(deployment);
    }

    @Override
    public EnvironmentSupportedVersions getEnvironmentSupportedVersions() throws DeploymentException {
      throw new DeploymentException(CANNOT_RESOLVE_ENVIRONMENT_VERSION_MESSAGE);
    }
  }
}

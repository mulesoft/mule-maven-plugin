/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation.arm;

import org.junit.jupiter.api.Test;
import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.validation.AbstractDeploymentValidator;
import org.mule.tools.validation.EnvironmentSupportedVersions;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ArmDeploymentValidatorTest {

  private static final String MULE_VERSION = "4.0.0";
  private static final String BASE_URI = "http://localhost:9999/";

  private static final EnvironmentSupportedVersions EXPECTED_ENVIRONMENT_SUPPORTED_VERSIONS =
      new EnvironmentSupportedVersions(MULE_VERSION);
  private final ArmDeployment armDeployment = new ArmDeployment();
  private static final DeployerLog LOG_MOCK = mock(DeployerLog.class);
  private AbstractDeploymentValidator validatorSpy;

  @Test
  public void getEnvironmentSupportedVersionsTest() throws Exception {
    armDeployment.setUri(BASE_URI);
    armDeployment.setArmInsecure(true);

    validatorSpy = spy(new ArmDeploymentValidator(armDeployment));

    ArmClient clientSpy = spy(new ArmClient(armDeployment, LOG_MOCK));
    doReturn(clientSpy).when((ArmDeploymentValidator) validatorSpy).getArmClient();
    doReturn(newArrayList(MULE_VERSION)).when((ArmDeploymentValidator) validatorSpy).findRuntimeVersion(clientSpy);

    assertThat(validatorSpy.getEnvironmentSupportedVersions())
        .describedAs("Supported version that was generated is not the expected")
        .isEqualTo(EXPECTED_ENVIRONMENT_SUPPORTED_VERSIONS);

  }
}

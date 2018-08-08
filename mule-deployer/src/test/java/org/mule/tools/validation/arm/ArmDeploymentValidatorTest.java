/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation.arm;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.validation.AbstractDeploymentValidator;
import org.mule.tools.validation.EnvironmentSupportedVersions;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ArmDeploymentValidator.class)
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
    doReturn(clientSpy).when(validatorSpy, "getArmClient");

    doReturn(newArrayList(MULE_VERSION)).when(validatorSpy, "findRuntimeVersion", clientSpy);

    assertThat("Supported version that was generated is not the expected", validatorSpy.getEnvironmentSupportedVersions(),
               equalTo(EXPECTED_ENVIRONMENT_SUPPORTED_VERSIONS));

  }
}

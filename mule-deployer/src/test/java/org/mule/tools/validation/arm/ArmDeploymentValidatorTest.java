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

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.validation.AbstractDeploymentValidator;
import org.mule.tools.validation.EnvironmentSupportedVersions;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArmDeploymentValidatorTest {

  private static final String MULE_VERSION = "4.0.0";
  private static final String BASE_URI = "http://localhost:9999/";

  private static final EnvironmentSupportedVersions EXPECTED_ENVIRONMENT_SUPPORTED_VERSIONS =
      new EnvironmentSupportedVersions(MULE_VERSION);
  private final ArmDeployment armDeployment = new ArmDeployment();
  private static final DeployerLog LOG_MOCK = mock(DeployerLog.class);
  private AbstractDeploymentValidator validatorSpy;
  private ArmClient clientSpy;

  @Test
  public void getEnvironmentSupportedVersionsTest() throws Exception {
    armDeployment.setUri(BASE_URI);
    armDeployment.setArmInsecure(true);

    validatorSpy = new TestArmDeploymentValidator(armDeployment);
    clientSpy = spy(new ArmClient(armDeployment, LOG_MOCK));

    assertThat("Supported version that was generated is not the expected", validatorSpy.getEnvironmentSupportedVersions(),
               equalTo(EXPECTED_ENVIRONMENT_SUPPORTED_VERSIONS));

  }

  protected class TestArmDeploymentValidator extends ArmDeploymentValidator {

    public TestArmDeploymentValidator(Deployment deployment) {
      super(deployment);
    }

    @Override
    protected List<String> findRuntimeVersion(ArmClient client) {
      return newArrayList(MULE_VERSION);
    }

    @Override
    protected ArmClient getArmClient() {
      return clientSpy;
    }
  }
}

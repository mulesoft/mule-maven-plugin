/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation.standalone;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.standalone.StandaloneDeployment;
import org.mule.tools.validation.AbstractDeploymentValidator;
import org.mule.tools.validation.EnvironmentSupportedVersions;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StandaloneDeploymentValidatorTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private static final String MULE_VERSION = "4.0.0";
  private static final EnvironmentSupportedVersions EXPECTED_ENVIRONMENT_SUPPORTED_VERSIONS =
      new EnvironmentSupportedVersions(MULE_VERSION);
  private final StandaloneDeployment standaloneDeployment = new StandaloneDeployment();
  private AbstractDeploymentValidator validatorSpy;

  @Test
  public void getEnvironmentSupportedVersionsTest() throws Exception {
    temporaryFolder.create();
    File muleHome = temporaryFolder.getRoot();

    standaloneDeployment.setMuleHome(muleHome);

    validatorSpy = new TestStandaloneDeploymentValidator(standaloneDeployment);

    assertThat("Supported version that was generated is not the expected", validatorSpy.getEnvironmentSupportedVersions(),
               equalTo(EXPECTED_ENVIRONMENT_SUPPORTED_VERSIONS));

  }

  protected class TestStandaloneDeploymentValidator extends StandaloneDeploymentValidator {

    public TestStandaloneDeploymentValidator(Deployment deployment) {
      super(deployment);
    }

    @Override
    protected String findRuntimeVersion(File muleHome) throws DeploymentException {
      return MULE_VERSION;
    }
  }
}

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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.model.standalone.StandaloneDeployment;
import org.mule.tools.validation.AbstractDeploymentValidator;
import org.mule.tools.validation.EnvironmentSupportedVersions;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class StandaloneDeploymentValidatorTest {

  @TempDir
  Path temporaryFolder;
  private static final String MULE_VERSION = "4.0.0";
  private static final EnvironmentSupportedVersions EXPECTED_ENVIRONMENT_SUPPORTED_VERSIONS =
      new EnvironmentSupportedVersions(MULE_VERSION);
  private final StandaloneDeployment standaloneDeployment = new StandaloneDeployment();
  private AbstractDeploymentValidator validatorSpy;

  @Test
  public void getEnvironmentSupportedVersionsTest() throws Exception {
    File muleHome = temporaryFolder.toFile();

    standaloneDeployment.setMuleHome(muleHome);

    validatorSpy = spy(new StandaloneDeploymentValidator(standaloneDeployment));

    doReturn(MULE_VERSION).when((StandaloneDeploymentValidator) validatorSpy).findRuntimeVersion(muleHome);

    assertThat(validatorSpy.getEnvironmentSupportedVersions())
        .describedAs("Supported version that was generated is not the expected")
        .isEqualTo(EXPECTED_ENVIRONMENT_SUPPORTED_VERSIONS);

  }
}

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation.cloudhub;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mule.tools.client.cloudhub.CloudhubClient;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.validation.AbstractDeploymentValidator;
import org.mule.tools.validation.EnvironmentSupportedVersions;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CloudHubDeploymentValidator.class)
public class CloudHubDeploymentValidatorTest {

  private static final String MULE_VERSION1 = "4.0.0";
  private static final String MULE_VERSION2 = "4.1.0";
  private static final String BASE_URI = "https://anypoint.mulesoft.com";
  private static Set<String> supportedVersions = newHashSet(MULE_VERSION1, MULE_VERSION2);

  private static final EnvironmentSupportedVersions EXPECTED_ENVIRONMENT_SUPPORTED_VERSIONS =
      new EnvironmentSupportedVersions(supportedVersions);

  private final CloudHubDeployment cloudHubDeployment = new CloudHubDeployment();
  private static final DeployerLog LOG_MOCK = mock(DeployerLog.class);
  private AbstractDeploymentValidator validatorSpy;

  @Test
  public void getEnvironmentSupportedVersionsTest() throws Exception {
    cloudHubDeployment.setUri(BASE_URI);

    validatorSpy = spy(new CloudHubDeploymentValidator(cloudHubDeployment));

    CloudhubClient clientSpy = spy(new CloudhubClient(cloudHubDeployment, LOG_MOCK));
    doReturn(clientSpy).when(validatorSpy, "getCloudHubClient");

    doReturn(supportedVersions).when(clientSpy).getSupportedMuleVersions();

    assertThat("Supported version that was generated is not the expected", validatorSpy.getEnvironmentSupportedVersions(),
               equalTo(EXPECTED_ENVIRONMENT_SUPPORTED_VERSIONS));

  }
}

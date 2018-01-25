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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.cloudhub.model.SupportedVersion;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.validation.AbstractDeploymentValidator;
import org.mule.tools.validation.EnvironmentSupportedVersions;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
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

  private List<SupportedVersion> supportedVersions;

  private EnvironmentSupportedVersions expectedEnvironmentSupportedVersions;


  private final CloudHubDeployment cloudHubDeployment = new CloudHubDeployment();
  private static final DeployerLog LOG_MOCK = mock(DeployerLog.class);
  private AbstractDeploymentValidator validatorSpy;

  @Before
  public void setUp() {
    SupportedVersion sv1 = new SupportedVersion();
    sv1.setVersion(MULE_VERSION1);

    SupportedVersion sv2 = new SupportedVersion();
    sv1.setVersion(MULE_VERSION2);

    supportedVersions = asList(sv1, sv2);
    expectedEnvironmentSupportedVersions = new EnvironmentSupportedVersions(supportedVersions.stream().map(sv -> sv.getVersion())
        .collect(Collectors.toSet()));
  }

  @Test
  public void getEnvironmentSupportedVersionsTest() throws Exception {
    cloudHubDeployment.setUri(BASE_URI);

    validatorSpy = spy(new CloudHubDeploymentValidator(cloudHubDeployment));

    CloudHubClient clientSpy = spy(new CloudHubClient(cloudHubDeployment, LOG_MOCK));
    doReturn(clientSpy).when(validatorSpy, "getCloudHubClient");

    doReturn(supportedVersions).when(clientSpy).getSupportedMuleVersions();

    assertThat("Supported version that was generated is not the expected", validatorSpy.getEnvironmentSupportedVersions(),
               equalTo(expectedEnvironmentSupportedVersions));

  }
}

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.cloudhub.model.SupportedVersion;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.validation.AbstractDeploymentValidator;
import org.mule.tools.validation.EnvironmentSupportedVersions;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CloudHubDeploymentValidatorTest {

  private static final String MULE_VERSION1 = "4.0.0";
  private static final String MULE_VERSION2 = "4.1.0";
  private static final String BASE_URI = "https://anypoint.mulesoft.com";

  private List<SupportedVersion> supportedVersions;

  private EnvironmentSupportedVersions expectedEnvironmentSupportedVersions;


  private final CloudHubDeployment cloudHubDeployment = new CloudHubDeployment();
  private static final DeployerLog LOG_MOCK = mock(DeployerLog.class);
  private AbstractDeploymentValidator validatorSpy;

  @BeforeEach
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
    doReturn(clientSpy).when((CloudHubDeploymentValidator) validatorSpy).getCloudHubClient();

    doReturn(supportedVersions).when(clientSpy).getSupportedMuleVersions();

    assertThat(validatorSpy.getEnvironmentSupportedVersions())
        .describedAs("Supported version that was generated is not the expected").isEqualTo(expectedEnvironmentSupportedVersions);

  }
}

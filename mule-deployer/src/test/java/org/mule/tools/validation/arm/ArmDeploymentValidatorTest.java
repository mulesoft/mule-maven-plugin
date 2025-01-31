/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation.arm;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.client.arm.model.Server;
import org.mule.tools.client.arm.model.Servers;
import org.mule.tools.client.core.exception.DeploymentException;

import org.mule.tools.client.model.TargetType;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.validation.AbstractDeploymentValidator;
import org.mule.tools.validation.EnvironmentSupportedVersions;

import java.lang.reflect.Method;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;

public class ArmDeploymentValidatorTest {

  private static final String MULE_VERSION = "4.0.0";
  private static final String BASE_URI = "http://localhost:9999/";

  private static final EnvironmentSupportedVersions EXPECTED_ENVIRONMENT_SUPPORTED_VERSIONS =
      new EnvironmentSupportedVersions(MULE_VERSION);
  private final ArmDeployment armDeployment = new ArmDeployment();
  private static final DeployerLog LOG_MOCK = mock(DeployerLog.class);
  private AbstractDeploymentValidator validatorSpy;
  private ArmDeploymentValidator validator;

  public void setUp() {
    validator = new ArmDeploymentValidator(armDeployment);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1})
  void getEnvironmentSupportedVersionsTest(int index) throws Exception {
    armDeployment.setUri(BASE_URI);
    armDeployment.setArmInsecure(true);

    validatorSpy = spy(new ArmDeploymentValidator(armDeployment));

    ArmClient clientSpy = spy(new ArmClient(armDeployment, LOG_MOCK));
    doReturn(clientSpy).when((ArmDeploymentValidator) validatorSpy).getArmClient();

    if (index == 0) {
      doReturn(newArrayList(MULE_VERSION)).when((ArmDeploymentValidator) validatorSpy).findRuntimeVersion(clientSpy);
      assertThat(validatorSpy.getEnvironmentSupportedVersions())
          .describedAs("Supported version that was generated is not the expected")
          .isEqualTo(EXPECTED_ENVIRONMENT_SUPPORTED_VERSIONS);
    } else if (index == 1) {
      doReturn(Collections.emptyList()).when((ArmDeploymentValidator) validatorSpy).findRuntimeVersion(clientSpy);

      assertThatThrownBy(() -> validatorSpy.getEnvironmentSupportedVersions())
          .isInstanceOf(DeploymentException.class)
          .hasMessageContaining("There are no runtime available in this server or serverGroup");
    }
  }

  @Test
  public void findRuntimeVersionTest1() {
    armDeployment.setUri(BASE_URI);
    armDeployment.setArmInsecure(true);
    armDeployment.setTargetType(TargetType.server);

    validatorSpy = spy(new ArmDeploymentValidator(armDeployment));
    ArmClient clientSpy = spy(new ArmClient(armDeployment, LOG_MOCK));

    Mockito.doReturn("123").when(clientSpy).getId(TargetType.server, armDeployment.getTarget());
    Server mockServer = new Server();
    mockServer.muleVersion = MULE_VERSION;
    Servers mockServerResponse = new Servers();
    mockServerResponse.data = new Server[] {mockServer};
    Mockito.doReturn(mockServerResponse).when(clientSpy).getServer(123);
    List<String> expectedRuntimeVersions = new ArrayList<>();
    expectedRuntimeVersions.add(MULE_VERSION);

    assertThat(((ArmDeploymentValidator) validatorSpy).findRuntimeVersion(clientSpy))
        .describedAs("Supported version that was generated is not the expected")
        .isEqualTo(expectedRuntimeVersions);
  }

  @Test
  public void findRuntimeVersionTest2() throws Exception {
    ArmDeployment armDeploymentMock = Mockito.mock(ArmDeployment.class);
    Mockito.when(armDeploymentMock.getUri()).thenReturn(BASE_URI);
    armDeploymentMock.setUri(BASE_URI);
    armDeploymentMock.setArmInsecure(true);

    Optional<String> muleVersionOptional = Optional.of(MULE_VERSION);
    Mockito.when(armDeploymentMock.getMuleVersion()).thenReturn(muleVersionOptional);
    Mockito.when(armDeploymentMock.getTargetType()).thenReturn(TargetType.cluster);

    validatorSpy = spy(new ArmDeploymentValidator(armDeploymentMock));
    ArmClient clientSpy = spy(new ArmClient(armDeploymentMock, LOG_MOCK));

    List<String> mockRuntimeVersions = Mockito.mock(List.class);
    Mockito.when(mockRuntimeVersions.size()).thenReturn(1);
    Mockito.when(mockRuntimeVersions.get(0)).thenReturn(MULE_VERSION);

    Method method = ArmDeploymentValidator.class.getDeclaredMethod("findRuntimeVersion", ArmClient.class);
    method.setAccessible(true);

    List<String> runtimeVersions = (List<String>) method.invoke(validatorSpy, clientSpy);

    List<String> expectedVersions = new ArrayList<>();
    expectedVersions.add(MULE_VERSION);

    assertThat(runtimeVersions)
        .describedAs("The runtime versions should match the expected Mule version")
        .isEqualTo(expectedVersions);
  }

  @Test
  void testGetArmClient() {
    armDeployment.setUri(BASE_URI);
    armDeployment.setArmInsecure(true);
    validator = new ArmDeploymentValidator(armDeployment);
    ArmDeploymentValidator validatorSpy = spy(validator);
    ArmClient clientSpy = spy(new ArmClient(armDeployment, LOG_MOCK));
    doReturn(clientSpy).when(validatorSpy).getArmClient();
    ArmClient clientReturned = validatorSpy.getArmClient();
    assertThat(clientReturned).isEqualTo(clientSpy);
    verify(validatorSpy).getArmClient();
  }
}

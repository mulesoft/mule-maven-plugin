/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.arm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.client.arm.ApplicationMetadata;
import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.client.model.TargetType;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.standalone.exception.MuleControllerException;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.utils.DeployerLog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class ArmArtifactDeployerTest {

  private static final Integer FAKE_APPLICATION_ID = 1;
  private static final String FAKE_APPLICATION_NAME = "fake-name";

  @TempDir
  Path temporaryFolder;

  private DeployerLog logMock;
  private ArmClient clientMock;
  private ArmDeployment deploymentMock;
  private ApplicationMetadata metadataMock;
  private ArmArtifactDeployer armArtifactDeployerSpy;

  private ArmArtifactDeployer armArtifactDeployer;

  @BeforeEach
  public void setUp() throws DeploymentException {
    deploymentMock = mock(ArmDeployment.class);
    when(deploymentMock.getApplicationName()).thenReturn(FAKE_APPLICATION_NAME);

    clientMock = mock(ArmClient.class);
    when(clientMock.findApplicationId(any())).thenReturn(FAKE_APPLICATION_ID);

    logMock = mock(DeployerLog.class);

    armArtifactDeployer = new ArmArtifactDeployer(deploymentMock, clientMock, logMock);

    armArtifactDeployerSpy = spy(armArtifactDeployer);
    doNothing().when(armArtifactDeployerSpy).checkApplicationHasStarted();

    metadataMock = mock(ApplicationMetadata.class);
    doReturn(metadataMock).when(armArtifactDeployerSpy).getApplicationMetadata();
    doReturn(FAKE_APPLICATION_ID).when(armArtifactDeployerSpy).getApplicationId();
  }

  @Test
  public void deployDomainTest() {
    assertThatThrownBy(() ->  armArtifactDeployer.deployDomain())
            .isExactlyInstanceOf(DeploymentException.class);
  }

  @Test
  public void undeployDomainTest() throws DeploymentException {
    assertThatThrownBy(() ->  armArtifactDeployer.undeployDomain())
            .isExactlyInstanceOf(DeploymentException.class);
  }

  @Test
  public void deployApplicationTest() throws DeploymentException {
    armArtifactDeployerSpy.deployApplication();

    verify(clientMock).deployApplication(metadataMock);
    verify(armArtifactDeployerSpy).checkApplicationHasStarted();
    verify(clientMock, never()).undeployApplication(metadataMock);
    verify(clientMock, never()).redeployApplication(FAKE_APPLICATION_ID, metadataMock);
  }

  @Test
  public void deployApplicationSkipVerificationTest() throws DeploymentException {
    when(deploymentMock.getSkipDeploymentVerification()).thenReturn(true);
    armArtifactDeployerSpy.deployApplication();

    verify(clientMock).deployApplication(metadataMock);
    verify(armArtifactDeployerSpy, never()).checkApplicationHasStarted();
    verify(clientMock, never()).undeployApplication(metadataMock);
    verify(clientMock, never()).redeployApplication(FAKE_APPLICATION_ID, metadataMock);
  }

  @Test
  public void undeployApplicationTest() throws DeploymentException {
    armArtifactDeployerSpy.undeployApplication();

    verify(clientMock).undeployApplication(metadataMock);
    verify(clientMock, never()).deployApplication(metadataMock);
    verify(clientMock, never()).redeployApplication(FAKE_APPLICATION_ID, metadataMock);
  }

  @Test
  public void redeployApplicationTest() throws DeploymentException {
    armArtifactDeployerSpy.redeployApplication();

    verify(clientMock).redeployApplication(FAKE_APPLICATION_ID, metadataMock);
    verify(armArtifactDeployerSpy).checkApplicationHasStarted();
    verify(clientMock, never()).deployApplication(metadataMock);
    verify(clientMock, never()).undeployApplication(metadataMock);
  }

  @Test
  public void getApplicationIdTest() {
    assertThat(armArtifactDeployer.getApplicationId()).describedAs("Application id is not the expected")
        .isEqualTo(FAKE_APPLICATION_ID);
  }

  @Test
  public void getApplicationNameTest() {
    assertThat(armArtifactDeployer.getApplicationName()).describedAs("Application name is not the expected")
        .isEqualTo(FAKE_APPLICATION_NAME);
  }

  @Test
  public void getApplicationMetadataTest() {
    File artifactFile = temporaryFolder.toFile();
    when(deploymentMock.getArtifact()).thenReturn(artifactFile);

    when(deploymentMock.getApplicationName()).thenReturn(FAKE_APPLICATION_NAME);

    TargetType targetType = TargetType.server;
    when(deploymentMock.getTargetType()).thenReturn(targetType);

    String target = "target";
    when(deploymentMock.getTarget()).thenReturn(target);

    ApplicationMetadata metadata = armArtifactDeployer.getApplicationMetadata();

    assertThat(metadata.getFile()).describedAs("Artifact file is not the expected").isEqualTo(artifactFile);
    assertThat(metadata.getName()).describedAs("Application name is not the expected").isEqualTo(FAKE_APPLICATION_NAME);
    assertThat(metadata.getTargetType()).describedAs("Target type is not the expected").isEqualTo(targetType);
    assertThat(metadata.getTarget()).describedAs("Target is not the expected").isEqualTo(target);
  }

  @Test
  public void isFailIfNotExistsNotSetTest() {
    when(deploymentMock.isFailIfNotExists()).thenReturn(Optional.empty());

    assertThat(armArtifactDeployer.isFailIfNotExists()).describedAs("isFailIfNotExists method should have returned true")
        .isTrue();
  }

  @Test
  public void isFailIfNotExistsSetFalseTest() {
    when(deploymentMock.isFailIfNotExists()).thenReturn(Optional.of(false));

    assertThat(armArtifactDeployer.isFailIfNotExists()).describedAs("isFailIfNotExists method should have returned false")
        .isFalse();
  }

  @Test
  public void isFailIfNotExistsSetTrueTest() {
    when(deploymentMock.isFailIfNotExists()).thenReturn(Optional.of(true));

    assertThat(armArtifactDeployer.isFailIfNotExists()).describedAs("isFailIfNotExists method should have returned true")
        .isTrue();
  }
}

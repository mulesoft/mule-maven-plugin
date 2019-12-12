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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.client.arm.ApplicationMetadata;
import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.client.model.TargetType;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.utils.DeployerLog;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.spy;

public class ArmArtifactDeployerTest {

  private static final Integer FAKE_APPLICATION_ID = 1;
  private static final String FAKE_APPLICATION_NAME = "fake-name";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private DeployerLog logMock;
  private ArmClient clientMock;
  private ArmDeployment deploymentMock;
  private ApplicationMetadata metadataMock;
  private ArmArtifactDeployer armArtifactDeployerSpy;

  private ArmArtifactDeployer armArtifactDeployer;

  @Before
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
  public void deployDomainTest() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    armArtifactDeployer.deployDomain();
  }

  @Test
  public void undeployDomainTest() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    armArtifactDeployer.undeployDomain();
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
    assertThat("Application id is not the expected", armArtifactDeployer.getApplicationId(), equalTo(FAKE_APPLICATION_ID));
  }

  @Test
  public void getApplicationNameTest() {
    assertThat("Application name is not the expected", armArtifactDeployer.getApplicationName(), equalTo(FAKE_APPLICATION_NAME));
  }

  @Test
  public void getApplicationMetadataTest() throws IOException {
    File artifactFile = temporaryFolder.newFile();
    when(deploymentMock.getArtifact()).thenReturn(artifactFile);

    when(deploymentMock.getApplicationName()).thenReturn(FAKE_APPLICATION_NAME);

    TargetType targetType = TargetType.server;
    when(deploymentMock.getTargetType()).thenReturn(targetType);

    String target = "target";
    when(deploymentMock.getTarget()).thenReturn(target);

    ApplicationMetadata metadata = armArtifactDeployer.getApplicationMetadata();

    assertThat("Artifact file is not the expected", metadata.getFile(), equalTo(artifactFile));
    assertThat("Application name is not the expected", metadata.getName(), equalTo(FAKE_APPLICATION_NAME));
    assertThat("Target type is not the expected", metadata.getTargetType(), equalTo(targetType));
    assertThat("Target is not the expected", metadata.getTarget(), equalTo(target));
  }

  @Test
  public void isFailIfNotExistsNotSetTest() {
    when(deploymentMock.isFailIfNotExists()).thenReturn(Optional.empty());

    assertThat("isFailIfNotExists method should have returned true", armArtifactDeployer.isFailIfNotExists(), is(true));
  }

  @Test
  public void isFailIfNotExistsSetFalseTest() {
    when(deploymentMock.isFailIfNotExists()).thenReturn(Optional.of(false));

    assertThat("isFailIfNotExists method should have returned false", armArtifactDeployer.isFailIfNotExists(), is(false));
  }

  @Test
  public void isFailIfNotExistsSetTrueTest() {
    when(deploymentMock.isFailIfNotExists()).thenReturn(Optional.of(true));

    assertThat("isFailIfNotExists method should have returned true", armArtifactDeployer.isFailIfNotExists(), is(true));
  }
}

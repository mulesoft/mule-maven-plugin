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
import org.mule.tools.client.standalone.exception.DeploymentException;
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
  private ArmArtifactDeployer armArtifactDeployer;
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private ArmDeployment deploymentMock;
  private ArmClient clientMock;
  private DeployerLog logMock;
  private ApplicationMetadata metadataMock;
  private ArmArtifactDeployer armArtifactDeployerSpy;

  @Before
  public void setUp() {
    deploymentMock = mock(ArmDeployment.class);
    when(deploymentMock.getApplicationName()).thenReturn(FAKE_APPLICATION_NAME);

    clientMock = mock(ArmClient.class);
    when(clientMock.findApplicationId(any())).thenReturn(FAKE_APPLICATION_ID);

    logMock = mock(DeployerLog.class);

    armArtifactDeployer = new ArmArtifactDeployer(deploymentMock, clientMock, logMock);

    armArtifactDeployerSpy = spy(armArtifactDeployer);

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

    verify(clientMock, times(1)).deployApplication(metadataMock);
    verify(clientMock, times(0)).undeployApplication(metadataMock);
    verify(clientMock, times(0)).redeployApplication(FAKE_APPLICATION_ID, metadataMock);
  }

  @Test
  public void undeployApplicationTest() throws DeploymentException {
    armArtifactDeployerSpy.undeployApplication();

    verify(clientMock, times(1)).undeployApplication(metadataMock);
    verify(clientMock, times(0)).deployApplication(metadataMock);
    verify(clientMock, times(0)).redeployApplication(FAKE_APPLICATION_ID, metadataMock);
  }

  @Test
  public void redeployApplicationTest() {
    armArtifactDeployerSpy.redeployApplication();

    verify(clientMock, times(1)).redeployApplication(FAKE_APPLICATION_ID, metadataMock);
    verify(clientMock, times(0)).deployApplication(metadataMock);
    verify(clientMock, times(0)).undeployApplication(metadataMock);
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
  public void getClientTest() {
    verify(clientMock, times(0)).init();

    armArtifactDeployer.getClient();

    verify(clientMock, times(1)).init();

    armArtifactDeployer.getClient();

    verify(clientMock, times(1)).init();
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

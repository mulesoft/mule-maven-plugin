/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.cloudhub;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.verification.cloudhub.CloudHubDeploymentVerification;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.range;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

public class CloudHubArtifactDeployerTest {

  private static final String FAKE_APPLICATION_NAME = "fake-name";
  private static final String EXPECTED_STATUS = "status";
  private CloudHubArtifactDeployer cloudHubArtifactDeployer;

  private CloudHubDeployment deploymentMock;
  private CloudHubClient clientMock;
  private DeployerLog logMock;
  private File fileMock;
  private CloudHubArtifactDeployer cloudHubArtifactDeployerSpy;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private ApplicationMetadata metadataMock;

  @Before
  public void setUp() throws IOException {
    fileMock = temporaryFolder.newFile();
    deploymentMock = mock(CloudHubDeployment.class);
    when(deploymentMock.getApplicationName()).thenReturn(FAKE_APPLICATION_NAME);
    when(deploymentMock.getArtifact()).thenReturn(fileMock);

    clientMock = mock(CloudHubClient.class);

    logMock = mock(DeployerLog.class);
    cloudHubArtifactDeployer = new CloudHubArtifactDeployer(deploymentMock, clientMock, logMock);
    cloudHubArtifactDeployerSpy = spy(cloudHubArtifactDeployer);

    metadataMock = mock(ApplicationMetadata.class);
    doReturn(metadataMock).when(cloudHubArtifactDeployerSpy).getMetadata();
  }

  @Test(expected = DeploymentException.class)
  public void deployDomainTest() throws DeploymentException {
    cloudHubArtifactDeployer.deployDomain();
  }

  @Test(expected = DeploymentException.class)
  public void undeployDomainTest() throws DeploymentException {
    cloudHubArtifactDeployer.undeployDomain();
  }

  @Test
  public void deployApplicationVerificationStartedFailTest() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    doNothing().when(cloudHubArtifactDeployerSpy).persistApplication();
    doNothing().when(cloudHubArtifactDeployerSpy).startApplication();
    CloudHubDeploymentVerification verificationMock = mock(CloudHubDeploymentVerification.class);
    doThrow(DeploymentException.class).when(verificationMock).assertDeployment(deploymentMock);
    doReturn(verificationMock).when(cloudHubArtifactDeployerSpy).getDeploymentVerification();

    cloudHubArtifactDeployerSpy.deployApplication();

    verify(cloudHubArtifactDeployerSpy).persistApplication();
    verify(cloudHubArtifactDeployerSpy).startApplication();
    verify(cloudHubArtifactDeployerSpy).undeployApplication();
  }

  @Test
  public void deployApplicationTest() throws DeploymentException {
    doNothing().when(cloudHubArtifactDeployerSpy).persistApplication();
    doNothing().when(cloudHubArtifactDeployerSpy).startApplication();
    doNothing().when(cloudHubArtifactDeployerSpy).checkApplicationHasStarted();

    cloudHubArtifactDeployerSpy.deployApplication();

    verify(cloudHubArtifactDeployerSpy).persistApplication();
    // verify(cloudHubArtifactDeployerSpy).uploadContents();
    verify(cloudHubArtifactDeployerSpy).startApplication();
    verify(cloudHubArtifactDeployerSpy).checkApplicationHasStarted();
  }

  @Test
  public void undeployApplicationTest() throws DeploymentException {
    cloudHubArtifactDeployerSpy.undeployApplication();

    verify(clientMock, times(1)).stopApplication(FAKE_APPLICATION_NAME);
  }

  @Test
  public void persistApplicationAvailableNameTest() throws DeploymentException {
    doNothing().when(cloudHubArtifactDeployerSpy).createApplication(metadataMock);

    doReturn(true).when(clientMock).isDomainAvailable(FAKE_APPLICATION_NAME);

    cloudHubArtifactDeployerSpy.persistApplication();

    verify(cloudHubArtifactDeployerSpy).createApplication(metadataMock);
    verify(cloudHubArtifactDeployerSpy, never()).updateApplication(metadataMock);
  }

  @Test
  public void persistApplicationUnavailableNameTest() throws DeploymentException {
    doNothing().when(cloudHubArtifactDeployerSpy).updateApplication(metadataMock);

    doReturn(false).when(clientMock).isDomainAvailable(FAKE_APPLICATION_NAME);

    cloudHubArtifactDeployerSpy.persistApplication();

    verify(cloudHubArtifactDeployerSpy).updateApplication(metadataMock);
    verify(cloudHubArtifactDeployerSpy, never()).createApplication(metadataMock);
  }

  @Ignore // TODO fix this
  @Test
  public void createApplicationTest() {
    // when(clientMock.createApplication(metadataMock)).thenReturn(mock(Application.class));

    cloudHubArtifactDeployer.createApplication(metadataMock);

    // verify(clientMock).createApplication(metadataMock);
  }

  @Ignore // TODO fix this
  @Test
  public void updateExistentApplicationTest() throws DeploymentException {
    Application applicationMock = mock(Application.class);
    doReturn(applicationMock).when(cloudHubArtifactDeployerSpy).findApplicationFromCurrentUser(FAKE_APPLICATION_NAME);

    cloudHubArtifactDeployerSpy.updateApplication(metadataMock);

    verify(metadataMock).updateValues(applicationMock);
    // verify(clientMock).updateApplication(metadataMock);
  }

  @Ignore // TODO fix this
  @Test(expected = DeploymentException.class)
  public void updateApplicationDoesntExistTest() throws DeploymentException {
    doReturn(null).when(cloudHubArtifactDeployerSpy).findApplicationFromCurrentUser(FAKE_APPLICATION_NAME);

    cloudHubArtifactDeployerSpy.updateApplication(metadataMock);

    verify(metadataMock, never()).updateValues(any());
    // verify(clientMock).updateApplication(metadataMock);
  }

  @Test
  public void startApplicationTest() {
    cloudHubArtifactDeployer.startApplication();

    verify(clientMock).startApplication(FAKE_APPLICATION_NAME);
  }

  @Test
  public void checkApplicationHasStartedTest() throws DeploymentException {
    CloudHubDeploymentVerification verificationMock = mock(CloudHubDeploymentVerification.class);
    doReturn(verificationMock).when(cloudHubArtifactDeployerSpy).getDeploymentVerification();

    cloudHubArtifactDeployerSpy.checkApplicationHasStarted();

    verify(verificationMock).assertDeployment(deploymentMock);
  }

  @Test(expected = IllegalArgumentException.class)
  public void findApplicationFromCurrentUserNullArgumentTest() {
    cloudHubArtifactDeployer.findApplicationFromCurrentUser(null);
  }

  @Test
  public void findApplicationFromCurrentUserTest() {
    List<Application> applications = getApplications();

    Application fakeApplication = new Application();
    fakeApplication.setDomain(FAKE_APPLICATION_NAME);
    applications.add(fakeApplication);

    when(clientMock.getApplications()).thenReturn(applications);

    assertThat("Found application is not the expected",
               cloudHubArtifactDeployer.findApplicationFromCurrentUser(FAKE_APPLICATION_NAME), equalTo(fakeApplication));
  }

  @Test
  public void findApplicationFromCurrentUserNotExistentTest() {
    List<Application> applications = getApplications();

    when(clientMock.getApplications()).thenReturn(applications);

    assertThat("The method should have returned null",
               cloudHubArtifactDeployer.findApplicationFromCurrentUser(FAKE_APPLICATION_NAME), equalTo(null));
  }

  @Test
  public void retryValidationTest() {

  }

  @Test
  public void getApplicationNameTest() {
    assertThat("Application name is not the expected", cloudHubArtifactDeployer.getApplicationName(),
               equalTo(FAKE_APPLICATION_NAME));
  }

  @Test
  public void getClientTest() {
    verify(clientMock, never()).init();

    cloudHubArtifactDeployer.getClient();

    verify(clientMock).init();

    cloudHubArtifactDeployer.getClient();

    verify(clientMock).init();
  }

  public List<Application> getApplications() {
    List<Application> applications =
        range(0, 10).mapToObj(i -> toString()).map(this::createApplication).collect(Collectors.toList());

    return applications;
  }

  private Application createApplication(String name) {
    Application app = new Application();
    app.setDomain("app" + name);
    return app;
  }
}

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.client.cloudhub.Application;
import org.mule.tools.client.cloudhub.ApplicationMetadata;
import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.utils.DeployerLog;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.range;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;
import static org.mule.tools.deployment.cloudhub.CloudHubArtifactDeployer.*;

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
  public void deployApplicationTest() throws DeploymentException {
    doNothing().when(cloudHubArtifactDeployerSpy).persistApplication();
    doNothing().when(cloudHubArtifactDeployerSpy).uploadContents();
    doNothing().when(cloudHubArtifactDeployerSpy).startApplication();

    cloudHubArtifactDeployerSpy.deployApplication();

    verify(cloudHubArtifactDeployerSpy, times(1)).persistApplication();
    verify(cloudHubArtifactDeployerSpy, times(1)).uploadContents();
    verify(cloudHubArtifactDeployerSpy, times(1)).startApplication();
  }

  @Test
  public void undeployApplicationTest() throws DeploymentException {
    cloudHubArtifactDeployerSpy.undeployApplication();

    verify(clientMock, times(1)).stopApplication(FAKE_APPLICATION_NAME);
  }

  @Test
  public void persistApplicationAvailableNameTest() throws DeploymentException {
    doNothing().when(cloudHubArtifactDeployerSpy).createApplication(metadataMock);

    doReturn(true).when(clientMock).isNameAvailable(FAKE_APPLICATION_NAME);

    cloudHubArtifactDeployerSpy.persistApplication();

    verify(cloudHubArtifactDeployerSpy, times(1)).createApplication(metadataMock);
    verify(cloudHubArtifactDeployerSpy, times(0)).updateApplication(metadataMock);
  }

  @Test
  public void persistApplicationUnavailableNameTest() throws DeploymentException {
    doNothing().when(cloudHubArtifactDeployerSpy).updateApplication(metadataMock);

    doReturn(false).when(clientMock).isNameAvailable(FAKE_APPLICATION_NAME);

    cloudHubArtifactDeployerSpy.persistApplication();

    verify(cloudHubArtifactDeployerSpy, times(1)).updateApplication(metadataMock);
    verify(cloudHubArtifactDeployerSpy, times(0)).createApplication(metadataMock);
  }

  @Test
  public void uploadContentsTest() throws IOException {
    cloudHubArtifactDeployer.uploadContents();

    verify(clientMock, times(1)).uploadFile(FAKE_APPLICATION_NAME, fileMock);
  }

  @Test
  public void createApplicationTest() {
    when(clientMock.createApplication(metadataMock)).thenReturn(mock(Application.class));

    cloudHubArtifactDeployer.createApplication(metadataMock);

    verify(clientMock, times(1)).createApplication(metadataMock);
  }

  @Test
  public void updateExistentApplicationTest() throws DeploymentException {
    Application applicationMock = mock(Application.class);
    doReturn(applicationMock).when(cloudHubArtifactDeployerSpy).findApplicationFromCurrentUser(FAKE_APPLICATION_NAME);

    cloudHubArtifactDeployerSpy.updateApplication(metadataMock);

    verify(metadataMock, times(1)).updateValues(applicationMock);
    verify(clientMock, times(1)).updateApplication(metadataMock);
  }

  @Test(expected = DeploymentException.class)
  public void updateApplicationDoesntExistTest() throws DeploymentException {
    doReturn(null).when(cloudHubArtifactDeployerSpy).findApplicationFromCurrentUser(FAKE_APPLICATION_NAME);

    cloudHubArtifactDeployerSpy.updateApplication(metadataMock);

    verify(metadataMock, times(0)).updateValues(any());
    verify(clientMock, times(1)).updateApplication(metadataMock);
  }

  @Test
  public void startApplicationTest() {
    cloudHubArtifactDeployer.startApplication();

    verify(clientMock, times(1)).startApplication(FAKE_APPLICATION_NAME);
  }

  @Test
  public void checkApplicationHasStartedTest() throws DeploymentException {
    doNothing().when(cloudHubArtifactDeployerSpy).validateApplicationIsInStatus(anyString(), anyString());

    cloudHubArtifactDeployerSpy.checkApplicationHasStarted();

    verify(cloudHubArtifactDeployerSpy, times(1)).validateApplicationIsInStatus(FAKE_APPLICATION_NAME, "STARTED");
  }

  @Test(expected = IllegalArgumentException.class)
  public void findApplicationFromCurrentUserNullArgumentTest() {
    cloudHubArtifactDeployer.findApplicationFromCurrentUser(null);
  }

  @Test
  public void findApplicationFromCurrentUserTest() {
    List<Application> applications = getApplications();

    Application fakeApplication = new Application();
    fakeApplication.domain = FAKE_APPLICATION_NAME;
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
  public void isExpectedStatusApplicationDoesNotExistTest() {
    doReturn(null).when(clientMock).getApplication(FAKE_APPLICATION_NAME);

    assertThat("Method should have returned false",
               cloudHubArtifactDeployer.isExpectedStatus(FAKE_APPLICATION_NAME, EXPECTED_STATUS), is(false));
  }

  @Test
  public void isExpectedStatusApplicationOtherStatusTest() {
    Application fakeApplication = new Application();
    fakeApplication.status = "different " + EXPECTED_STATUS;
    doReturn(fakeApplication).when(clientMock).getApplication(FAKE_APPLICATION_NAME);

    assertThat("Method should have returned false",
               cloudHubArtifactDeployer.isExpectedStatus(FAKE_APPLICATION_NAME, EXPECTED_STATUS), is(false));
  }

  @Test
  public void isExpectedStatusApplicationTest() {
    Application fakeApplication = new Application();
    fakeApplication.status = EXPECTED_STATUS;
    doReturn(fakeApplication).when(clientMock).getApplication(FAKE_APPLICATION_NAME);

    assertThat("Method should have returned true",
               cloudHubArtifactDeployer.isExpectedStatus(FAKE_APPLICATION_NAME, EXPECTED_STATUS), is(true));
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
  public void shouldValidateApplicationHasStartedTrueTest() {
    System.setProperty(VALIDATE_APPLICATION_STARTED_SYSTEM_PROPERTY, "true");

    assertThat("Method should have returned true", cloudHubArtifactDeployer.shouldValidateApplicationHasStarted(), equalTo(true));

    System.clearProperty(VALIDATE_APPLICATION_STARTED_SYSTEM_PROPERTY);
  }

  @Test
  public void shouldValidateApplicationHasStartedFalseTest() {
    System.setProperty(VALIDATE_APPLICATION_STARTED_SYSTEM_PROPERTY, "false");

    assertThat("Method should have returned false", cloudHubArtifactDeployer.shouldValidateApplicationHasStarted(),
               equalTo(false));

    System.clearProperty(VALIDATE_APPLICATION_STARTED_SYSTEM_PROPERTY);
  }

  @Test
  public void getAttemptsSetTest() {
    System.setProperty(VALIDATE_APPLICATION_STARTED_ATTEMPTS_SYSTEM_PROPERTY, "100");

    assertThat("Method should have returned false", cloudHubArtifactDeployer.getAttempts(),
               equalTo(100));

    System.clearProperty(VALIDATE_APPLICATION_STARTED_ATTEMPTS_SYSTEM_PROPERTY);
  }

  @Test
  public void getAttemptsNotSetTest() {
    assertThat("Method should have returned false", cloudHubArtifactDeployer.getAttempts(),
               equalTo(ATTEMPTS_DEFAULT_VALUE));
  }


  @Test
  public void getSleepTimeSetTest() {
    System.setProperty(VALIDATE_APPLICATION_STARTED_SLEEP_SYSTEM_PROPERTY, "100");

    assertThat("Method did not return the expected value", cloudHubArtifactDeployer.getSleepTime(),
               equalTo(100L));

    System.clearProperty(VALIDATE_APPLICATION_STARTED_SLEEP_SYSTEM_PROPERTY);
  }

  @Test
  public void getSleepTimeNotSetTest() {
    assertThat("Method did not return the expected value", cloudHubArtifactDeployer.getSleepTime(),
               equalTo(DEFAULT_SLEEP_TIME));
  }

  @Test
  public void getClientTest() {
    verify(clientMock, times(0)).init();

    cloudHubArtifactDeployer.getClient();

    verify(clientMock, times(1)).init();

    cloudHubArtifactDeployer.getClient();

    verify(clientMock, times(1)).init();
  }

  public List<Application> getApplications() {
    List<Application> applications =
        range(0, 10).mapToObj(i -> toString()).map(this::createApplication).collect(Collectors.toList());

    return applications;
  }

  private Application createApplication(String name) {
    Application app = new Application();
    app.domain = "app" + name;
    return app;
  }
}

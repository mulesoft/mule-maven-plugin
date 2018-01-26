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
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.verification.cloudhub.CloudHubDeploymentVerification;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.range;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

public class CloudHubArtifactDeployerTest {

  private static final String EXPECTED_STATUS = "status";
  private static final String FAKE_APPLICATION_NAME = "fake-name";

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private File applicationFile;
  private DeployerLog logMock;
  private CloudHubClient clientMock;
  private Application applicationMock;
  private CloudHubDeployment deploymentMock;
  private CloudHubArtifactDeployer cloudHubArtifactDeployerSpy;

  private CloudHubArtifactDeployer cloudHubArtifactDeployer;

  @Before
  public void setUp() throws IOException {
    applicationFile = temporaryFolder.newFile();

    logMock = mock(DeployerLog.class);
    clientMock = mock(CloudHubClient.class);
    applicationMock = mock(Application.class);
    deploymentMock = mock(CloudHubDeployment.class);

    when(deploymentMock.getApplicationName()).thenReturn(FAKE_APPLICATION_NAME);
    when(deploymentMock.getMuleVersion()).thenReturn(Optional.of("4.0.0"));
    when(deploymentMock.getArtifact()).thenReturn(applicationFile);


    when(clientMock.getApplications(FAKE_APPLICATION_NAME)).thenReturn(applicationMock);

    cloudHubArtifactDeployer = new CloudHubArtifactDeployer(deploymentMock, clientMock, logMock);
    cloudHubArtifactDeployerSpy = spy(cloudHubArtifactDeployer);

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
  public void deployApplicationNew() throws DeploymentException {
    when(clientMock.isDomainAvailable(any())).thenReturn(true);

    doNothing().when(cloudHubArtifactDeployerSpy).checkApplicationHasStarted();

    cloudHubArtifactDeployerSpy.deployApplication();


    verify(clientMock).isDomainAvailable(any());
    verify(cloudHubArtifactDeployerSpy).createOrUpdateApplication();
    verify(cloudHubArtifactDeployerSpy).createApplication();
    verify(cloudHubArtifactDeployerSpy).startApplication();
    verify(clientMock).startApplications(FAKE_APPLICATION_NAME);
    verify(cloudHubArtifactDeployerSpy).checkApplicationHasStarted();
  }

  @Test
  public void deployApplicationUpdate() throws DeploymentException {
    when(clientMock.isDomainAvailable(any())).thenReturn(false);

    doNothing().when(cloudHubArtifactDeployerSpy).checkApplicationHasStarted();

    cloudHubArtifactDeployerSpy.deployApplication();

    verify(clientMock).isDomainAvailable(any());
    verify(cloudHubArtifactDeployerSpy).createOrUpdateApplication();
    verify(cloudHubArtifactDeployerSpy).updateApplication();
    verify(cloudHubArtifactDeployerSpy).startApplication();
    verify(clientMock).startApplications(FAKE_APPLICATION_NAME);
    verify(cloudHubArtifactDeployerSpy).checkApplicationHasStarted();
  }

  @Test
  public void deployApplicationVerificationStartedFail() throws DeploymentException {
    expectedException.expect(DeploymentException.class);

    when(clientMock.isDomainAvailable(any())).thenReturn(true);

    CloudHubDeploymentVerification verificationMock = mock(CloudHubDeploymentVerification.class);
    doThrow(DeploymentException.class).when(verificationMock).assertDeployment(deploymentMock);
    cloudHubArtifactDeployer.setDeploymentVerification(verificationMock);

    cloudHubArtifactDeployer.deployApplication();

    verify(clientMock).isDomainAvailable(any());
    verify(cloudHubArtifactDeployerSpy).createOrUpdateApplication();
    verify(cloudHubArtifactDeployerSpy).createApplication();
    verify(cloudHubArtifactDeployerSpy).startApplication();
    verify(clientMock).startApplications(FAKE_APPLICATION_NAME);
    verify(cloudHubArtifactDeployerSpy).checkApplicationHasStarted();
    verify(verificationMock).assertDeployment(eq(deploymentMock));
  }

  @Test
  public void undeployApplicationTest() throws DeploymentException {
    cloudHubArtifactDeployer.undeployApplication();
    verify(clientMock, times(1)).stopApplications(FAKE_APPLICATION_NAME);
  }

  @Test
  public void getApplicationNameTest() {
    assertThat("Application name is not the expected", cloudHubArtifactDeployer.getApplicationName(),
               equalTo(FAKE_APPLICATION_NAME));
  }

  private Application createApplication(String name) {
    Application app = new Application();
    app.setDomain("app" + name);
    return app;
  }
}

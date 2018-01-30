/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.agent;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.client.agent.AgentClient;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.verification.agent.AgentDeploymentVerification;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class AgentArtifactDeployerTest {

  private AgentArtifactDeployer deployer;
  private AgentClient clientMock;
  private AgentDeployment deploymentMock;

  private static final String DOMAIN_NAME = "domain-name";
  private static final String APPLICATION_NAME = "app-name";
  private File applicationFile;
  private AgentArtifactDeployer deployerSpy;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private AgentDeploymentVerification deploymentVerificationMock;

  @Before
  public void setUp() throws IOException, DeploymentException {
    applicationFile = temporaryFolder.newFile();
    clientMock = mock(AgentClient.class);
    deploymentMock = mock(AgentDeployment.class);
    deployer = new AgentArtifactDeployer(deploymentMock, clientMock);
    deployerSpy = spy(deployer);
    deploymentVerificationMock = mock(AgentDeploymentVerification.class);
    doReturn(deploymentVerificationMock).when(deployerSpy).getDeploymentVerification();
    doNothing().when(deploymentVerificationMock).assertDeployment(deploymentMock);
  }

  @Test
  public void deployDomainTest() throws DeploymentException {
    when(deploymentMock.getApplicationName()).thenReturn(DOMAIN_NAME);
    when(deploymentMock.getArtifact()).thenReturn(applicationFile);

    deployer.deployDomain();

    verify(clientMock).deployDomain(DOMAIN_NAME, applicationFile);
  }

  @Test
  public void undeployDomainTest() throws DeploymentException {
    when(deploymentMock.getApplicationName()).thenReturn(DOMAIN_NAME);

    deployer.undeployDomain();

    verify(clientMock).undeployDomain(DOMAIN_NAME);
  }

  @Test
  public void deployApplicationTest() throws DeploymentException {
    when(deploymentMock.getApplicationName()).thenReturn(APPLICATION_NAME);
    when(deploymentMock.getArtifact()).thenReturn(applicationFile);

    deployerSpy.deployApplication();

    verify(clientMock).deployApplication(APPLICATION_NAME, applicationFile);
    verify(deploymentVerificationMock).assertDeployment(deploymentMock);
  }

  @Test
  public void undeployApplicationTest() throws DeploymentException {
    when(deploymentMock.getApplicationName()).thenReturn(APPLICATION_NAME);

    deployer.undeployApplication();

    verify(clientMock).undeployApplication(APPLICATION_NAME);
  }
}

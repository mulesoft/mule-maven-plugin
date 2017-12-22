/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.deployment;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.client.standalone.exception.MuleControllerException;
import org.mule.tools.model.standalone.StandaloneDeployment;
import org.mule.tools.utils.DeployerLog;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class StandaloneDeployerTest {

  private StandaloneDeployer deployerSpy;
  private StandaloneDeployment deployment;

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() throws IOException {
    deployment = new StandaloneDeployment();
    deployerSpy = spy(new StandaloneDeployer(deployment, mock(DeployerLog.class)));
    folder.create();
  }

  @Test
  public void deployArtifactDomainTest() throws DeploymentException {
    deployment.setPackaging("mule-domain");
    doReturn(folder.getRoot()).when(deployerSpy).getApplicationFile();
    doReturn(deployerSpy).when(deployerSpy).deployDomain(eq(folder.getRoot()));

    deployerSpy.deployArtifact();

    verify(deployerSpy, times(0)).deployApplication(eq(folder.getRoot()));
    verify(deployerSpy, times(1)).deployDomain(eq(folder.getRoot()));
  }

  @Test
  public void deployArtifactApplicationTest() throws DeploymentException {
    deployment.setPackaging("mule-application");
    doReturn(folder.getRoot()).when(deployerSpy).getApplicationFile();
    doReturn(deployerSpy).when(deployerSpy).deployApplication(eq(folder.getRoot()));

    deployerSpy.deployArtifact();

    verify(deployerSpy, times(1)).deployApplication(eq(folder.getRoot()));
    verify(deployerSpy, times(0)).deployDomain(eq(folder.getRoot()));
  }

  @Test
  public void deployArtifactDomainExceptionTest() throws DeploymentException {
    expectedException.expect(DeploymentException.class);

    deployment.setPackaging("mule-domain");
    doReturn(folder.getRoot()).when(deployerSpy).getApplicationFile();
    doThrow(MuleControllerException.class).when(deployerSpy).deployDomain(eq(folder.getRoot()));

    deployerSpy.deployArtifact();
  }

  @Test
  public void deployArtifactApplicationExceptionTest() throws DeploymentException {
    expectedException.expect(DeploymentException.class);

    deployment.setPackaging("mule-application");
    doReturn(folder.getRoot()).when(deployerSpy).getApplicationFile();
    doThrow(MuleControllerException.class).when(deployerSpy).deployApplication(eq(folder.getRoot()));

    deployerSpy.deployArtifact();
  }
}

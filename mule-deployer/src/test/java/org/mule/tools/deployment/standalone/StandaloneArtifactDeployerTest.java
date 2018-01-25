/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.standalone;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.client.standalone.controller.MuleProcessController;
import org.mule.tools.client.standalone.controller.probing.Prober;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.standalone.exception.MuleControllerException;
import org.mule.tools.model.standalone.StandaloneDeployment;
import org.mule.tools.utils.DeployerLog;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StandaloneArtifactDeployerTest {

  private static final String ARTIFACT_NAME = "artifact";
  private static final String ARTIFACT_FILENAME = ARTIFACT_NAME + ".jar";
  private static final String MULE_HOME_DIRECTORY = "mule_home";
  private StandaloneArtifactDeployer deployer;
  private StandaloneDeployment deploymentMock;
  private MuleProcessController controllerMock;
  private DeployerLog logMock;
  private Prober proberMock;
  private StandaloneArtifactDeployer deployerSpy;
  private File artifactFile;
  private File muleHome;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setUp() throws DeploymentException, IOException {
    deploymentMock = mock(StandaloneDeployment.class);
    artifactFile = temporaryFolder.newFile(ARTIFACT_FILENAME);
    muleHome = temporaryFolder.newFolder(MULE_HOME_DIRECTORY);
    doReturn(artifactFile).when(deploymentMock).getArtifact();
    doReturn(muleHome).when(deploymentMock).getMuleHome();
    doReturn(ARTIFACT_NAME).when(deploymentMock).getApplicationName();
    controllerMock = mock(MuleProcessController.class);
    logMock = mock(DeployerLog.class);
    proberMock = mock(Prober.class);
    deployer = new StandaloneArtifactDeployer(deploymentMock, controllerMock, logMock, proberMock);
    deployerSpy = spy(deployer);
    doNothing().when(deployerSpy).renameApplicationToApplicationName();
    doNothing().when(deployerSpy).addDomainFromstandaloneDeployment(any());
  }

  @Test
  public void deployDomainTest() {}

  @Test(expected = DeploymentException.class)
  public void undeployDomainTest() throws DeploymentException {
    deployer.undeployDomain();
  }

  @Test(expected = IllegalStateException.class)
  public void deployApplicationNullFileTest() throws DeploymentException {
    doReturn(null).when(deploymentMock).getArtifact();
    deployerSpy.deployApplication();
  }

  @Test
  public void deployApplicationTest() throws DeploymentException {
    deployerSpy.deployApplication();
    verify(controllerMock, times(1)).deploy(artifactFile.getAbsolutePath());
  }

  @Test(expected = DeploymentException.class)
  public void deployApplicationMuleControllerExceptionTest() throws DeploymentException {
    doThrow(new MuleControllerException()).when(controllerMock).deploy(artifactFile.getAbsolutePath());

    deployer.deployApplication();
  }

  @Test(expected = DeploymentException.class)
  public void undeployApplicationNotExistentMuleHomeTest() throws DeploymentException {
    muleHome.delete();
    deployer.undeployApplication();
  }

  @Test
  public void undeployApplicationTest() throws DeploymentException {
    doNothing().when(deployerSpy).undeploy(muleHome);

    deployerSpy.undeployApplication();

    verify(deployerSpy, times(1)).undeploy(muleHome);
  }


  @Test
  public void undeployTest() throws DeploymentException, IOException {
    File appsFolder = new File(muleHome, "apps");
    assertThat("Directory should have been created", appsFolder.mkdir(), is(true));

    File deployedFile = new File(appsFolder, ARTIFACT_FILENAME);
    assertThat("File should have been created", deployedFile.createNewFile(), is(true));

    deployer.undeploy(muleHome);

    assertThat("File should have been deleted", deployedFile.exists(), is(false));
    assertThat("Folder shouldn't be deleted", appsFolder.exists(), is(true));
  }

  @Test(expected = DeploymentException.class)
  public void undeployNotFoundTest() throws DeploymentException, IOException {
    File appsFolder = new File(muleHome, "apps");
    assertThat("Directory should have been created", appsFolder.mkdir(), is(true));

    File deployedFile = new File(appsFolder, ARTIFACT_FILENAME);
    assertThat("File should have been deleted", deployedFile.exists(), is(false));

    deployer.undeploy(muleHome);
  }

  @Test
  public void verifyMuleIsStartedTest() {
    doReturn(true).when(controllerMock).isRunning();
    deployer.verifyMuleIsStarted();
    verify(controllerMock, times(1)).isRunning();
  }

  @Test(expected = MuleControllerException.class)
  public void verifyMuleIsStartedExceptionTest() {
    doReturn(false).when(controllerMock).isRunning();
    deployer.verifyMuleIsStarted();
    verify(controllerMock, times(1)).isRunning();
  }
}

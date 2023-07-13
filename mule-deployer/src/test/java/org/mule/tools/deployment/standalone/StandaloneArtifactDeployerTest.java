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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.client.standalone.controller.MuleProcessController;
import org.mule.tools.client.standalone.controller.probing.Prober;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.standalone.exception.MuleControllerException;
import org.mule.tools.model.standalone.StandaloneDeployment;
import org.mule.tools.utils.DeployerLog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

  @TempDir
  Path temporaryFolder;

  @BeforeEach
  public void setUp() throws DeploymentException, IOException {
    deploymentMock = mock(StandaloneDeployment.class);
    //    temporaryFolder.toPath().resolve("target").resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
    //            .toFile()
    artifactFile = temporaryFolder.resolve((ARTIFACT_FILENAME)).toFile();
    muleHome = Files.createDirectories(temporaryFolder.resolve(MULE_HOME_DIRECTORY)).toFile();
    doReturn(artifactFile).when(deploymentMock).getArtifact();
    doReturn(muleHome).when(deploymentMock).getMuleHome();
    doReturn(ARTIFACT_NAME).when(deploymentMock).getApplicationName();
    controllerMock = mock(MuleProcessController.class);
    logMock = mock(DeployerLog.class);
    proberMock = mock(Prober.class);
    deployer = new StandaloneArtifactDeployer(deploymentMock, controllerMock, logMock, proberMock);
    deployerSpy = spy(deployer);
    doNothing().when(deployerSpy).renameApplicationToApplicationName();
    doNothing().when(deployerSpy).addDomainFromStandaloneDeployment(any());
  }

  @Test
  public void deployDomainTest() {}

  @Test
  public void undeployDomainTest() {
    assertThrows(DeploymentException.class, () -> deployer.undeployDomain());
  }

  @Test
  public void deployApplicationNullFileTest() {
    assertThrows(IllegalStateException.class, () -> {
      doReturn(null).when(deploymentMock).getArtifact();
      deployerSpy.deployApplication();
    });
  }

  @Test
  public void deployApplicationTest() throws DeploymentException {
    deployerSpy.deployApplication();
    verify(controllerMock, times(1)).deploy(artifactFile.getAbsolutePath());
  }

  @Test
  public void deployApplicationMuleControllerExceptionTest() {
    assertThrows(DeploymentException.class, () -> {
      doThrow(new MuleControllerException()).when(controllerMock).deploy(artifactFile.getAbsolutePath());
      deployer.deployApplication();
    });

  }

  @Test
  public void undeployApplicationNotExistentMuleHomeTest() {
    assertThrows(DeploymentException.class, () -> {
      muleHome.delete();
      deployer.undeployApplication();
    });
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
    assertThat(appsFolder.mkdir()).describedAs("Directory should have been created").isTrue();

    File deployedFile = new File(appsFolder, ARTIFACT_FILENAME);
    assertThat(deployedFile.createNewFile()).describedAs("File should have been created").isTrue();

    deployer.undeploy(muleHome);

    assertThat(deployedFile.exists()).describedAs("File should have been deleted").isFalse();
    assertThat(appsFolder.exists()).describedAs("Folder shouldn't be deleted").isTrue();
  }

  @Test
  public void undeployNotFoundTest() {
    assertThrows(DeploymentException.class, () -> {
      File appsFolder = new File(muleHome, "apps");
      assertThat(appsFolder.mkdir()).describedAs("Directory should have been created").isTrue();

      File deployedFile = new File(appsFolder, ARTIFACT_FILENAME);
      assertThat(deployedFile.exists()).describedAs("File should have been deleted").isFalse();

      deployer.undeploy(muleHome);
    });
  }

  @Test
  public void verifyMuleIsStartedTest() {
    doReturn(true).when(controllerMock).isRunning();
    deployer.verifyMuleIsStarted();
    verify(controllerMock, times(1)).isRunning();
  }

  @Test
  public void verifyMuleIsStartedExceptionTest() {
    assertThrows(MuleControllerException.class, () -> {
      doReturn(false).when(controllerMock).isRunning();
      deployer.verifyMuleIsStarted();
      verify(controllerMock, times(1)).isRunning();
    });
  }
}

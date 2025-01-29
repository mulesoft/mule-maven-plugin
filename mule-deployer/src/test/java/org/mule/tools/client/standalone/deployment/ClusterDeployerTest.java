/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.deployment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.client.standalone.configuration.ClusterConfigurator;
import org.mule.tools.client.standalone.controller.MuleProcessController;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.standalone.controller.probing.Probe;
import org.mule.tools.client.standalone.controller.probing.deployment.DeploymentProbe;
import org.mule.tools.model.standalone.ClusterDeployment;
import org.mule.tools.utils.DeployerLog;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class ClusterDeployerTest {

  @TempDir
  public File temporaryFolder;

  private ClusterDeployment clusterDeploymentMock;
  private ClusterDeployer clusterDeployer;
  private DeployerLog logMock;
  private ClusterConfigurator configuratorMock;

  @BeforeEach
  public void setUp() throws DeploymentException, NoSuchFieldException, IllegalAccessException, IOException {
    temporaryFolder.toPath().resolve("conf").toFile().mkdirs();
    temporaryFolder.toPath().resolve("lib").resolve("user").toFile().mkdirs();
    temporaryFolder.toPath().resolve("conf").resolve("wrapper.conf").toFile().createNewFile();
    temporaryFolder.toPath().resolve("mule-app").toFile().createNewFile();
    temporaryFolder.toPath().resolve("mule-invalid-app").toFile().createNewFile();
    temporaryFolder.toPath().resolve("lib.jar").toFile().createNewFile();

    Path muleInvalidAppPath = temporaryFolder.toPath().resolve("mule-invalid-app1");
    Files.createFile(muleInvalidAppPath);
    muleInvalidAppPath.toFile().setWritable(false);
    muleInvalidAppPath.toFile().setReadable(false);

    logMock = mock(DeployerLog.class);
    clusterDeploymentMock = mock(ClusterDeployment.class);
    configuratorMock = mock(ClusterConfigurator.class);

    when(clusterDeploymentMock.getArtifact()).thenReturn(mock(File.class));
    when(clusterDeploymentMock.getDeploymentTimeout()).thenReturn(java.util.Optional.of(60000L));

    clusterDeployer = spy(new ClusterDeployer(clusterDeploymentMock, logMock));
    MuleProcessController mule1 = mock(MuleProcessController.class);
    MuleProcessController mule2 = mock(MuleProcessController.class);

    Field mulesField = ClusterDeployer.class.getDeclaredField("mules");
    mulesField.setAccessible(true);
    mulesField.set(clusterDeployer, Arrays.asList(mule1, mule2));

    Field configuratorField = ClusterDeployer.class.getDeclaredField("configurator");
    configuratorField.setAccessible(true);
    configuratorField.set(clusterDeployer, configuratorMock);

    when(configuratorMock.configureCluster(any(), any())).thenReturn(true);
    when(clusterDeploymentMock.getPackaging()).thenReturn("mule-application");
    when(clusterDeploymentMock.getApplicationName()).thenReturn("othername");
    when(clusterDeploymentMock.getArtifact().getName()).thenReturn("othername");

    when(clusterDeploymentMock.getArtifact().exists()).thenReturn(true);
    when(clusterDeploymentMock.getDeploymentTimeout()).thenReturn(java.util.Optional.of(60000L));

  }

  private static class XDeploymentProbe extends DeploymentProbe {

    @Override
    public Probe isDeployed(MuleProcessController mule, String artifactName) {
      return null;
    }

    @Override
    public Probe notDeployed(MuleProcessController mule, String artifactName) {
      return null;
    }

    public XDeploymentProbe(MuleProcessController mule, String artifactName, Boolean check) {
      super(mule, artifactName, check);
      this.mule = mule;
      this.artifactName = artifactName;
      this.check = check;
    }

    void resetDeploymentProbe() {
      reset(this.mule);
      reset(this.artifactName);
      reset(this.check);
    }
  }


  @Test
  public void clusterDeployerTest() throws DeploymentException {
    ClusterDeployer clusterDeployer = new ClusterDeployer(clusterDeploymentMock, logMock);
    assertNotNull(clusterDeployer);
  }

  @Test
  public void deployWhenArtifactDoesNotExistTest() {
    when(clusterDeploymentMock.getArtifact().exists()).thenReturn(false);

    assertThatThrownBy(() -> clusterDeployer.deploy())
        .isInstanceOf(DeploymentException.class)
        .hasMessageContaining("Application does not exists");
  }

  @Test
  public void initializeInvalidTest() throws DeploymentException {
    when(clusterDeploymentMock.getSize()).thenReturn(10);

    assertThatThrownBy(() -> clusterDeployer.initialize())
        .isInstanceOf(DeploymentException.class)
        .hasMessageContaining("Cannot create cluster with more than 8 nodes");
  }

  @Test
  public void initializeValidTest() throws DeploymentException {
    when(clusterDeploymentMock.getSize()).thenReturn(5);

    clusterDeployer.initialize();
  }

}


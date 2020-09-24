/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo.deploy;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.model.standalone.ClusterDeployment;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Properties;

public class AbstractMuleDeployerMojoTest {

  private AbstractMuleDeployerMojo mojoSpy;
  private MavenProject projectMock;
  private MavenSession sessionMock;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    mojoSpy = spy(AbstractMuleDeployerMojo.class);

    sessionMock = mock(MavenSession.class);
    mojoSpy.setSession(sessionMock);
    when(sessionMock.getGoals()).thenReturn(newArrayList("deploy"));

    Build buildMock = mock(Build.class);
    when(buildMock.getDirectory()).thenReturn("");

    projectMock = mock(MavenProject.class);
    when(projectMock.getBuild()).thenReturn(buildMock);
    when(projectMock.getPackaging()).thenReturn("mule-application");
    when(projectMock.getModel()).thenReturn(mock(Model.class));
    when(projectMock.getGroupId()).thenReturn("groupId");
    when(projectMock.getArtifactId()).thenReturn("artifactId");
    when(projectMock.getDistributionManagementArtifactRepository()).thenReturn(mock(ArtifactRepository.class));
    when(projectMock.getVersion()).thenReturn("1.0.0");
    mojoSpy.setProject(projectMock);

    File projectBaseFolder = new File(".");
    mojoSpy.setProjectBaseFolder(projectBaseFolder);

    mojoSpy.initMojo();
  }

  // In this test, we simulate the behavior of what is inject by plexus when one deployment
  // configuration is defined, i.e., all but one deployment configuration are null.
  // The resolved configuration should be the non-null.
  @Test
  public void setDeploymentOneDeploymentNotNullTest() throws DeploymentException {
    mojoSpy.setAgentDeployment(null);
    mojoSpy.setArmDeployment(null);
    mojoSpy.setCloudHubDeployment(null);
    mojoSpy.setStandaloneDeployment(null);

    ClusterDeployment clusterDeploymentMock = mock(ClusterDeployment.class);
    doNothing().when(clusterDeploymentMock).setDefaultValues(projectMock);
    mojoSpy.setClusterDeployment(clusterDeploymentMock);

    assertThat("The resolved deployment is not the expected", mojoSpy.getDeploymentConfiguration(),
               equalTo(clusterDeploymentMock));
  }

  @Test
  public void setDeploymentOneDeploymentAndDistributionManagementTest() throws DeploymentException {
    mojoSpy.setAgentDeployment(null);
    mojoSpy.setArmDeployment(null);
    mojoSpy.setStandaloneDeployment(null);
    mojoSpy.setClusterDeployment(null);

    CloudHubDeployment cloudHubDeploymentMock = mock(CloudHubDeployment.class);
    doNothing().when(cloudHubDeploymentMock).setDefaultValues(projectMock);
    mojoSpy.setCloudHubDeployment(cloudHubDeploymentMock);

    DistributionManagement distributionManagementMock = mock(DistributionManagement.class);
    DeploymentRepository deploymentRepositoryMock = mock(DeploymentRepository.class);
    Settings settingsMock = mock(Settings.class);
    when(deploymentRepositoryMock.getUrl())
        .thenReturn("https://maven.anypoint.mulesoft.com/api/v1/organizations/f8b03602-3b92-4bac-ada1-1ddd017ef5f4/maven");
    when(distributionManagementMock.getRepository()).thenReturn(deploymentRepositoryMock);
    when(settingsMock.getServer(any())).thenReturn(mock(Server.class));
    when(projectMock.getDistributionManagement()).thenReturn(distributionManagementMock);
    Properties systemProperties = new Properties();
    MavenExecutionRequest mavenExecutionRequestMock = mock(MavenExecutionRequest.class);
    when(mavenExecutionRequestMock.getSystemProperties()).thenReturn(systemProperties);
    when(sessionMock.getRequest()).thenReturn(mavenExecutionRequestMock);
    when(sessionMock.getSettings()).thenReturn(settingsMock);

    assertThat("The resolved deployment is not the expected", mojoSpy.getDeploymentConfiguration(),
               equalTo(cloudHubDeploymentMock));
  }

  @Test
  public void setDeploymentAllDeploymentNullTest() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    expectedException.expectMessage("No deployment configuration was defined. Aborting.");
    mojoSpy.setAgentDeployment(null);
    mojoSpy.setArmDeployment(null);
    mojoSpy.setCloudHubDeployment(null);
    mojoSpy.setStandaloneDeployment(null);
    mojoSpy.setClusterDeployment(null);

    mojoSpy.getDeploymentConfiguration();
  }
}

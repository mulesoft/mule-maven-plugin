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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.model.standalone.ClusterDeployment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

import java.io.File;

public class AbstractMuleDeployerMojoTest {

  private AbstractMuleDeployerMojo mojoSpy;
  private MavenProject mavenProjectMock;


  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    mojoSpy = spy(AbstractMuleDeployerMojo.class);
    mavenProjectMock = mock(MavenProject.class);
  }

  // In this test, we simulate the behavior of what is inject by plexus when one deployment
  // configuration is defined, i.e., all but one deployment configuration are null.
  // The resolved configuration should be the non-null.
  // TODO re enable
  @Ignore
  @Test
  public void setDeploymentOneDeploymentNotNullTest() throws DeploymentException {
    mojoSpy.setAgentDeployment(null);
    mojoSpy.setArmDeployment(null);
    mojoSpy.setCloudHubDeployment(null);
    mojoSpy.setStandaloneDeployment(null);

    ClusterDeployment clusterDeploymentMock = mock(ClusterDeployment.class);
    doNothing().when(clusterDeploymentMock).setDefaultValues(mavenProjectMock);
    mojoSpy.setClusterDeployment(clusterDeploymentMock);

    MavenSession sessionMock = mock(MavenSession.class);
    mojoSpy.setSession(sessionMock);

    Build buildMock = mock(Build.class);
    when(buildMock.getDirectory()).thenReturn("");

    MavenProject projectMock = mock(MavenProject.class);
    when(projectMock.getBuild()).thenReturn(buildMock);
    mojoSpy.setProject(projectMock);

    File projectBaseFolder = new File(".");
    mojoSpy.setProjectBaseFolder(projectBaseFolder);

    mojoSpy.initMojo();
    // mojoSpy.setDeployment();

    assertThat("The resolved deployment is not the expected", mojoSpy.getDeploymentConfiguration(),
               equalTo(clusterDeploymentMock));
  }

  // TODO re enable
  @Ignore
  @Test
  public void setDeploymentAllDeploymentNullTest() throws DeploymentException {
    expectedException.expect(DeploymentException.class);
    expectedException.expectMessage("Please define one deployment configuration");
    mojoSpy.setAgentDeployment(null);
    mojoSpy.setArmDeployment(null);
    mojoSpy.setCloudHubDeployment(null);
    mojoSpy.setStandaloneDeployment(null);
    mojoSpy.setClusterDeployment(null);

    // mojoSpy.setDeployment();
  }
}

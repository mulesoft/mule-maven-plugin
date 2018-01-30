/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.client.core.exception.DeploymentException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

public class DeploymentTest {


  private static final String PACKAGE_FILE_NAME = "package.jar";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private Deployment deploymentSpy;
  private MavenProject project;
  private MavenProject projectMock;


  @Before
  public void setUp() {
    deploymentSpy = spy(Deployment.class);
    project = new MavenProject();
    System.setProperty("mule.artifact", PACKAGE_FILE_NAME);
    projectMock = mock(MavenProject.class);

  }

  @Test
  public void setBasicDeploymentValuesApplicationFileNotSetTest() throws DeploymentException {
    System.clearProperty("mule.artifact");
    expectedException.expect(DeploymentException.class);
    expectedException
        .expectMessage("Artifact to be deployed could not be found. Please set its location setting -Dmule.artifact=path/to/jar or in the deployment configuration pom element");
    deploymentSpy.setBasicDeploymentValues(project);
  }

  @Test
  public void setBasicDeploymentValuesApplicationFileSetSystemPropertiesTest() throws DeploymentException {
    deploymentSpy.setBasicDeploymentValues(project);
    assertThat("The application package jar could not be resolved by system property", deploymentSpy.getArtifact().getPath(),
               equalTo(PACKAGE_FILE_NAME));
  }

  @Test
  public void setBasicDeploymentValuesApplicationFileByMavenProjectTest() throws DeploymentException {
    System.clearProperty("mule.application");
    Artifact artifactMock = mock(Artifact.class);
    when(artifactMock.getFile()).thenReturn(new File(PACKAGE_FILE_NAME));
    List<Artifact> artifacts = new ArrayList<>();
    artifacts.add(artifactMock);
    when(projectMock.getAttachedArtifacts()).thenReturn(artifacts);
    deploymentSpy.setBasicDeploymentValues(project);
    assertThat("The application package jar could not be resolved by maven project", deploymentSpy.getArtifact().getName(),
               equalTo(PACKAGE_FILE_NAME));
  }

  @Test
  public void setBasicDeploymentValuesApplicationNameSetSystemPropertiesTest() throws DeploymentException {
    String applicationName = "package";
    System.setProperty("mule.application.name", applicationName);
    deploymentSpy.setBasicDeploymentValues(project);
    assertThat("The application name could not be resolved by system property", deploymentSpy.getApplicationName(),
               equalTo(applicationName));
    System.clearProperty("mule.application.name");

  }

  @Test
  public void setBasicDeploymentValuesApplicationNameSetByMavenProjectTest() throws DeploymentException {
    String artifactId = "artifact-id";
    when(projectMock.getArtifactId()).thenReturn(artifactId);
    deploymentSpy.setBasicDeploymentValues(projectMock);
    assertThat("The application application name could not be resolved by maven project", deploymentSpy.getApplicationName(),
               equalTo(artifactId));
  }

  @Test
  public void setBasicDeploymentValuesSkipSetSystemPropertiesTest() throws DeploymentException {
    String isSkip = "true";
    System.setProperty("mule.skip", isSkip);
    deploymentSpy.setBasicDeploymentValues(projectMock);
    assertThat("The skip property could not be resolved by system property", deploymentSpy.getSkip(),
               equalTo(isSkip));
    System.clearProperty("mule.skip");

  }

  @Test
  public void setBasicDeploymentValueSkipNotSetTest() throws DeploymentException {
    deploymentSpy.setBasicDeploymentValues(projectMock);
    assertThat("The skip property could not be resolved by default", deploymentSpy.getSkip(),
               equalTo("false"));
  }

  @Test
  public void setBasicDeploymentValuesMuleVersionSetSystemPropertiesTest() throws DeploymentException {
    String muleVersion = "4.0.0";
    System.setProperty("mule.version", muleVersion);
    deploymentSpy.setBasicDeploymentValues(projectMock);
    assertThat("The mule version property could not be resolved by system property", deploymentSpy.getMuleVersion().get(),
               equalTo(muleVersion));
    System.clearProperty("mule.version");

  }

  @Test
  public void setBasicDeploymentValueMuleVersionNotSetTest() throws DeploymentException {
    deploymentSpy.setBasicDeploymentValues(projectMock);
    assertThat("The mule version should not be present", deploymentSpy.getMuleVersion().isPresent(),
               equalTo(false));
  }
}

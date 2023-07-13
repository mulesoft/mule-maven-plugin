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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.core.exception.DeploymentException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class DeploymentTest {


  private static final String PACKAGE_FILE_NAME = "package.jar";
  private Deployment deploymentSpy;
  private MavenProject project;
  private MavenProject projectMock;


  @BeforeEach
  public void setUp() {
    deploymentSpy = spy(Deployment.class);
    project = new MavenProject();
    System.setProperty("mule.artifact", PACKAGE_FILE_NAME);
    projectMock = mock(MavenProject.class);

  }

  @Test
  public void setBasicDeploymentValuesApplicationFileNotSetTest() {
    System.clearProperty("mule.artifact");
    Exception exception = assertThrows(DeploymentException.class, () -> deploymentSpy.setBasicDeploymentValues(project));

    String expectedMessage =
        "Artifact to be deployed could not be found. Please set its location setting -Dmule.artifact=path/to/jar or in the deployment configuration pom element";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void setBasicDeploymentValuesApplicationFileSetSystemPropertiesTest() throws DeploymentException {
    deploymentSpy.setBasicDeploymentValues(project);
    assertThat(deploymentSpy.getArtifact().getPath())
        .describedAs("The application package jar could not be resolved by system property").isEqualTo(PACKAGE_FILE_NAME);
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
    assertThat(deploymentSpy.getArtifact().getName())
        .describedAs("The application package jar could not be resolved by maven project").isEqualTo(PACKAGE_FILE_NAME);
  }

  @Test
  public void setBasicDeploymentValuesApplicationNameSetSystemPropertiesTest() throws DeploymentException {
    String applicationName = "package";
    System.setProperty("mule.application.name", applicationName);
    deploymentSpy.setBasicDeploymentValues(project);
    assertThat(deploymentSpy.getApplicationName()).describedAs("The application name could not be resolved by system property")
        .isEqualTo(applicationName);
    System.clearProperty("mule.application.name");

  }

  @Test
  public void setBasicDeploymentValuesApplicationNameSetByMavenProjectTest() throws DeploymentException {
    String artifactId = "artifact-id";
    when(projectMock.getArtifactId()).thenReturn(artifactId);
    deploymentSpy.setBasicDeploymentValues(projectMock);
    assertThat(deploymentSpy.getApplicationName())
        .describedAs("The application application name could not be resolved by maven project").isEqualTo(artifactId);
  }

  @Test
  public void setBasicDeploymentValuesSkipSetSystemPropertiesTest() throws DeploymentException {
    String isSkip = "true";
    System.setProperty("mule.skip", isSkip);
    deploymentSpy.setBasicDeploymentValues(projectMock);
    assertThat(deploymentSpy.getSkip()).describedAs("The skip property could not be resolved by system property")
        .isEqualTo(isSkip);
    System.clearProperty("mule.skip");

  }

  @Test
  public void setBasicDeploymentValueSkipNotSetTest() throws DeploymentException {
    deploymentSpy.setBasicDeploymentValues(projectMock);
    assertThat(deploymentSpy.getSkip()).describedAs("The skip property could not be resolved by default").isEqualTo("false");
  }

  @Test
  public void setBasicDeploymentValuesMuleVersionSetSystemPropertiesTest() throws DeploymentException {
    String muleVersion = "4.0.0";
    System.setProperty("mule.version", muleVersion);
    deploymentSpy.setBasicDeploymentValues(projectMock);
    assertThat(deploymentSpy.getMuleVersion().get())
        .describedAs("The mule version property could not be resolved by system property").isEqualTo(muleVersion);
    System.clearProperty("mule.version");

  }

  @Test
  public void setBasicDeploymentValueMuleVersionNotSetTest() throws DeploymentException {
    deploymentSpy.setBasicDeploymentValues(projectMock);
    assertThat(deploymentSpy.getMuleVersion().isPresent()).describedAs("The mule version should not be present").isFalse();
  }
}

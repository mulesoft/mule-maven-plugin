/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo.deploy;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mule.tools.api.util.MavenProjectInformation;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.deployment.DefaultDeployer;
import org.mule.tools.model.agent.AgentDeployment;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mule.tools.deployment.AbstractDeployerFactory.MULE_APPLICATION_CLASSIFIER;

class UndeployMojoTest {

  private static MockedStatic<MavenProjectInformation> MAVEN_PROJECT_INFORMATION;
  private static final MavenProjectInformation PROJECT_INFORMATION = mock(MavenProjectInformation.class);

  @TempDir
  public File projectBaseFolder;
  @Mock
  private MavenSession session;
  @Mock
  private MavenProject project;
  @InjectMocks
  private UndeployMojo mojo = new UndeployMojo();
  private AutoCloseable autoCloseable;

  @SuppressWarnings("unchecked")
  @BeforeAll
  static void staticSetup() {
    MAVEN_PROJECT_INFORMATION = mockStatic(MavenProjectInformation.class);

    MAVEN_PROJECT_INFORMATION
        .when(() -> MavenProjectInformation
            .getProjectInformation(nullable(MavenSession.class), nullable(MavenProject.class), nullable(File.class),
                                   nullable(Boolean.class), nullable(List.class), nullable(String.class), nullable(List.class)))
        .thenReturn(PROJECT_INFORMATION);
  }

  @AfterAll
  static void staticTearDown() {
    MAVEN_PROJECT_INFORMATION.close();
  }

  @BeforeEach
  void setUp() throws Exception {
    autoCloseable = MockitoAnnotations.openMocks(this);
    // WHEN
    mojo.setProjectBaseFolder(projectBaseFolder);
    when(session.getGoals()).thenReturn(Collections.singletonList("undeploy"));
    setProject();
  }

  @AfterEach
  void tearDown() throws Exception {
    autoCloseable.close();
  }

  @ParameterizedTest
  @MethodSource("executeTestValue")
  void executeTest(File artifact, Exception exception, Class<?> exceptionClass, String message)
      throws MojoFailureException, MojoExecutionException {
    AgentDeployment deployment = new AgentDeployment();
    deployment.setArtifact(artifact);
    when(PROJECT_INFORMATION.getDeployments()).thenReturn(Collections.singletonList(deployment));
    setProject();

    try (MockedConstruction<DefaultDeployer> constructor = mockConstruction(DefaultDeployer.class, (mock, context) -> {
      if (exception != null) {
        doThrow(exception).when(mock).undeploy();
      } else {
        doNothing().when(mock).undeploy();
      }
    })) {
      if (exception != null) {
        assertThatThrownBy(mojo::execute).isInstanceOf(exceptionClass).hasMessage(message);
      } else {
        mojo.execute();
      }
    }
  }

  static Stream<Arguments> executeTestValue() {
    String path = UUID.randomUUID().toString();
    File artifact = mock(File.class);
    when(artifact.getPath()).thenReturn(path);

    return Stream.of(Arguments.of(artifact, null, null, null),
                     Arguments.of(artifact, new DeploymentException(""), MojoFailureException.class,
                                  "Failed to undeploy [" + path + "]"));
  }

  @Test
  void dummyTest() {
    assertThat("MULE_MAVEN_PLUGIN_UNDEPLOY_PREVIOUS_RUN_PLACEHOLDER").isEqualTo(mojo.getPreviousRunPlaceholder());
  }

  private void setProject() {
    Build build = mock(Build.class);
    reset(project);

    when(build.getDirectory()).thenReturn(projectBaseFolder.getAbsolutePath());
    when(project.getPackaging()).thenReturn(MULE_APPLICATION_CLASSIFIER);
    when(project.getBasedir()).thenReturn(projectBaseFolder);
    when(project.getBuild()).thenReturn(build);
    when(project.getModel()).thenReturn(mock(Model.class));
    when(project.getGroupId()).thenReturn(UUID.randomUUID().toString());
    when(project.getArtifactId()).thenReturn(UUID.randomUUID().toString());
    when(project.getVersion()).thenReturn(UUID.randomUUID().toString());
    when(project.getDependencies()).thenReturn(Collections.emptyList());
  }
}

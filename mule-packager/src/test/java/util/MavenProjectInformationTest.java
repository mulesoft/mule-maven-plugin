/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package util;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.util.MavenProjectInformation.getProjectInformation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.api.util.MavenProjectInformation;

import java.nio.file.Path;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;

class MavenProjectInformationTest {

  private MavenProjectInformation mavenProjectInformation;
  private MavenSession mavenSessionMock;
  private MavenProject mavenProjectMock;
  private Build buildMock;

  @BeforeEach
  void setUp() {
    mavenSessionMock = mock(MavenSession.class);
    mavenProjectMock = mock(MavenProject.class);
    buildMock = mock(Build.class);

    Properties systemProperties = new Properties();
    systemProperties.put("sysPropTest", "false");
    when(mavenSessionMock.getSystemProperties()).thenReturn(systemProperties);
    when(mavenSessionMock.getGoals()).thenReturn(newArrayList("deploy"));

    when(mavenProjectMock.getBuild()).thenReturn(buildMock);
    when(mavenProjectMock.getModel()).thenReturn(mock(Model.class));
    when(mavenProjectMock.getGroupId()).thenReturn("com.plugin");
    when(mavenProjectMock.getArtifactId()).thenReturn("test");
    when(mavenProjectMock.getVersion()).thenReturn("1.0.0");
    when(mavenProjectMock.getPackaging()).thenReturn("jar");
  }

  @Test
  void isDeploySpecifyingGoal(@TempDir Path tempDir) {
    when(buildMock.getDirectory()).thenReturn(tempDir.toAbsolutePath().toString());
    mavenProjectInformation = getProjectInformation(mavenSessionMock, mavenProjectMock, tempDir.toAbsolutePath().toFile(), false,
                                                    newArrayList(), "mule-application");
    assertThat(mavenProjectInformation.isDeployment()).as("The project information is not a deploy goal").isTrue();
  }

  @Test
  void isDeploySpecifyingSystemProperty(@TempDir Path tempDir) {
    when(buildMock.getDirectory()).thenReturn(tempDir.toAbsolutePath().toString());
    Properties systemProperties = new Properties();
    systemProperties.put("muleDeploy", "true");
    when(mavenSessionMock.getSystemProperties()).thenReturn(systemProperties);
    when(mavenSessionMock.getGoals()).thenReturn(newArrayList("verify"));

    mavenProjectInformation = getProjectInformation(mavenSessionMock, mavenProjectMock, tempDir.toAbsolutePath().toFile(), false,
                                                    newArrayList(), "mule-application");
    assertThat(mavenProjectInformation.isDeployment()).as("The project information is not a deploy goal").isTrue();
  }

  @Test
  void isNotDeploy(@TempDir Path tempDir) {
    when(buildMock.getDirectory()).thenReturn(tempDir.toAbsolutePath().toString());
    when(mavenSessionMock.getGoals()).thenReturn(newArrayList("verify"));

    mavenProjectInformation = getProjectInformation(mavenSessionMock, mavenProjectMock, tempDir.toAbsolutePath().toFile(), false,
                                                    newArrayList(), "mule-application");
    assertThat(mavenProjectInformation.isDeployment()).as("The project information is for a deploy goal").isFalse();
  }

}

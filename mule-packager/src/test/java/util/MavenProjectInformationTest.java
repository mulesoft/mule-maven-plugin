/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package util;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.util.MavenProjectInformation.getProjectInformation;

import org.mule.tools.api.util.MavenProjectInformation;

import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MavenProjectInformationTest {

  private MavenProjectInformation mavenProjectInformation;
  private MavenSession mavenSessionMock;
  private MavenProject mavenProjectMock;
  private Build buildMock;

  @Rule
  public TemporaryFolder projectBaseFolder = new TemporaryFolder();

  @Before
  public void setUp() {
    mavenSessionMock = mock(MavenSession.class);
    mavenProjectMock = mock(MavenProject.class);
    buildMock = mock(Build.class);

    Properties systemProperties = new Properties();
    systemProperties.put("sysPropTest", "false");
    when(mavenSessionMock.getSystemProperties()).thenReturn(systemProperties);
    when(mavenSessionMock.getGoals()).thenReturn(newArrayList("deploy"));

    when(buildMock.getDirectory()).thenReturn(projectBaseFolder.toString());

    when(mavenProjectMock.getBuild()).thenReturn(buildMock);
    when(mavenProjectMock.getModel()).thenReturn(mock(Model.class));
    when(mavenProjectMock.getGroupId()).thenReturn("com.plugin");
    when(mavenProjectMock.getArtifactId()).thenReturn("test");
    when(mavenProjectMock.getVersion()).thenReturn("1.0.0");
    when(mavenProjectMock.getPackaging()).thenReturn("jar");
  }

  @Test
  public void isDeploySpecifyingGoal() {
    mavenProjectInformation = getProjectInformation(mavenSessionMock, mavenProjectMock, projectBaseFolder.getRoot(), false,
                                                    newArrayList(), "mule-application");
    assertThat("The project information is not a deploy goal", mavenProjectInformation.isDeployment(), equalTo(true));
  }

  @Test
  public void isDeploySpecifyingSystemProperty() {
    Properties systemProperties = new Properties();
    systemProperties.put("muleDeploy", "true");
    when(mavenSessionMock.getSystemProperties()).thenReturn(systemProperties);
    when(mavenSessionMock.getGoals()).thenReturn(newArrayList("verify"));

    mavenProjectInformation = getProjectInformation(mavenSessionMock, mavenProjectMock, projectBaseFolder.getRoot(), false,
                                                    newArrayList(), "mule-application");
    assertThat("The project information is not a deploy goal", mavenProjectInformation.isDeployment(), equalTo(true));
  }

  @Test
  public void isnotDeploy() {
    when(mavenSessionMock.getGoals()).thenReturn(newArrayList("verify"));

    mavenProjectInformation = getProjectInformation(mavenSessionMock, mavenProjectMock, projectBaseFolder.getRoot(), false,
                                                    newArrayList(), "mule-application");
    assertThat("The project information is for a deploy goal", mavenProjectInformation.isDeployment(), equalTo(false));
  }

}

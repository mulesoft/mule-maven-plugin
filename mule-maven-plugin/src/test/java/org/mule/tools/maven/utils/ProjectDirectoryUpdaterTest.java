/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Reporting;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Mulesoft Inc.
 * @since 3.1.0
 */
public class ProjectDirectoryUpdaterTest {

  private static final String DEFAULT_BUILD_DIRECTORY = "/my/fake/dir";

  private Build buildMock;
  private Model modelMock;
  private Reporting reportingMock;
  private MavenProject mavenProjectMock;

  private ProjectDirectoryUpdater projectDirectoryUpdater;

  @Before
  public void setUp() {
    modelMock = mock(Model.class);
    buildMock = mock(Build.class);
    reportingMock = mock(Reporting.class);
    mavenProjectMock = mock(MavenProject.class);

    when(modelMock.getBuild()).thenReturn(buildMock);

    when(mavenProjectMock.getBuild()).thenReturn(buildMock);
    when(mavenProjectMock.getModel()).thenReturn(modelMock);

    when(modelMock.getReporting()).thenReturn(reportingMock);

    projectDirectoryUpdater = new ProjectDirectoryUpdater(mavenProjectMock);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createNullProject() {
    new ProjectDirectoryUpdater(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void updateBuildOutputDirectoryNullDirectory() {
    projectDirectoryUpdater.updateBuildOutputDirectory(null);
  }

  @Test
  public void updateBuildOutputDirectory() {
    projectDirectoryUpdater.updateBuildOutputDirectory(DEFAULT_BUILD_DIRECTORY);

    verify(mavenProjectMock, times(1)).getModel();
    verify(modelMock, times(1)).getBuild();
    verify(buildMock, times(1)).setOutputDirectory(Paths.get(DEFAULT_BUILD_DIRECTORY, "classes").toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void updateBuildTestOputputDirectoryNullDirectory() {
    projectDirectoryUpdater.updateBuildTestOputputDirectory(null);
  }

  @Test
  public void updateBuildTestOputputDirectory() {
    projectDirectoryUpdater.updateBuildTestOputputDirectory(DEFAULT_BUILD_DIRECTORY);

    verify(mavenProjectMock, times(1)).getModel();
    verify(modelMock, times(1)).getBuild();
    verify(buildMock, times(1)).setTestOutputDirectory(Paths.get(DEFAULT_BUILD_DIRECTORY, "test-classes").toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void updateReportingOutputDirectoryNullDirectory() {
    projectDirectoryUpdater.updateReportingOutputDirectory(null);
  }

  @Test
  public void updateReportingOutputDirectory() {
    projectDirectoryUpdater.updateReportingOutputDirectory(DEFAULT_BUILD_DIRECTORY);

    verify(mavenProjectMock, times(1)).getModel();
    verify(modelMock, times(1)).getReporting();
    verify(reportingMock, times(1)).setOutputDirectory(Paths.get(DEFAULT_BUILD_DIRECTORY, "site").toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void updateBuildDirectoryNullDirectory() {
    projectDirectoryUpdater.updateBuildDirectory(null);
  }

  @Test
  public void updateBuildDirectory() {
    projectDirectoryUpdater.updateBuildDirectory(DEFAULT_BUILD_DIRECTORY);

    verify(mavenProjectMock, times(3)).getModel();
    verify(modelMock, times(2)).getBuild();
    verify(modelMock, times(1)).getReporting();


    verify(buildMock, times(1)).setDirectory(DEFAULT_BUILD_DIRECTORY);
    verify(buildMock, times(1)).setOutputDirectory(Paths.get(DEFAULT_BUILD_DIRECTORY, "classes").toString());
    verify(buildMock, times(1)).setTestOutputDirectory(Paths.get(DEFAULT_BUILD_DIRECTORY, "test-classes").toString());
    verify(reportingMock, times(1)).setOutputDirectory(Paths.get(DEFAULT_BUILD_DIRECTORY, "site").toString());
  }

}

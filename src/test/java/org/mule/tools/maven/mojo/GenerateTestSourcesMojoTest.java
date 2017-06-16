/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mule.tools.api.ContentGenerator;

import java.io.IOException;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

public class GenerateTestSourcesMojoTest extends AbstractMuleMojoTest {

  private Log logMock;
  private GenerateTestSourcesMojo mojoMock;

  @Before
  public void before() throws IOException {
    logMock = mock(Log.class);

    buildMock = mock(Build.class);
    when(buildMock.getDirectory()).thenReturn(projectBaseFolder.getRoot().getAbsolutePath());

    projectMock = mock(MavenProject.class);

    mojoMock = mock(GenerateTestSourcesMojo.class);
    mojoMock.project = projectMock;
    mojoMock.projectBaseFolder = projectBaseFolder.getRoot();

    when(mojoMock.getLog()).thenReturn(logMock);
  }

  @Test
  public void execute() throws MojoFailureException, MojoExecutionException, IOException {
    ContentGenerator contentGeneratorMock = mock(ContentGenerator.class);
    doReturn(contentGeneratorMock).when(mojoMock).getContentGenerator();

    doCallRealMethod().when(mojoMock).execute();
    mojoMock.execute();

    verify(mojoMock, times(1)).getContentGenerator();
    verify(contentGeneratorMock, times(1)).createTestFolderContent();
  }

  @Test(expected = MojoFailureException.class)
  public void executeFailIOException() throws MojoFailureException, MojoExecutionException, IOException {
    ContentGenerator contentGeneratorMock = mock(ContentGenerator.class);
    doReturn(contentGeneratorMock).when(mojoMock).getContentGenerator();

    doThrow(new IOException("")).when(contentGeneratorMock).createTestFolderContent();

    doCallRealMethod().when(mojoMock).execute();
    mojoMock.execute();
  }

  @Test(expected = MojoFailureException.class)
  public void executeFailIllegalArgument() throws MojoFailureException, MojoExecutionException, IOException {
    ContentGenerator contentGeneratorMock = mock(ContentGenerator.class);
    doReturn(contentGeneratorMock).when(mojoMock).getContentGenerator();

    doThrow(new IllegalArgumentException("")).when(contentGeneratorMock).createTestFolderContent();

    doCallRealMethod().when(mojoMock).execute();
    mojoMock.execute();
  }

  @Test
  public void getContentGenerator() {
    when(projectMock.getGroupId()).thenReturn(GROUP_ID);
    when(projectMock.getArtifactId()).thenReturn(ARTIFACT_ID);
    when(projectMock.getVersion()).thenReturn("1.0.0-SNAPSHOT");
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);
    when(projectMock.getBuild()).thenReturn(buildMock);

    doCallRealMethod().when(mojoMock).getContentGenerator();
    mojoMock.getContentGenerator();

    verify(projectMock, times(1)).getGroupId();
    verify(projectMock, times(1)).getArtifactId();
    verify(projectMock, times(1)).getVersion();
    verify(projectMock, times(1)).getPackaging();
    verify(projectMock, times(1)).getBuild();
    verify(buildMock, times(1)).getDirectory();
  }
}

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

import java.io.IOException;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.sources.MuleContentGenerator;

import static org.mockito.Mockito.*;

public class CompileMojoTest extends AbstractMuleMojoTest {

  private static final String VERSION = "1.0.0-SNAPSHOT";
  private Log logMock;
  private CompileMojo mojoMock;

  @Before
  public void before() throws IOException {
    logMock = mock(Log.class);

    buildMock = mock(Build.class);
    when(buildMock.getDirectory()).thenReturn(projectBaseFolder.getRoot().getAbsolutePath());

    projectMock = mock(MavenProject.class);

    mojoMock = mock(CompileMojo.class);
    mojoMock.project = projectMock;
    mojoMock.projectBaseFolder = projectBaseFolder.getRoot();

    when(mojoMock.getLog()).thenReturn(logMock);
  }

  @Test
  public void execute() throws MojoFailureException, MojoExecutionException, IOException {
    MuleContentGenerator contentGeneratorMock = mock(MuleContentGenerator.class);
    doReturn(contentGeneratorMock).when(mojoMock).getContentGenerator();

    doCallRealMethod().when(mojoMock).execute();
    mojoMock.execute();

    verify(mojoMock, times(1)).getContentGenerator();
    verify(contentGeneratorMock, times(1)).createMuleSrcFolderContent();
  }

  @Test(expected = MojoFailureException.class)
  public void executeFailIOException() throws MojoFailureException, MojoExecutionException, IOException {
    MuleContentGenerator contentGeneratorMock = mock(MuleContentGenerator.class);
    doReturn(contentGeneratorMock).when(mojoMock).getContentGenerator();

    doThrow(new IOException("")).when(contentGeneratorMock).createMuleSrcFolderContent();

    doCallRealMethod().when(mojoMock).execute();
    mojoMock.execute();
  }

  @Test(expected = MojoFailureException.class)
  public void executeFailIllegalArgument() throws MojoFailureException, MojoExecutionException, IOException {
    MuleContentGenerator contentGeneratorMock = mock(MuleContentGenerator.class);
    doReturn(contentGeneratorMock).when(mojoMock).getContentGenerator();

    doThrow(new IllegalArgumentException("")).when(contentGeneratorMock).createMuleSrcFolderContent();

    doCallRealMethod().when(mojoMock).execute();
    mojoMock.execute();
  }
}

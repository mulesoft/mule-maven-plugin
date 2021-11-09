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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringInputStream;
import org.junit.Before;
import org.junit.Test;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.tooling.api.AstGenerator;
import org.mule.tooling.api.ConfigurationException;
import org.mule.tools.api.packager.sources.MuleContentGenerator;

import static org.mockito.Mockito.*;

public class CompileMojoTest extends AbstractMuleMojoTest {

  private CompileMojo mojoMock;

  @Before
  public void before() throws IOException {
    logMock = mock(Log.class);

    buildMock = mock(Build.class);
    when(buildMock.getDirectory()).thenReturn(projectBaseFolder.getRoot().getAbsolutePath());

    projectMock = mock(MavenProject.class);
    when(projectMock.getPackaging()).thenReturn("mule-application");
    mojoMock = mock(CompileMojo.class);
    mojoMock.project = projectMock;
    mojoMock.projectBaseFolder = projectBaseFolder.getRoot();

    when(mojoMock.getLog()).thenReturn(logMock);

  }

  @Test
  public void execute()
      throws FileNotFoundException, ConfigurationException, IOException, MojoExecutionException, MojoFailureException {
    MuleContentGenerator contentGeneratorMock = mock(MuleContentGenerator.class);
    InputStream stream = new StringInputStream("");
    doReturn(contentGeneratorMock).when(mojoMock).getContentGenerator();
    ArtifactAst artifactAst = mock(ArtifactAst.class);
    doReturn(artifactAst).when(mojoMock).getArtifactAst();
    doReturn(stream).when(mojoMock).serialize(artifactAst);
    doCallRealMethod().when(mojoMock).execute();
    doCallRealMethod().when(mojoMock).doExecute();
    mojoMock.execute();

    verify(mojoMock, times(2)).getContentGenerator();
    verify(contentGeneratorMock, times(1)).createMuleSrcFolderContent();
    verify(contentGeneratorMock, times(1)).createAstFile(stream);
  }

  @Test(expected = MojoFailureException.class)
  public void executeFailIOException() throws MojoFailureException, MojoExecutionException, IOException {
    MuleContentGenerator contentGeneratorMock = mock(MuleContentGenerator.class);
    doReturn(contentGeneratorMock).when(mojoMock).getContentGenerator();

    doThrow(new IOException("")).when(contentGeneratorMock).createMuleSrcFolderContent();

    doCallRealMethod().when(mojoMock).execute();
    doCallRealMethod().when(mojoMock).doExecute();
    mojoMock.execute();
  }

  @Test(expected = MojoFailureException.class)
  public void executeFailIllegalArgument() throws MojoFailureException, MojoExecutionException, IOException {
    MuleContentGenerator contentGeneratorMock = mock(MuleContentGenerator.class);
    doReturn(contentGeneratorMock).when(mojoMock).getContentGenerator();

    doThrow(new IllegalArgumentException("")).when(contentGeneratorMock).createMuleSrcFolderContent();

    doCallRealMethod().when(mojoMock).execute();
    doCallRealMethod().when(mojoMock).doExecute();
    mojoMock.execute();
  }
}

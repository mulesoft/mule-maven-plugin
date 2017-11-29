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

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

import org.mule.tools.api.packager.MuleProjectFoldersGenerator;

public class InitializeMojoTest extends AbstractMuleMojoTest {

  private InitializeMojo mojoMock;

  @Before
  public void before() throws IOException {
    mojoMock = mock(InitializeMojo.class);
    mojoMock.project = projectMock;

    when(buildMock.getDirectory()).thenReturn(projectBaseFolder.getRoot().getAbsolutePath());
  }

  @Test
  public void execute() throws MojoFailureException, MojoExecutionException, IOException {
    Log logMock = mock(Log.class);
    MuleProjectFoldersGenerator projectFoldersGeneratorMock = mock(MuleProjectFoldersGenerator.class);

    when(mojoMock.getLog()).thenReturn(logMock);
    doReturn(projectFoldersGeneratorMock).when(mojoMock).getProjectFoldersGenerator();

    doCallRealMethod().when(mojoMock).execute();
    doCallRealMethod().when(mojoMock).doExecute();
    mojoMock.execute();

    verify(mojoMock, times(1)).getProjectFoldersGenerator();
    verify(projectFoldersGeneratorMock, times(1)).generate(projectBaseFolder.getRoot().toPath());
  }

}

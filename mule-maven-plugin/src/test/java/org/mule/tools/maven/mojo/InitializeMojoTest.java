/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.api.packager.MuleProjectFoldersGenerator;

class InitializeMojoTest extends AbstractMuleMojoTest {

  private InitializeMojo mojoMock;

  @BeforeEach
  void before() {
    mojoMock = mock(InitializeMojo.class);
    mojoMock.project = projectMock;

    when(buildMock.getDirectory()).thenReturn(projectBaseFolder.toFile().getAbsolutePath());
  }

  @Test
  void execute() throws MojoFailureException, MojoExecutionException {
    Log logMock = mock(Log.class);
    MuleProjectFoldersGenerator projectFoldersGeneratorMock = mock(MuleProjectFoldersGenerator.class);

    when(mojoMock.getLog()).thenReturn(logMock);
    doReturn(projectFoldersGeneratorMock).when(mojoMock).getProjectFoldersGenerator();

    doCallRealMethod().when(mojoMock).execute();
    doCallRealMethod().when(mojoMock).doExecute();
    mojoMock.execute();

    verify(mojoMock, times(1)).getProjectFoldersGenerator();
    verify(projectFoldersGeneratorMock, times(1)).generate(projectBaseFolder);
  }

}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mule.tools.api.packager.MuleProjectFoldersGenerator;
import org.mule.tools.api.packager.ProjectFoldersGeneratorFactory;
import org.mule.tools.api.packager.ProjectInformation;

class InitializeMojoTest extends AbstractMuleMojoTest {

  private InitializeMojo mojoMock;

  private final InitializeMojo mojo = new InitializeMojo() {

    @Override
    protected ProjectInformation getProjectInformation() {
      return mock(ProjectInformation.class);
    }
  };

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

  @Test
  void getProjectFoldersGeneratorTest() {
    try (MockedStatic<ProjectFoldersGeneratorFactory> factory = mockStatic(ProjectFoldersGeneratorFactory.class)) {
      factory.when(() -> ProjectFoldersGeneratorFactory.create(any(ProjectInformation.class)))
          .thenReturn(mock(MuleProjectFoldersGenerator.class));

      assertThat(mojo.getProjectFoldersGenerator()).isNotNull();
      factory.verify(() -> ProjectFoldersGeneratorFactory.create(any(ProjectInformation.class)));
    }
  }

  @Test
  void getPreviousRunPlaceholder() {
    assertThat(mojo.getPreviousRunPlaceholder()).isEqualTo("MULE_MAVEN_PLUGIN_INITIALIZE_PREVIOUS_RUN_PLACEHOLDER");
  }
}

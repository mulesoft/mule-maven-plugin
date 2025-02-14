/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.api.packager.sources.MuleContentGenerator;

class GenerateTestSourcesMojoTest extends AbstractMuleMojoTest {

  private GenerateTestSourcesMojo mojoMock;
  private final GenerateTestSourcesMojo mojo = new GenerateTestSourcesMojo();

  @BeforeEach
  void before() {
    logMock = mock(Log.class);

    buildMock = mock(Build.class);
    when(buildMock.getDirectory()).thenReturn(projectBaseFolder.toFile().getAbsolutePath());

    projectMock = mock(MavenProject.class);

    mojoMock = mock(GenerateTestSourcesMojo.class);
    mojoMock.project = projectMock;
    mojoMock.projectBaseFolder = projectBaseFolder.toFile();

    when(mojoMock.getLog()).thenReturn(logMock);
  }

  @Test
  void execute() throws MojoFailureException, MojoExecutionException, IOException {
    MuleContentGenerator contentGeneratorMock = mock(MuleContentGenerator.class);
    doReturn(contentGeneratorMock).when(mojoMock).getContentGenerator();

    doCallRealMethod().when(mojoMock).execute();
    doCallRealMethod().when(mojoMock).doExecute();
    mojoMock.execute();

    verify(mojoMock, times(1)).getContentGenerator();
    verify(contentGeneratorMock, times(1)).createTestFolderContent();
  }

  @Test
  void executeFailIOException() throws MojoFailureException, MojoExecutionException, IOException {
    MuleContentGenerator contentGeneratorMock = mock(MuleContentGenerator.class);
    doReturn(contentGeneratorMock).when(mojoMock).getContentGenerator();

    doThrow(new IOException("")).when(contentGeneratorMock).createTestFolderContent();

    doCallRealMethod().when(mojoMock).execute();
    doCallRealMethod().when(mojoMock).doExecute();
    assertThatThrownBy(() -> mojoMock.execute()).isExactlyInstanceOf(MojoFailureException.class);
  }

  @Test
  void executeFailIllegalArgument() throws MojoFailureException, MojoExecutionException, IOException {
    MuleContentGenerator contentGeneratorMock = mock(MuleContentGenerator.class);
    doReturn(contentGeneratorMock).when(mojoMock).getContentGenerator();

    doThrow(new IllegalArgumentException("")).when(contentGeneratorMock).createTestFolderContent();

    doCallRealMethod().when(mojoMock).execute();
    doCallRealMethod().when(mojoMock).doExecute();
    assertThatThrownBy(() -> mojoMock.execute()).isExactlyInstanceOf(MojoFailureException.class);
  }

  @Test
  void getPreviousRunPlaceholder() {
    assertThat(mojo.getPreviousRunPlaceholder()).isEqualTo("MULE_MAVEN_PLUGIN_GENERATE_TEST_SOURCES_PREVIOUS_RUN_PLACEHOLDER");
  }
}

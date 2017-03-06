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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tools.artifact.archiver.api.PackagerFolders.META_INF;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;

import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.artifact.archiver.internal.PackageBuilder;
import org.mule.tools.maven.dependency.MulePluginsCompatibilityValidator;
import org.mule.tools.maven.dependency.resolver.MulePluginResolver;

public class AbstractMuleMojoTest {

  protected static final String GROUP_ID = "group-id";
  protected static final String ARTIFACT_ID = "artifact-id";
  protected static final String PACKAGE_NAME = "packageName";
  protected static final String MUNIT_TEST_FILE_NAME = "munit-test.xml";
  protected static final String PROJECT_ARTIFACT_ID = "project-artifact-id";
  protected static final String MULE_APPLICATION_JSON = "mule-application.json";
  protected final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  protected Build buildMock;
  protected File munitFolder;
  protected File metaInfFolder;
  protected File testMuleFolder;
  protected File destinationFile;
  protected File munitSourceFolder;
  protected File muleApplicationJson;
  protected MavenProject projectMock;
  protected File muleSourceFolderMock;
  protected PackageBuilder packageBuilderMock;
  protected MulePluginResolver resolverMock = mock(MulePluginResolver.class);
  protected MulePluginsCompatibilityValidator validatorMock = mock(MulePluginsCompatibilityValidator.class);

  @Rule
  public TemporaryFolder projectRootFolder = new TemporaryFolder();

  @Rule
  public TemporaryFolder buildTemporaryFolder = new TemporaryFolder();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void beforeTest() throws IOException {
    projectRootFolder.create();
    buildTemporaryFolder.create();
    metaInfFolder = buildTemporaryFolder.newFolder(META_INF);
    System.setOut(new PrintStream(outContent));

    projectMock = mock(MavenProject.class);
    buildMock = mock(Build.class);
    packageBuilderMock = mock(PackageBuilder.class);
    muleSourceFolderMock = mock(File.class);

    when(buildMock.getDirectory()).thenReturn(buildTemporaryFolder.getRoot().getAbsolutePath());
    when(projectMock.getBuild()).thenReturn(buildMock);
    when(projectMock.getPackaging()).thenReturn("mule-application");
  }
}

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
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import org.mule.tools.api.packager.builder.MulePackageBuilder;
import org.mule.tools.api.packager.builder.PackageBuilder;

public class AbstractMuleMojoTest {

  protected static final String GROUP_ID = "group-id";
  protected static final String ARTIFACT_ID = "artifact-id";
  protected static final String PACKAGE_NAME = "packageName";
  protected static final String MULE_APPLICATION = "mule-application";
  protected static final String MULE_DOMAIN = "mule-domain";
  protected static final String MULE_APPLICATION_EXAMPLE = "mule-application-example";
  protected final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

  protected Build buildMock;
  protected File metaInfFolder;
  protected File destinationFile;
  protected MavenProject projectMock;
  protected File muleSourceFolderMock;
  protected MulePackageBuilder packageBuilderMock;

  @Rule
  public TemporaryFolder projectBaseFolder = new TemporaryFolder();

  @Rule
  public TemporaryFolder buildFolderFolder = new TemporaryFolder();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void beforeTest() throws IOException {
    metaInfFolder = buildFolderFolder.newFolder(META_INF.value());
    System.setOut(new PrintStream(outContent));

    buildMock = mock(Build.class);
    projectMock = mock(MavenProject.class);
    packageBuilderMock = mock(MulePackageBuilder.class);
    muleSourceFolderMock = mock(File.class);

    when(projectMock.getBuild()).thenReturn(buildMock);
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);

    when(buildMock.getDirectory()).thenReturn(buildFolderFolder.getRoot().getAbsolutePath());
  }
}

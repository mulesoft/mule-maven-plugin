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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Before;
import org.mule.tools.maven.mojo.model.Classifier;
import org.mule.tools.maven.mojo.model.PackagingType;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PackageMojoTest extends AbstractMuleMojoTest {

  private static final String VERSION = "1.0";
  private static final String LIGHT_PACKAGE_CLASSIFIER = "light-package";

  protected PackageMojo mojo = new PackageMojo();

  @Before
  public void before() throws IOException {
    mojo = new PackageMojoImpl();
    mojo.project = projectMock;
    mojo.finalName = PACKAGE_NAME;

    destinationFile = new File(buildTemporaryFolder.getRoot().getAbsolutePath(), PACKAGE_NAME + ".zip");

    when(packageBuilderMock.withDestinationFile(any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withClasses(any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withMule(any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withRepository(any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withMuleArtifact(any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withMaven(any())).thenReturn(packageBuilderMock);
  }

  @Test
  public void createMuleAppOnlyMuleSourcesTest() throws MojoExecutionException {
    mojo.onlyMuleSources = true;

    mojo.createMuleApp(destinationFile, buildTemporaryFolder.getRoot().getAbsolutePath());

    verify(packageBuilderMock, times(1)).withDestinationFile(any());
    verify(packageBuilderMock, times(0)).withClasses(any());
    verify(packageBuilderMock, times(0)).withMule(any());
    verify(packageBuilderMock, times(0)).withRepository(any());
  }

  @Test
  public void createMuleAppWithBinariesTest() throws MojoExecutionException {
    mojo.onlyMuleSources = false;
    mojo.attachMuleSources = false;

    mojo.createMuleApp(destinationFile, buildTemporaryFolder.getRoot().getAbsolutePath());

    verify(packageBuilderMock, times(1)).withDestinationFile(any());
    verify(packageBuilderMock, times(1)).withClasses(any());
    verify(packageBuilderMock, times(1)).withMule(any());
    verify(packageBuilderMock, times(1)).withRepository(any());
    verify(packageBuilderMock, times(1)).withMuleArtifact(any());
    verify(packageBuilderMock, times(1)).withMaven(any());
  }

  @Test
  public void createMuleAppWithBinariesAndSourcesTest() throws MojoExecutionException {
    mojo.onlyMuleSources = false;
    mojo.attachMuleSources = true;

    mojo.createMuleApp(destinationFile, buildTemporaryFolder.getRoot().getAbsolutePath());

    verify(packageBuilderMock, times(1)).withDestinationFile(any());
    verify(packageBuilderMock, times(1)).withClasses(any());
    verify(packageBuilderMock, times(1)).withMule(any());
    verify(packageBuilderMock, times(1)).withRepository(any());
  }

  @Test
  public void getFinalNameTest() {
    when(projectMock.getArtifactId()).thenReturn(ARTIFACT_ID);
    when(projectMock.getVersion()).thenReturn(VERSION);
    mojo.project = projectMock;
    mojo.packagingType = PackagingType.MULE_DOMAIN;

    mojo.finalName = null;
    assertThat("Final name is not the expected", mojo.getFinalName(), equalTo(ARTIFACT_ID + "-" + VERSION + "-" + MULE_DOMAIN));

    mojo.packagingType = PackagingType.MULE_APPLICATION;
    mojo.finalName = null;
    assertThat("Final name is not the expected", mojo.getFinalName(),
               equalTo(ARTIFACT_ID + "-" + VERSION + "-" + MULE_APPLICATION));

    mojo.classifier = Classifier.MULE_APPLICATION_EXAMPLE.toString();
    mojo.finalName = null;
    assertThat("Final name is not the expected", mojo.getFinalName(),
               equalTo(ARTIFACT_ID + "-" + VERSION + "-" + MULE_APPLICATION_EXAMPLE));

    mojo.finalName = null;
    mojo.lightwayPackage = true;
    assertThat("Final name is not the expected", mojo.getFinalName(),
               equalTo(ARTIFACT_ID + "-" + VERSION + "-" + MULE_APPLICATION_EXAMPLE + "-" + LIGHT_PACKAGE_CLASSIFIER));
  }

  private class PackageMojoImpl extends PackageMojo {

    @Override
    public void initializePackageBuilder() {
      this.packageBuilder = packageBuilderMock;

    }
  }
}

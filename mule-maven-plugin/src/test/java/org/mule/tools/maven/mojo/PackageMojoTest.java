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
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.mule.tools.api.packager.builder.PackageBuilder;
import org.mule.tools.api.packager.packaging.Classifier;
import org.mule.tools.api.packager.packaging.PackagingType;

public class PackageMojoTest extends AbstractMuleMojoTest {

  private static final String VERSION = "1.0";
  private static final String LIGHT_PACKAGE_CLASSIFIER = "light-package";
  private static final java.lang.String FINAL_NAME = ARTIFACT_ID + "-" + VERSION;

  @Rule
  public TemporaryFolder targetFolder = new TemporaryFolder();

  protected PackageMojo mojo = new PackageMojo();
  private static final String TYPE = "jar";

  private Build buildMock;


  @Before
  public void before() throws IOException {
    mojo = new PackageMojoImpl();
    mojo.project = projectMock;

    buildMock = mock(Build.class);
    when(projectMock.getBuild()).thenReturn(buildMock);
    when(buildMock.getFinalName()).thenReturn(PACKAGE_NAME);

    destinationFile = new File(buildFolderFolder.getRoot().getAbsolutePath(), PACKAGE_NAME + "." + TYPE);

    when(packageBuilderMock.withDestinationFile(any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withClasses(any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withRepository(any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withMuleArtifact(any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withMaven(any())).thenReturn(packageBuilderMock);
  }

  @Test
  public void getFileNameTest() {
    when(projectMock.getArtifactId()).thenReturn(ARTIFACT_ID);
    when(projectMock.getVersion()).thenReturn(VERSION);

    when(buildMock.getFinalName()).thenReturn(FINAL_NAME);

    mojo.project = projectMock;
    mojo.classifier = Classifier.MULE_APPLICATION.toString();
    mojo.packagingType = PackagingType.MULE_DOMAIN;

    assertThat("Final name is not the expected", mojo.getFileName(),
               equalTo(FINAL_NAME + "-" + MULE_DOMAIN + "." + TYPE));

    mojo.packagingType = PackagingType.MULE_APPLICATION;
    assertThat("Final name is not the expected", mojo.getFileName(),
               equalTo(FINAL_NAME + "-" + MULE_APPLICATION + "." + TYPE));

    mojo.classifier = Classifier.MULE_APPLICATION_EXAMPLE.toString();
    assertThat("Final name is not the expected", mojo.getFileName(),
               equalTo(ARTIFACT_ID + "-" + VERSION + "-" + MULE_APPLICATION_EXAMPLE + "." + TYPE));

    mojo.lightweightPackage = true;
    assertThat("Final name is not the expected", mojo.getFileName(),
               equalTo(ARTIFACT_ID + "-" + VERSION + "-" + MULE_APPLICATION_EXAMPLE + "-" + LIGHT_PACKAGE_CLASSIFIER + "."
                   + TYPE));
  }

  @Test
  public void getDestinationFileTest() throws MojoExecutionException, IOException {
    when(buildMock.getFinalName()).thenReturn(FINAL_NAME);

    mojo.classifier = Classifier.MULE_APPLICATION.toString();
    mojo.packagingType = PackagingType.MULE_APPLICATION;
    File expectedDestinationFile = targetFolder.newFile(FINAL_NAME + "-" + MULE_APPLICATION + "." + TYPE);
    File destinationFile = mojo.getDestinationFile(targetFolder.getRoot().getPath());
    assertThat("Destination file is not the expected", destinationFile.getAbsolutePath(),
               equalTo(expectedDestinationFile.getAbsolutePath()));
  }

  @Test
  public void getDestinationFileNullArgumentTest() throws MojoExecutionException, IOException {
    expectedException.expect(IllegalArgumentException.class);
    mojo.getDestinationFile(null);
  }

  private class PackageMojoImpl extends PackageMojo {

    @Override
    public PackageBuilder getPackageBuilder() {
      return packageBuilderMock;
    }
  }
}

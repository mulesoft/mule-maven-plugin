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
import static org.hamcrest.core.Is.is;
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

  private static final String TYPE = "jar";
  private static final String VERSION = "1.0";
  private static final String LIGHT_PACKAGE_CLASSIFIER = "light-package";
  private static final String FINAL_NAME = ARTIFACT_ID + "-" + VERSION;

  @Rule
  public TemporaryFolder targetFolder = new TemporaryFolder();

  private Build buildMock;
  private PackageMojo mojo;


  @Before
  public void before() throws IOException {
    mojo = new PackageMojoImpl();
    mojo.project = projectMock;

    buildMock = mock(Build.class);
    when(projectMock.getBuild()).thenReturn(buildMock);
    when(buildMock.getFinalName()).thenReturn(FINAL_NAME);

    destinationFile = new File(buildFolderFolder.getRoot().getAbsolutePath(), PACKAGE_NAME + "." + TYPE);

    when(packageBuilderMock.withClasses(any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withRepository(any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withMuleArtifact(any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withMaven(any())).thenReturn(packageBuilderMock);
  }

  @Test
  public void getFileNameMuleDomainTest() {
    String classifier = Classifier.MULE_DOMAIN.toString();
    when(projectMock.getPackaging()).thenReturn(PackagingType.MULE_DOMAIN.toString());

    mojo.classifier = classifier;
    String expectedFileName = FINAL_NAME + "-" + classifier + "." + TYPE;

    assertThat("Final name is not the expected", mojo.getFileName(), equalTo(expectedFileName));
  }

  @Test
  public void getFileNameMuleApplicationTest() {
    String classifier = Classifier.MULE_APPLICATION.toString();
    when(projectMock.getPackaging()).thenReturn(PackagingType.MULE_APPLICATION.toString());

    mojo.classifier = classifier;
    String expectedFileName = FINAL_NAME + "-" + classifier + "." + TYPE;

    assertThat("Final name is not the expected", mojo.getFileName(), equalTo(expectedFileName));
  }

  @Test
  public void getFileNameMuleApplicationExampleTest() {
    String classifier = Classifier.MULE_APPLICATION_EXAMPLE.toString();
    when(projectMock.getPackaging()).thenReturn(PackagingType.MULE_APPLICATION.toString());

    mojo.classifier = classifier;
    String expectedFileName = FINAL_NAME + "-" + classifier + "." + TYPE;

    assertThat("Final name is not the expected", mojo.getFileName(), equalTo(expectedFileName));
  }

  @Test
  public void getFileNameMuleApplicationExampleLighPackageTest() {
    mojo.lightweightPackage = true;
    String classifier = Classifier.MULE_APPLICATION_EXAMPLE.toString();
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);

    mojo.classifier = classifier;
    String expectedFileName = FINAL_NAME + "-" + classifier + "-" + LIGHT_PACKAGE_CLASSIFIER + "." + TYPE;

    assertThat("Final name is not the expected", mojo.getFileName(), equalTo(expectedFileName));
  }

  @Test
  public void getDestinationFileTest() throws MojoExecutionException, IOException {
    mojo.classifier = Classifier.MULE_APPLICATION.toString();
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);

    when(projectMock.getPackaging()).thenReturn(PackagingType.MULE_APPLICATION.toString());
    File expectedDestinationFile = targetFolder.newFile(FINAL_NAME + "-" + MULE_APPLICATION + "." + TYPE);
    File destinationFile = mojo.getDestinationFile(targetFolder.getRoot().getPath());
    assertThat("Destination file is not the expected", destinationFile.getAbsolutePath(),
               equalTo(expectedDestinationFile.getAbsolutePath()));
  }

  @Test
  public void setOnlyMuleSourcesWhenMuleApplicationTemplate() throws MojoExecutionException, IOException {
    mojo = new PackageMojo();
    mojo.classifier = Classifier.MULE_APPLICATION_TEMPLATE.toString();
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);
    mojo.project = projectMock;

    mojo.getPackageBuilder();

    assertThat("Packaging options should be set to only mule sources", mojo.options.isOnlyMuleSources(),
               is(true));
  }

  @Test
  public void getDestinationFileNullArgumentTest() throws MojoExecutionException, IOException {
    expectedException.expect(IllegalArgumentException.class);
    mojo.getDestinationFile(null);
  }

  @Test
  public void buildPackagingOptionsMuleApplicationTemplateTest() {
    mojo.onlyMuleSources = false;
    mojo.classifier = Classifier.MULE_APPLICATION_TEMPLATE.toString();
    assertThat("Packaging options should have onlyMuleSources property set to true",
               mojo.buildPackagingOptions().isOnlyMuleSources(), equalTo(true));
  }

  @Test
  public void buildPackagingOptionsMuleApplicationExampleTest() {
    mojo.attachMuleSources = false;
    mojo.classifier = Classifier.MULE_APPLICATION_EXAMPLE.toString();
    assertThat("Packaging options should have attachMuleSources property set to true",
               mojo.buildPackagingOptions().isAttachMuleSources(), equalTo(true));
  }

  private class PackageMojoImpl extends PackageMojo {

    @Override
    public PackageBuilder getPackageBuilder() {
      return packageBuilderMock;
    }
  }
}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.api.packager.builder.PackageBuilder;
import org.mule.tools.api.packager.packaging.Classifier;
import org.mule.tools.api.packager.packaging.PackagingType;

public class PackageMojoTest extends AbstractMuleMojoTest {

  private static final String TYPE = "jar";
  private static final String VERSION = "1.0";
  private static final String LIGHT_PACKAGE_CLASSIFIER = "light-package";
  private static final String FINAL_NAME = ARTIFACT_ID + "-" + VERSION;

  @TempDir
  public Path targetFolder;

  private PackageMojo mojo;

  @BeforeEach
  void before() {
    mojo = new PackageMojoImpl();
    mojo.project = projectMock;

    Build buildMock = mock(Build.class);
    when(projectMock.getBuild()).thenReturn(buildMock);
    when(buildMock.getFinalName()).thenReturn(FINAL_NAME);

    destinationFile = new File(buildFolderFolder.toFile().getAbsolutePath(), PACKAGE_NAME + "." + TYPE);

    when(packageBuilderMock.withClasses(any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withRepository(any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withMuleArtifact(any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withMaven(any())).thenReturn(packageBuilderMock);
  }

  @Test
  void getFileNameMuleDomainTest() {
    String classifier = Classifier.MULE_DOMAIN.toString();
    when(projectMock.getPackaging()).thenReturn(PackagingType.MULE_DOMAIN.toString());

    mojo.classifier = classifier;
    String expectedFileName = FINAL_NAME + "-" + classifier + "." + TYPE;

    assertThat(mojo.getFileName()).as("Final name is not the expected").isEqualTo(expectedFileName);
  }

  @Test
  void getFileNameMuleApplicationTest() {
    String classifier = Classifier.MULE_APPLICATION.toString();
    when(projectMock.getPackaging()).thenReturn(PackagingType.MULE_APPLICATION.toString());

    mojo.classifier = classifier;
    String expectedFileName = FINAL_NAME + "-" + classifier + "." + TYPE;

    assertThat(mojo.getFileName()).as("Final name is not the expected").isEqualTo(expectedFileName);
  }

  @Test
  void getFileNameMuleApplicationExampleTest() {
    String classifier = Classifier.MULE_APPLICATION_EXAMPLE.toString();
    when(projectMock.getPackaging()).thenReturn(PackagingType.MULE_APPLICATION.toString());

    mojo.classifier = classifier;
    String expectedFileName = FINAL_NAME + "-" + classifier + "." + TYPE;

    assertThat(mojo.getFileName()).as("Final name is not the expected").isEqualTo(expectedFileName);
  }

  @Test
  void getFileNameMuleApplicationExampleLighPackageTest() {
    mojo.lightweightPackage = true;
    String classifier = Classifier.MULE_APPLICATION_EXAMPLE.toString();
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);

    mojo.classifier = classifier;
    String expectedFileName = FINAL_NAME + "-" + classifier + "-" + LIGHT_PACKAGE_CLASSIFIER + "." + TYPE;

    assertThat(mojo.getFileName()).as("Final name is not the expected").isEqualTo(expectedFileName);
  }

  @Test
  void getDestinationFileTest() throws MojoExecutionException {
    mojo.classifier = Classifier.MULE_APPLICATION.toString();
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);

    when(projectMock.getPackaging()).thenReturn(PackagingType.MULE_APPLICATION.toString());
    File expectedDestinationFile = targetFolder.resolve(FINAL_NAME + "-" + MULE_APPLICATION + "." + TYPE).toFile();
    File destinationFile = mojo.getDestinationFile(targetFolder.toString());
    assertThat(destinationFile.getAbsolutePath()).as("Destination file is not the expected")
        .isEqualTo(expectedDestinationFile.getAbsolutePath());
  }

  @Test
  void setOnlyMuleSourcesWhenMuleApplicationTemplate() {
    mojo = new PackageMojo();
    mojo.classifier = Classifier.MULE_APPLICATION_TEMPLATE.toString();
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);
    mojo.project = projectMock;

    mojo.getPackageBuilder();

    assertThat(mojo.options.isOnlyMuleSources()).as("Packaging options should be set to only mule sources").isTrue();
  }

  @Test
  void getDestinationFileNullArgumentTest() {
    assertThatThrownBy(() -> mojo.getDestinationFile(null)).isExactlyInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void buildPackagingOptionsMuleApplicationTemplateTest() {
    mojo.onlyMuleSources = false;
    mojo.classifier = Classifier.MULE_APPLICATION_TEMPLATE.toString();
    assertThat(mojo.buildPackagingOptions().isOnlyMuleSources())
        .as("Packaging options should have onlyMuleSources property set to true").isTrue();
  }

  @Test
  void buildPackagingOptionsMuleApplicationExampleTest() {
    mojo.attachMuleSources = false;
    mojo.classifier = Classifier.MULE_APPLICATION_EXAMPLE.toString();
    assertThat(mojo.buildPackagingOptions().isAttachMuleSources())
        .as("Packaging options should have attachMuleSources property set to true").isTrue();
  }

  private class PackageMojoImpl extends PackageMojo {

    @Override
    public PackageBuilder getPackageBuilder() {
      return packageBuilderMock;
    }
  }
}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.sources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.api.packager.DefaultProjectInformation;
import org.mule.tools.api.packager.packaging.PackagingType;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentGeneratorFactoryTest {

  private DefaultProjectInformation defaultProjectInformation;

  @TempDir
  public Path projectBaseFolder;

  @TempDir
  public Path buidlDirectory;

  @BeforeEach
  public void setUp() throws IOException {
    defaultProjectInformation = mock(DefaultProjectInformation.class);
    when(defaultProjectInformation.getProjectBaseFolder()).thenReturn(projectBaseFolder.toAbsolutePath());
    when(defaultProjectInformation.getBuildDirectory()).thenReturn(buidlDirectory.toAbsolutePath());
  }

  @Test
  public void createDomainBundleContentGeneratorTest() {
    when(defaultProjectInformation.getPackaging()).thenReturn(PackagingType.MULE_DOMAIN_BUNDLE.toString());
    ContentGenerator actualGenerator = ContentGeneratorFactory.create(defaultProjectInformation);
    assertThat(actualGenerator).describedAs("Content generator type is not the expected")
        .isInstanceOf(DomainBundleContentGenerator.class);
  }

  @Test
  public void createMuleContentGeneratorFromMuleApplicationPackagingTypeTest() {
    when(defaultProjectInformation.getPackaging()).thenReturn(PackagingType.MULE_APPLICATION.toString());
    ContentGenerator actualGenerator = ContentGeneratorFactory.create(defaultProjectInformation);
    assertThat(actualGenerator).describedAs("Content generator type is not the expected")
        .isInstanceOf(MuleContentGenerator.class);
  }

  @Test
  public void createMuleContentGeneratorFromMuleDomainPackagingTypeTest() {
    when(defaultProjectInformation.getPackaging()).thenReturn(PackagingType.MULE_DOMAIN.toString());
    ContentGenerator actualGenerator = ContentGeneratorFactory.create(defaultProjectInformation);
    assertThat(actualGenerator).describedAs("Content generator type is not the expected")
        .isInstanceOf(MuleContentGenerator.class);
  }

  @Test
  public void createMuleContentGeneratorFromMulePolicyPackagingTypeTest() {
    when(defaultProjectInformation.getPackaging()).thenReturn(PackagingType.MULE_POLICY.toString());
    ContentGenerator actualGenerator = ContentGeneratorFactory.create(defaultProjectInformation);
    assertThat(actualGenerator).describedAs("Content generator type is not the expected")
        .isInstanceOf(MuleContentGenerator.class);
  }
}

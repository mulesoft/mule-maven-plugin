/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.sources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.api.packager.DefaultProjectInformation;
import org.mule.tools.api.packager.packaging.PackagingType;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentGeneratorFactoryTest {

  private DefaultProjectInformation defaultProjectInformation;

  @Rule
  public TemporaryFolder projectBaseFolder = new TemporaryFolder();

  @Rule
  public TemporaryFolder buidlDirectory = new TemporaryFolder();

  @Before
  public void setUp() throws IOException {
    defaultProjectInformation = mock(DefaultProjectInformation.class);
    projectBaseFolder.create();
    buidlDirectory.create();
    when(defaultProjectInformation.getProjectBaseFolder()).thenReturn(projectBaseFolder.getRoot().toPath());
    when(defaultProjectInformation.getBuildDirectory()).thenReturn(buidlDirectory.getRoot().toPath());
  }

  @Test
  public void createDomainBundleContentGeneratorTest() {
    when(defaultProjectInformation.getPackaging()).thenReturn(PackagingType.MULE_DOMAIN_BUNDLE.toString());
    ContentGenerator actualGenerator = ContentGeneratorFactory.create(defaultProjectInformation);
    assertThat("Content generator type is not the expected", actualGenerator, instanceOf(DomainBundleContentGenerator.class));
  }

  @Test
  public void createMuleContentGeneratorFromMuleApplicationPackagingTypeTest() {
    when(defaultProjectInformation.getPackaging()).thenReturn(PackagingType.MULE_APPLICATION.toString());
    ContentGenerator actualGenerator = ContentGeneratorFactory.create(defaultProjectInformation);
    assertThat("Content generator type is not the expected", actualGenerator, instanceOf(MuleContentGenerator.class));
  }

  @Test
  public void createMuleContentGeneratorFromMuleDomainPackagingTypeTest() {
    when(defaultProjectInformation.getPackaging()).thenReturn(PackagingType.MULE_DOMAIN.toString());
    ContentGenerator actualGenerator = ContentGeneratorFactory.create(defaultProjectInformation);
    assertThat("Content generator type is not the expected", actualGenerator, instanceOf(MuleContentGenerator.class));
  }

  @Test
  public void createMuleContentGeneratorFromMulePolicyPackagingTypeTest() {
    when(defaultProjectInformation.getPackaging()).thenReturn(PackagingType.MULE_POLICY.toString());
    ContentGenerator actualGenerator = ContentGeneratorFactory.create(defaultProjectInformation);
    assertThat("Content generator type is not the expected", actualGenerator, instanceOf(MuleContentGenerator.class));
  }
}

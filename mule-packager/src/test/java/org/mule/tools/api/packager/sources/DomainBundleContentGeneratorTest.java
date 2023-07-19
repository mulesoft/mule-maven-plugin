/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.packager.sources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.api.packager.DefaultProjectInformation;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class DomainBundleContentGeneratorTest {

  private DomainBundleContentGenerator generator;

  @Rule
  public TemporaryFolder projectBaseFolder = new TemporaryFolder();

  @Rule
  public TemporaryFolder buidlDirectory = new TemporaryFolder();
  private DefaultProjectInformation defaultProjectInformation;

  @Before
  public void setUp() throws IOException {
    defaultProjectInformation = mock(DefaultProjectInformation.class);
    projectBaseFolder.create();
    buidlDirectory.create();
    when(defaultProjectInformation.getProjectBaseFolder()).thenReturn(projectBaseFolder.getRoot().toPath());
    when(defaultProjectInformation.getBuildDirectory()).thenReturn(buidlDirectory.getRoot().toPath());
    generator = new DomainBundleContentGenerator(defaultProjectInformation);
  }

  @Test
  public void createContentTest() throws IOException {
    DomainBundleContentGenerator generatorSpy = spy(generator);
    doNothing().when(generatorSpy).createMavenDescriptors();
    generatorSpy.createContent();
    verify(generatorSpy, times(1)).createMavenDescriptors();
  }
}

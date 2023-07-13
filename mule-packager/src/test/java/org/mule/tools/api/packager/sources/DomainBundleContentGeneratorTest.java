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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.api.packager.DefaultProjectInformation;

import java.io.IOException;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

public class DomainBundleContentGeneratorTest {

  private DomainBundleContentGenerator generator;

  @TempDir
  public Path projectBaseFolder;
  @TempDir
  public Path buidlDirectory;

  private DefaultProjectInformation defaultProjectInformation;

  @BeforeEach
  public void setUp() throws IOException {
    defaultProjectInformation = mock(DefaultProjectInformation.class);
    when(defaultProjectInformation.getProjectBaseFolder()).thenReturn(projectBaseFolder.toAbsolutePath());
    when(defaultProjectInformation.getBuildDirectory()).thenReturn(buidlDirectory.toAbsolutePath());
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

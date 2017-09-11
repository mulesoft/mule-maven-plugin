/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.resources.processor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.packager.resources.content.ResourcesContent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DomainBundleProjectResourcesContentProcessorTest {

  @Rule
  public TemporaryFolder targetFolder = new TemporaryFolder();
  private File domainFolder;
  private File applicationsFolder;
  private DomainBundleProjectResourcesContentProcessor contentProcessor;

  @Before
  public void setUp() throws IOException {
    targetFolder.create();
    domainFolder = targetFolder.newFolder("domain");
    applicationsFolder = targetFolder.newFolder("applications");
    contentProcessor = new DomainBundleProjectResourcesContentProcessor(targetFolder.getRoot().toPath());
  }

  @Test
  public void processTest() throws IOException {
    DomainBundleProjectResourcesContentProcessor contentProcessorSpy = spy(contentProcessor);
    doNothing().when(contentProcessorSpy).copyAsDomainOrApplication(any());

    ResourcesContent resourcesMock = mock(ResourcesContent.class);

    List<Artifact> artifactMockList = new ArrayList<>();

    artifactMockList.add(mock(Artifact.class));
    artifactMockList.add(mock(Artifact.class));
    artifactMockList.add(mock(Artifact.class));

    when(resourcesMock.getResources()).thenReturn(artifactMockList);

    contentProcessorSpy.process(resourcesMock);

    verify(contentProcessorSpy, times(3)).copyAsDomainOrApplication(any());
  }

}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.resources.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.packager.resources.content.ResourcesContent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DomainBundleProjectResourcesContentProcessorTest {

  @TempDir
  public Path targetFolder;
  private DomainBundleProjectResourcesContentProcessor contentProcessor;

  @BeforeEach
  public void setUp() throws IOException {
    contentProcessor = new DomainBundleProjectResourcesContentProcessor(targetFolder.toAbsolutePath());
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

  @ParameterizedTest
  @ValueSource(strings = {"mule-domain", "mule-application"})
  public void copyAsDomainOrApplicationTest(String packagingType) throws IOException {
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates("org.mule.tools.maven", "mule-classloader-model", "4.1.0");
    targetFolder.resolve("applications").toFile().mkdirs();
    targetFolder.resolve("domain").toFile().mkdirs();
    artifactCoordinates.setClassifier(packagingType);
    Artifact artifact = new Artifact(artifactCoordinates, targetFolder.toUri());
    contentProcessor.copyAsDomainOrApplication(artifact);
  }

}

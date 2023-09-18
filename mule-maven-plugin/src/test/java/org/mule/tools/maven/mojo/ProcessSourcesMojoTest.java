/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.packager.resources.processor.DomainBundleProjectResourcesContentProcessor;
import org.mule.tools.api.packager.resources.processor.ResourcesContentProcessor;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Mulesoft Inc.
 * @since 3.1.0
 */
class ProcessSourcesMojoTest extends AbstractMuleMojoTest {

  private static final String PREVIOUS_RUN_PLACEHOLDER = "MULE_MAVEN_PLUGIN_PROCESS_RESOURCES_PREVIOUS_RUN_PLACEHOLDER";

  private ProcessResourcesMojo mojo;
  private ProcessResourcesMojo mojoMock;

  @BeforeEach
  void setUp() {
    mojo = new ProcessResourcesMojo();
    mojoMock = mock(ProcessResourcesMojo.class);
  }

  @Test
  void getResourcesContentProcessorDomainBundle() {
    when(projectMock.getPackaging()).thenReturn(PackagingType.MULE_DOMAIN_BUNDLE.toString());
    prepareMojoForProjectInformation(mojo, GROUP_ID, ARTIFACT_ID, VERSION);

    Optional<ResourcesContentProcessor> resourcesContentProcessor = mojo.getResourcesContentProcessor();

    assertThat(resourcesContentProcessor).isPresent();
    assertThat(resourcesContentProcessor.get()).isInstanceOf(DomainBundleProjectResourcesContentProcessor.class);
  }

  @Test
  void getResourcesContentProcessor() {
    prepareMojoForProjectInformation(mojo, GROUP_ID, ARTIFACT_ID, VERSION);

    Optional<ResourcesContentProcessor> resourcesContentProcessor = mojo.getResourcesContentProcessor();

    assertThat(resourcesContentProcessor).isNotPresent();
  }

  @Test
  void doExecuteEmptyResourcesContentProcessor() throws MojoFailureException {
    when(mojoMock.getResourcesContentProcessor()).thenReturn(Optional.empty());

    doCallRealMethod().when(mojoMock).doExecute();
    mojoMock.doExecute();

    verify(mojoMock, times(1)).getResourcesContentProcessor();
  }

  @Test
  void doExecute() throws MojoFailureException, IOException {
    ResourcesContentProcessor resourcesContentProcessorMock = mock(ResourcesContentProcessor.class);
    when(mojoMock.getResourcesContentProcessor()).thenReturn(Optional.of(resourcesContentProcessorMock));

    doCallRealMethod().when(mojoMock).doExecute();
    mojoMock.doExecute();

    verify(mojoMock, times(1)).getResourcesContentProcessor();
    verify(resourcesContentProcessorMock, times(1)).process(any());
  }

  @Test
  void doExecuteFailProcessIllegalArgumentException() throws MojoFailureException, IOException {
    ResourcesContentProcessor resourcesContentProcessorMock = mock(ResourcesContentProcessor.class);
    doThrow(new IllegalArgumentException()).when(resourcesContentProcessorMock).process(any());
    when(mojoMock.getResourcesContentProcessor()).thenReturn(Optional.of(resourcesContentProcessorMock));

    doCallRealMethod().when(mojoMock).doExecute();
    assertThatThrownBy(() -> mojoMock.doExecute()).isExactlyInstanceOf(MojoFailureException.class);

    verify(mojoMock, times(1)).getResourcesContentProcessor();
  }

  @Test
  void doExecuteFailProcessIOException() throws MojoFailureException, IOException {
    ResourcesContentProcessor resourcesContentProcessorMock = mock(ResourcesContentProcessor.class);
    doThrow(new IOException()).when(resourcesContentProcessorMock).process(any());
    when(mojoMock.getResourcesContentProcessor()).thenReturn(Optional.of(resourcesContentProcessorMock));

    doCallRealMethod().when(mojoMock).doExecute();
    assertThatThrownBy(() -> mojoMock.doExecute()).isExactlyInstanceOf(MojoFailureException.class);

    verify(mojoMock, times(1)).getResourcesContentProcessor();
  }

  @Test
  void getPreviousRunPlaceholder() {
    assertThat(mojo.getPreviousRunPlaceholder()).isEqualTo(PREVIOUS_RUN_PLACEHOLDER);
  }

}

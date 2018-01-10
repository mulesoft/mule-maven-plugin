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
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Test;

import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.packager.resources.processor.DomainBundleProjectResourcesContentProcessor;
import org.mule.tools.api.packager.resources.processor.ResourcesContentProcessor;

/**
 * @author Mulesoft Inc.
 * @since 3.1.0
 */
public class ProcessSourcesMojoTest extends AbstractMuleMojoTest {

  private static final String PREVIOUS_RUN_PLACEHOLDER = "MULE_MAVEN_PLUGIN_PROCESS_RESOURCES_PREVIOUS_RUN_PLACEHOLDER";

  private ProcessResourcesMojo mojo;
  private ProcessResourcesMojo mojoMock;

  @Before
  public void setUp() {
    mojo = new ProcessResourcesMojo();
    mojoMock = mock(ProcessResourcesMojo.class);
  }

  @Test
  public void getResourcesContentProcessorDomainBundle() {
    when(projectMock.getPackaging()).thenReturn(PackagingType.MULE_DOMAIN_BUNDLE.toString());
    prepareMojoForProjectInformation(mojo, GROUP_ID, ARTIFACT_ID, VERSION);

    Optional<ResourcesContentProcessor> resourcesContentProcessor = mojo.getResourcesContentProcessor();

    assertThat(resourcesContentProcessor.isPresent(), is(true));
    assertThat(resourcesContentProcessor.get(), instanceOf(DomainBundleProjectResourcesContentProcessor.class));
  }

  @Test
  public void getResourcesContentProcessor() {
    prepareMojoForProjectInformation(mojo, GROUP_ID, ARTIFACT_ID, VERSION);

    Optional<ResourcesContentProcessor> resourcesContentProcessor = mojo.getResourcesContentProcessor();

    assertThat(resourcesContentProcessor.isPresent(), is(false));
  }

  @Test
  public void doExecuteEmptyResourcesContentProcessor() throws MojoFailureException, MojoExecutionException {
    when(mojoMock.getResourcesContentProcessor()).thenReturn(Optional.empty());

    doCallRealMethod().when(mojoMock).doExecute();
    mojoMock.doExecute();

    verify(mojoMock, times(1)).getResourcesContentProcessor();
  }

  @Test
  public void doExecute() throws MojoFailureException, MojoExecutionException, IOException {
    ResourcesContentProcessor resourcesContentProcessorMock = mock(ResourcesContentProcessor.class);
    when(mojoMock.getResourcesContentProcessor()).thenReturn(Optional.of(resourcesContentProcessorMock));

    doCallRealMethod().when(mojoMock).doExecute();
    mojoMock.doExecute();

    verify(mojoMock, times(1)).getResourcesContentProcessor();
    verify(resourcesContentProcessorMock, times(1)).process(any());
  }

  @Test(expected = MojoFailureException.class)
  public void doExecuteFailProcessIllegalArgumentException() throws MojoFailureException, MojoExecutionException, IOException {
    ResourcesContentProcessor resourcesContentProcessorMock = mock(ResourcesContentProcessor.class);
    doThrow(new IllegalArgumentException()).when(resourcesContentProcessorMock).process(any());
    when(mojoMock.getResourcesContentProcessor()).thenReturn(Optional.of(resourcesContentProcessorMock));

    doCallRealMethod().when(mojoMock).doExecute();
    mojoMock.doExecute();

    verify(mojoMock, times(1)).getResourcesContentProcessor();
  }

  @Test(expected = MojoFailureException.class)
  public void doExecuteFailProcessIOException() throws MojoFailureException, MojoExecutionException, IOException {
    ResourcesContentProcessor resourcesContentProcessorMock = mock(ResourcesContentProcessor.class);
    doThrow(new IOException()).when(resourcesContentProcessorMock).process(any());
    when(mojoMock.getResourcesContentProcessor()).thenReturn(Optional.of(resourcesContentProcessorMock));

    doCallRealMethod().when(mojoMock).doExecute();
    mojoMock.doExecute();

    verify(mojoMock, times(1)).getResourcesContentProcessor();
  }

  @Test
  public void getPreviousRunPlaceholder() {
    assertThat(mojo.getPreviousRunPlaceholder(), is(PREVIOUS_RUN_PLACEHOLDER));
  }

}

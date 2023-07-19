/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.packager;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectFoldersGeneratorFactoryTest {

  @Test
  public void createDomainBundleProjectFoldersGeneratorTest() {
    DefaultProjectInformation defaultProjectInformation = mock(DefaultProjectInformation.class);
    when(defaultProjectInformation.getGroupId()).thenReturn("group.id");
    when(defaultProjectInformation.getArtifactId()).thenReturn("artifact-id");
    when(defaultProjectInformation.getPackaging()).thenReturn("mule-application");
    assertThat("The project folder generator type is not the expected",
               ProjectFoldersGeneratorFactory.create(defaultProjectInformation),
               instanceOf(MuleProjectFoldersGenerator.class));
  }
}

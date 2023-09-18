/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectFoldersGeneratorFactoryTest {

  @Test
  public void createDomainBundleProjectFoldersGeneratorTest() {
    DefaultProjectInformation defaultProjectInformation = mock(DefaultProjectInformation.class);
    when(defaultProjectInformation.getGroupId()).thenReturn("group.id");
    when(defaultProjectInformation.getArtifactId()).thenReturn("artifact-id");
    when(defaultProjectInformation.getPackaging()).thenReturn("mule-application");
    assertThat(ProjectFoldersGeneratorFactory.create(defaultProjectInformation))
        .describedAs("The project folder generator type is not the expected").isInstanceOf(MuleProjectFoldersGenerator.class);
  }
}

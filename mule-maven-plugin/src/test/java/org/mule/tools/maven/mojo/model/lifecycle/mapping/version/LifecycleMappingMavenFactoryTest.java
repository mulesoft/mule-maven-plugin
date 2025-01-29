/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo.model.lifecycle.mapping.version;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.project.ProjectLifecycleMapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class LifecycleMappingMavenFactoryTest {

  @Test
  void buildLifecycleMappingMavenTest() {
    ProjectLifecycleMapping mapping = mock(ProjectLifecycleMapping.class);

    assertThat(LifecycleMappingMavenFactory.buildLifecycleMappingMaven(mapping))
        .isInstanceOf(LifecycleMappingMaven339OrHigher.class);

    try (MockedStatic<LifecycleMappingMavenFactory> mockStatic = mockStatic(LifecycleMappingMavenFactory.class)) {
      mockStatic.when(LifecycleMappingMavenFactory::loadClass).thenThrow(new ClassNotFoundException());
      mockStatic.when(() -> LifecycleMappingMavenFactory.buildLifecycleMappingMaven(mapping)).thenCallRealMethod();

      assertThat(LifecycleMappingMavenFactory.buildLifecycleMappingMaven(mapping)).isInstanceOf(LifecycleMappingMaven333.class);
    }
  }
}

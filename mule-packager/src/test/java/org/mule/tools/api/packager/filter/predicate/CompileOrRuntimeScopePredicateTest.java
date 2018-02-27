/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.filter.predicate;

import org.junit.Before;
import org.junit.Test;
import org.mule.tools.api.util.Artifact;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompileOrRuntimeScopePredicateTest {

  private ArtifactPredicate predicate = new CompileOrRuntimeScopePredicate();

  @Test
  public void compileScopeTest() {
    Artifact artifactCompileMock = mock(Artifact.class);
    when(artifactCompileMock.isCompileScope()).thenReturn(true);
    when(artifactCompileMock.isRuntimeScope()).thenReturn(false);
    assertThat("Predicate should have returned true", predicate.test(artifactCompileMock));
  }

  @Test
  public void runtimeScopeTest() {
    Artifact artifactRuntimeMock = mock(Artifact.class);
    when(artifactRuntimeMock.isCompileScope()).thenReturn(false);
    when(artifactRuntimeMock.isRuntimeScope()).thenReturn(true);
    assertThat("Predicate should have returned true", predicate.test(artifactRuntimeMock));
  }

  @Test
  public void notCompileNorRuntimeScopeTest() {
    Artifact notCompileNorRuntimeArtifactMock = mock(Artifact.class);
    when(notCompileNorRuntimeArtifactMock.isRuntimeScope()).thenReturn(false);
    when(notCompileNorRuntimeArtifactMock.isCompileScope()).thenReturn(false);
    assertThat("Predicate should have returned false", !predicate.test(notCompileNorRuntimeArtifactMock));
  }
}

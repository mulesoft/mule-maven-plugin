/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.muleclassloader.model.resolver;

import static java.util.Collections.emptyList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.muleclassloader.model.resolver.ApplicationDependencyResolver.MULE_DOMAIN_CLASSIFIER;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mule.maven.client.api.MavenClient;
import org.mule.maven.client.internal.MuleMavenClient;
import org.mule.maven.pom.parser.api.model.BundleDependency;
import org.mule.maven.pom.parser.api.model.BundleScope;

class ApplicationDependencyResolverTest {

  private final MavenClient mockMavenClient = mock(MuleMavenClient.class);

  @Test
  void emptyDependencies() {
    when(mockMavenClient.resolveArtifactDependencies(any(), anyBoolean(), anyBoolean(), any(), any(), any()))
        .thenReturn(emptyList());
    ApplicationDependencyResolver applicationDependencyResolver = new ApplicationDependencyResolver(mockMavenClient);
    List<BundleDependency> bundleDependencies =
        applicationDependencyResolver.resolveApplicationDependencies(new File("not-relevant"));
    assertThat(bundleDependencies).isEmpty();
  }

  @Test
  void filterProvidedDependencies() {
    BundleDependency mockProvidedDependency = mock(BundleDependency.class, RETURNS_DEEP_STUBS);
    BundleDependency mockNonProvidedDependency = mock(BundleDependency.class, RETURNS_DEEP_STUBS);
    BundleDependency mockDomainDependency = mock(BundleDependency.class, RETURNS_DEEP_STUBS);

    when(mockProvidedDependency.getScope()).thenReturn(BundleScope.PROVIDED);
    when(mockNonProvidedDependency.getScope()).thenReturn(BundleScope.COMPILE);
    when(mockDomainDependency.getScope()).thenReturn(BundleScope.COMPILE);
    when(mockDomainDependency.getDescriptor().getClassifier()).thenReturn(Optional.of(MULE_DOMAIN_CLASSIFIER));

    when(mockMavenClient.resolveArtifactDependencies(any(), anyBoolean(), anyBoolean(), any(), any(), any()))
        .thenReturn(ImmutableList.of());
    ApplicationDependencyResolver applicationDependencyResolver = new ApplicationDependencyResolver(mockMavenClient);
    List<BundleDependency> bundleDependencies =
        applicationDependencyResolver.resolveApplicationDependencies(new File("not-relevant"));
    assertThat(bundleDependencies).isEmpty();
  }
}

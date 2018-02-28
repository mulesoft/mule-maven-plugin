/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.filter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mule.tools.api.packager.filter.predicate.AbstractArtifactPrefixPredicate;
import org.mule.tools.api.packager.filter.predicate.ArtifactPredicate;
import org.mule.tools.api.packager.filter.predicate.CompileOrRuntimeScopePredicate;
import org.mule.tools.api.packager.packaging.Exclusion;
import org.mule.tools.api.packager.packaging.Inclusion;
import org.mule.tools.api.util.Artifact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

public class DependenciesFilterTest {

  private DependenciesFilter filter;
  private Set<Artifact> projectArtifactsMock;
  private List<? extends Inclusion> inclusions;
  private List<? extends Exclusion> exclusions;
  private boolean excludeMuleArtifacts;
  private DependenciesFilter filterSpy;
  private List<String> filters;

  @Before
  public void setUp() throws Exception {
    projectArtifactsMock = mock(Set.class);
    inclusions = mock(List.class);
    exclusions = mock(List.class);
    excludeMuleArtifacts = true;
    filter = new DependenciesFilter(projectArtifactsMock, inclusions, exclusions, excludeMuleArtifacts);
    filterSpy = spy(filter);
    filterSpy.artifactFilter = mock(ArtifactFilter.class);
    filters = new ArrayList<>();
    doReturn(filters).when(filterSpy).getAsFilters(anyList());
    doReturn(projectArtifactsMock).when(filterSpy.artifactFilter).filter(anySet(), anySet(), Matchers.<ArtifactPredicate>any());
  }

  @Test
  public void getArtifactsToArchive() throws Exception {
    filterSpy.getArtifactsToArchive();

    verify(filterSpy).resolveNotCompileNorRuntimeArtifacts(anySet());
    verify(filterSpy).removeDependenciesWithMuleGroupId(anySet());
    verify(filterSpy).applyIncludes(anySet());
    verify(filterSpy).applyExclusions(anySet());
  }

  @Test
  public void getArtifactsToArchiveDontExcludeMuleDependencies() throws Exception {
    filterSpy = spy(new DependenciesFilter(projectArtifactsMock, inclusions, exclusions, false));
    filterSpy.artifactFilter = mock(ArtifactFilter.class);
    doReturn(filters).when(filterSpy).getAsFilters(anyList());
    doReturn(projectArtifactsMock).when(filterSpy.artifactFilter).filter(anySet(), anySet(), Matchers.<ArtifactPredicate>any());

    filterSpy.getArtifactsToArchive();

    verify(filterSpy).resolveNotCompileNorRuntimeArtifacts(anySet());
    verify(filterSpy, never()).removeDependenciesWithMuleGroupId(anySet());
    verify(filterSpy).applyIncludes(anySet());
    verify(filterSpy).applyExclusions(anySet());
  }

  @Test
  public void resolveNotCompileNorRuntimeArtifacts() throws Exception {
    filterSpy.resolveNotCompileNorRuntimeArtifacts(projectArtifactsMock);
    verify(filterSpy.artifactFilter).filter(projectArtifactsMock, eq(anySet()), any(CompileOrRuntimeScopePredicate.class));
  }

  @Test
  public void removeDependenciesWithMuleGroupId() throws Exception {
    filterSpy.removeDependenciesWithMuleGroupId(projectArtifactsMock);
    verify(filterSpy.artifactFilter).filter(projectArtifactsMock, eq(anySet()),
                                            any(AbstractArtifactPrefixPredicate.OnlyDependenciesTrailFilterPredicate.class));
  }

  @Test
  public void applyExclusions() throws Exception {
    filterSpy.applyExclusions(projectArtifactsMock);
    verify(filterSpy.artifactFilter).filter(projectArtifactsMock, eq(anySet()),
                                            any(AbstractArtifactPrefixPredicate.TrailFilterPredicate.class));
  }
}

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.filter;

import org.mule.tools.api.packager.filter.predicate.*;
import org.mule.tools.api.packager.packaging.Exclusion;
import org.mule.tools.api.packager.packaging.Inclusion;
import org.mule.tools.api.util.Artifact;

import java.util.*;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.unmodifiableSet;

/**
 * Filter to mule project dependencies.
 */
public class DependenciesFilter {

  private Set<Artifact> projectArtifacts;
  private List<Exclusion> exclusions;
  private List<Inclusion> inclusions;
  private boolean excludeMuleArtifacts;

  private final HashSet<String> muleGroupIds;
  protected ArtifactFilter artifactFilter;

  /**
   * By default, it is created with the default exclusions (of mule group ids): org.mule, com.mulesource.muleesb,
   * com.mulesoft.muleesb
   * 
   * @param projectArtifacts The project direct dependencies
   * @param inclusions The artifacts to be included
   * @param exclusions The artifacts to be excluded
   * @param excludeMuleArtifacts Whether the mule artifacts should be excluded
   */
  public DependenciesFilter(Set<Artifact> projectArtifacts, List<? extends Inclusion> inclusions,
                            List<? extends Exclusion> exclusions,
                            boolean excludeMuleArtifacts) {
    this.muleGroupIds = newHashSet("org.mule", "com.mulesource.muleesb", "com.mulesoft.muleesb");
    this.projectArtifacts = unmodifiableSet(projectArtifacts);
    this.inclusions = (inclusions == null ? new ArrayList() : inclusions);
    this.exclusions = (exclusions == null ? new ArrayList() : exclusions);
    this.excludeMuleArtifacts = excludeMuleArtifacts;
    this.artifactFilter = new ArtifactFilter();
  }

  public Set<Artifact> getArtifactsToArchive() {
    Set<Artifact> filteredArtifacts = resolveNotCompileNorRuntimeArtifacts(projectArtifacts);
    if (excludeMuleArtifacts) {
      filteredArtifacts = removeDependenciesWithMuleGroupId(filteredArtifacts);
    }
    filteredArtifacts = applyExclusions(filteredArtifacts);
    filteredArtifacts = applyIncludes(filteredArtifacts);
    return filteredArtifacts;
  }

  protected Set<Artifact> resolveNotCompileNorRuntimeArtifacts(Set<Artifact> projectArtifacts) {
    ArtifactPredicate predicate = new CompileOrRuntimeScopePredicate();
    return artifactFilter.filter(projectArtifacts, new HashSet<Artifact>(), predicate);
  }

  protected Set<Artifact> removeDependenciesWithMuleGroupId(Set<Artifact> filteredArtifacts) {
    ArtifactPredicate predicate = new AbstractArtifactPrefixPredicate.OnlyDependenciesTrailFilterPredicate(muleGroupIds);
    return artifactFilter.filter(filteredArtifacts, new HashSet<Artifact>(), predicate);
  }

  protected Set<Artifact> applyExclusions(Set<Artifact> filteredArtifacts) {
    ArtifactPredicate predicate =
        new AbstractArtifactPrefixPredicate.TrailFilterPredicate(getAsFilters(exclusions));
    return artifactFilter.filter(filteredArtifacts, new HashSet<Artifact>(), predicate);
  }

  protected Set<Artifact> applyIncludes(Set<Artifact> filteredArtifacts) {
    ArtifactPredicate predicate =
        new AbstractArtifactPrefixPredicate.NotOptionalTrailFilterPredicate(getAsFilters(inclusions));
    return artifactFilter.filter(projectArtifacts, filteredArtifacts, predicate);
  }

  protected List<String> getAsFilters(List<? extends Exclusion> exclusions) {
    List<String> filters = new ArrayList<>();
    for (Exclusion exclusion : exclusions) {
      filters.add(exclusion.asFilter() + ":");
    }
    return filters;
  }
}

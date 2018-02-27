/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.filter.predicate;

import org.mule.tools.api.packager.filter.prefix.PrefixMatcher;
import org.mule.tools.api.util.Artifact;

import java.util.Collection;
import java.util.List;

public abstract class AbstractArtifactPrefixPredicate implements ArtifactPredicate {

  protected PrefixMatcher matcher;

  public AbstractArtifactPrefixPredicate(Collection<String> prefixes) {
    matcher = new PrefixMatcher(prefixes);
  }

  public abstract boolean test(Artifact artifact);

  public static class TrailFilterPredicate extends AbstractArtifactPrefixPredicate {

    public TrailFilterPredicate(List<String> exclusionPrefixes) {
      super(exclusionPrefixes);
    }

    public boolean test(Artifact artifact) {
      return !matcher.anyMatches(artifact.getDependencyTrail());
    }
  }

  public static class NotOptionalTrailFilterPredicate extends AbstractArtifactPrefixPredicate {

    public NotOptionalTrailFilterPredicate(Collection<String> prefixes) {
      super(prefixes);
    }

    public boolean test(Artifact artifact) {
      return matcher.anyMatches(artifact.getDependencyTrail()) && !artifact.isOptional();
    }
  }

  public static class OnlyDependenciesTrailFilterPredicate extends AbstractArtifactPrefixPredicate {

    public OnlyDependenciesTrailFilterPredicate(Collection<String> prefixes) {
      super(prefixes);
    }

    public boolean test(Artifact artifact) {
      return !matcher.anyMatches(artifact.getOnlyDependenciesTrail());
    }
  }

}

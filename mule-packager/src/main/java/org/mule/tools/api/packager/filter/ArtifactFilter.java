/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.filter;

import org.mule.tools.api.packager.filter.predicate.ArtifactPredicate;
import org.mule.tools.api.util.Artifact;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A class to filter artifacts based on a predicate.
 */
public class ArtifactFilter {

  /**
   * Filter a set of artifacts based on a predicate, adding the ones that evaluate to true to {@param toArtifact}.
   * 
   * @param fromArtifacts The artifacts to be filtered
   * @param toArtifacts The set to where the filtered artifacts are going to be added
   * @param predicate A {@link ArtifactPredicate} that test whether a artifact should be added to {@param toArtifact} from the
   *        {@param toArtifact} set
   * @return The filtered artifacts
   */
  public Set<Artifact> filter(Set<Artifact> fromArtifacts, Set<Artifact> toArtifacts,
                              ArtifactPredicate predicate) {
    checkArgument(fromArtifacts != null, "fromArtifacts cannot be null");
    checkArgument(toArtifacts != null, "toArtifacts cannot be null");
    checkArgument(predicate != null, "predicate cannot be null");

    for (Artifact artifact : fromArtifacts) {
      if (predicate.test(artifact)) {
        toArtifacts.add(artifact);
      }
    }
    return toArtifacts;
  }
}

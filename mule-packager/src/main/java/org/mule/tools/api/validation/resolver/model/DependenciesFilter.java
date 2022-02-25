/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.resolver.model;

import org.apache.commons.lang3.StringUtils;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Filter direct dependencies of projects based on a classifier, scope and type. The type is always fixed as "jar"
 */
public class DependenciesFilter {

  private static final String DEFAULT_TYPE = "jar";
  private String classifier;
  private String scope;

  public DependenciesFilter() {}

  public DependenciesFilter(String classifier, String scope) {
    this.classifier = classifier;
    this.scope = scope;
  }

  /**
   * Retrieves the project direct dependencies that have a {@code scope}, {@code classifier} and {@code DEFAULT_TYPE}.
   * 
   * @return The filtered list of artifacts
   */
  public Set<ArtifactCoordinates> filter(ProjectDependencyNode node) {
    return node.getProject().getDirectDependencies().stream()
        .filter(satisfies(classifier, scope, DEFAULT_TYPE))
        .collect(Collectors.toSet());
  }

  /**
   * Predicate that compares the dependency classifier with the given one
   * 
   * @param classifier The classifier to be compared to
   * @return true if the dependency classifier and {@param classifier} are the same
   */
  protected Predicate<ArtifactCoordinates> hasClassifier(String classifier) {
    return dep -> StringUtils.equals(classifier, dep.getClassifier());
  }

  /**
   * Predicate that compares the dependency scope with the given one
   *
   * @param scope The scope to be compared to
   * @return true if the dependency scope and {@param scope} are the same
   */
  protected Predicate<ArtifactCoordinates> hasScope(String scope) {
    return dep -> StringUtils.equals(scope, dep.getScope());
  }

  /**
   * Predicate that compares the dependency type with the given one
   *
   * @param type The scope to be compared to
   * @return true if the dependency type and {@param type} are the same
   */
  protected Predicate<ArtifactCoordinates> hasType(String type) {
    return dep -> StringUtils.equals(type, dep.getType());
  }

  /**
   * A predicate for {@link ArtifactCoordinates} that returns true if the instance have the specific {@param scope},
   * {@param classifier} and {@param type}.
   * 
   * @param classifier The classifier constraint
   * @param scope The scope constraint
   * @param type The type constraint
   * @return true if the {@code ArtifactCoordinates} satisfies the constraint, false otherwise
   */
  protected Predicate<ArtifactCoordinates> satisfies(String classifier, String scope, String type) {
    return hasClassifier(classifier).and(hasScope(scope)).and(hasType(type));
  }
}

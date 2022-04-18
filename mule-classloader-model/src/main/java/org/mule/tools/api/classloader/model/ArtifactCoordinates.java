/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.classloader.model;

import static com.google.common.base.Preconditions.checkArgument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Builder
@AllArgsConstructor
public class ArtifactCoordinates {

  public static final String DEFAULT_ARTIFACT_TYPE = "jar";

  private final String groupId;
  private final String artifactId;
  private final String version;
  private final String type;
  private final String classifier;
  private final String scope;

  public ArtifactCoordinates() {
    this(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
  }

  public ArtifactCoordinates(String groupId, String artifactId, String version) {
    this(groupId, artifactId, version, DEFAULT_ARTIFACT_TYPE, null);
  }

  public ArtifactCoordinates(String groupId, String artifactId, String version, String type, String classifier) {
    this(groupId, artifactId, version, type, classifier, null);
  }

  public ArtifactCoordinates setGroupId(String groupId) {
    checkArgument(StringUtils.isNotBlank(groupId), "Group id cannot be null nor blank");
    return getCopyBuilder().groupId(groupId).build();
  }

  public ArtifactCoordinates setArtifactId(String artifactId) {
    checkArgument(StringUtils.isNotBlank(artifactId), "Artifact id can not be null nor blank");
    return getCopyBuilder().artifactId(artifactId).build();
  }

  public ArtifactCoordinates setVersion(String version) {
    checkArgument(StringUtils.isNotBlank(version), "Version can not be null nor blank");
    return getCopyBuilder().version(version).build();
  }

  public ArtifactCoordinates setType(String type) {
    checkArgument(StringUtils.isNotBlank(type), "Type can not be null nor blank");
    return getCopyBuilder().type(type).build();
  }

  public ArtifactCoordinates setClassifier(String classifier) {
    return getCopyBuilder().classifier(classifier).build();
  }

  public ArtifactCoordinates setScope(String scope) {
    return getCopyBuilder().scope(scope).build();
  }

  @Override
  public String toString() {
    return Stream.of(groupId, artifactId, version, type, StringUtils.isNotBlank(classifier) ? classifier : null)
        .filter(Objects::nonNull)
        .collect(Collectors.joining(":"));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (Optional.ofNullable(o).map(value -> getClass() != value.getClass()).orElse(true)) {
      return false;
    }

    ArtifactCoordinates that = (ArtifactCoordinates) o;
    return Stream.<Function<ArtifactCoordinates, String>>of(
                                                            ArtifactCoordinates::getArtifactId,
                                                            ArtifactCoordinates::getGroupId,
                                                            ArtifactCoordinates::getClassifier,
                                                            ArtifactCoordinates::getVersion)
        .allMatch(function -> StringUtils.equals(function.apply(ArtifactCoordinates.this), function.apply(that)));
  }

  @Override
  public int hashCode() {
    return Stream.of(getArtifactId(), getGroupId(), getVersion(), getClassifier())
        .filter(Objects::nonNull)
        .map(Objects::hashCode)
        .reduce(1, (result, hashCode) -> 31 * result + hashCode);
  }

  private ArtifactCoordinates.ArtifactCoordinatesBuilder getCopyBuilder() {
    return ArtifactCoordinates.builder()
        .groupId(groupId)
        .artifactId(artifactId)
        .version(version)
        .type(type)
        .classifier(classifier)
        .scope(scope);
  }
}

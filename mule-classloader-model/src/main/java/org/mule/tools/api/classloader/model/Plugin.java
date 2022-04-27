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

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * POJO to model a plugin that will declare additional dependencies.
 *
 * @since 3.2.0
 */
@Getter
@Builder
public class Plugin {

  private final String groupId;
  private final String artifactId;
  private final List<Artifact> additionalDependencies;

  public Plugin() {
    this(StringUtils.EMPTY, StringUtils.EMPTY, Collections.emptyList());
  }

  public Plugin(String groupId, String artifactId, List<Artifact> additionalDependencies) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.additionalDependencies =
        Optional.ofNullable(additionalDependencies).map(list -> (List<Artifact>) ImmutableList.copyOf(list))
            .orElseGet(Collections::emptyList);
  }

  public Plugin setGroupId(String groupId) {
    checkArgument(groupId != null, "groupId cannot be null");
    return getCopyBuilder()
        .groupId(groupId)
        .build();
  }

  public Plugin setArtifactId(String artifactId) {
    checkArgument(artifactId != null, "artifactId cannot be null");
    return getCopyBuilder()
        .artifactId(artifactId)
        .build();
  }

  public Plugin setAdditionalDependencies(List<Artifact> dependencies) {
    checkArgument(dependencies != null, "artifactId cannot be null");
    return getCopyBuilder()
        .additionalDependencies(ImmutableList.copyOf(dependencies))
        .build();
  }

  public Plugin copyWithParameterizedDependenciesUri() {
    ImmutableList<Artifact> dependencies = additionalDependencies.stream()
        .map(Artifact::copyWithParameterizedUri)
        .collect(collectingAndThen(toList(), ImmutableList::copyOf));
    return getCopyBuilder()
        .additionalDependencies(dependencies)
        .build();
  }

  private PluginBuilder getCopyBuilder() {
    return Plugin.builder()
        .groupId(groupId)
        .artifactId(artifactId)
        .additionalDependencies(additionalDependencies);
  }
}

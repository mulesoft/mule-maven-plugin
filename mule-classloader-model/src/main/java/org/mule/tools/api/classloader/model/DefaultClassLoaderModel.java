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
import org.mule.tools.api.classloader.util.ArrayUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

@Getter
@Builder
public class DefaultClassLoaderModel implements ClassLoaderModel<DefaultClassLoaderModel> {

  private final String version;
  private final ArtifactCoordinates artifactCoordinates;
  private final List<Artifact> dependencies;
  private final String[] packages;
  private final String[] resources;
  private final List<Plugin> additionalPluginDependencies;

  public DefaultClassLoaderModel(String version, ArtifactCoordinates artifactCoordinates) {
    this(version, artifactCoordinates, null, null, null);
  }

  public DefaultClassLoaderModel(String version, ArtifactCoordinates artifactCoordinates, List<Artifact> dependencies,
                                 String[] packages,
                                 String[] resources) {
    this(version, artifactCoordinates, dependencies, packages, resources, null);
  }

  protected DefaultClassLoaderModel(String version, ArtifactCoordinates artifactCoordinates, List<Artifact> dependencies,
                                    String[] packages,
                                    String[] resources, List<Plugin> additionalPluginDependencies) {
    checkArgument(artifactCoordinates != null, "Artifact coordinates cannot be null");
    checkArgument(version != null, "Version cannot be null");

    this.version = version;
    this.artifactCoordinates = artifactCoordinates;
    this.packages = Optional.ofNullable(packages).orElseGet(() -> new String[0]);
    this.resources = Optional.ofNullable(resources).orElseGet(() -> new String[0]);
    this.dependencies = Optional.ofNullable(dependencies).map(list -> (List<Artifact>) ImmutableList.copyOf(list))
        .orElseGet(Collections::emptyList);
    this.additionalPluginDependencies =
        Optional.ofNullable(additionalPluginDependencies).map(list -> (List<Plugin>) ImmutableList.copyOf(list))
            .orElseGet(Collections::emptyList);
  }

  @Override
  public DefaultClassLoaderModel setVersion(String version) {
    checkArgument(version != null, "Version cannot be null");
    return getCopyBuilder()
        .version(version)
        .build();
  }

  @Override
  public DefaultClassLoaderModel setArtifactCoordinates(ArtifactCoordinates artifactCoordinates) {
    checkArgument(artifactCoordinates != null, "Artifact coordinates cannot be null");
    return getCopyBuilder()
        .artifactCoordinates(artifactCoordinates)
        .build();
  }

  @Override
  public DefaultClassLoaderModel setDependencies(List<Artifact> dependencies) {
    return getCopyBuilder()
        .dependencies(Optional.ofNullable(dependencies).map(list -> (List<Artifact>) ImmutableList.copyOf(list))
            .orElseGet(Collections::emptyList))
        .build();
  }

  @Override
  public DefaultClassLoaderModel setPackages(String[] packages) {
    return getCopyBuilder()
        .packages(Optional.ofNullable(packages).orElseGet(() -> new String[0]))
        .build();
  }

  @Override
  public DefaultClassLoaderModel setResources(String[] resources) {
    return getCopyBuilder()
        .resources(Optional.ofNullable(resources).orElseGet(() -> new String[0]))
        .build();
  }

  @Override
  public DefaultClassLoaderModel setAdditionalPluginDependencies(List<Plugin> additionalPluginDependencies) {
    return getCopyBuilder()
        .additionalPluginDependencies(Optional.ofNullable(additionalPluginDependencies)
            .map(list -> (List<Plugin>) ImmutableList.copyOf(list)).orElseGet(Collections::emptyList))
        .build();
  }

  public Set<Artifact> getArtifacts() {
    return new TreeSet<>(dependencies);
  }

  public DefaultClassLoaderModel getParametrizedUriModel() {
    return getCopyBuilder()
        .additionalPluginDependencies(Collections.emptyList())
        .dependencies(dependencies.stream().map(Artifact::copyWithParameterizedUri).collect(toList()))
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof DefaultClassLoaderModel)) {
      return false;
    }

    DefaultClassLoaderModel that = (DefaultClassLoaderModel) o;
    return artifactCoordinates.equals(that.artifactCoordinates);
  }

  @Override
  public int hashCode() {
    return artifactCoordinates.hashCode();
  }

  protected DefaultClassLoaderModel.DefaultClassLoaderModelBuilder getCopyBuilder() {
    return DefaultClassLoaderModel.builder()
        .version(version)
        .artifactCoordinates(artifactCoordinates)
        .dependencies(dependencies)
        .additionalPluginDependencies(additionalPluginDependencies)
        .packages(ArrayUtils.copyOf(packages, Function.identity()))
        .resources(ArrayUtils.copyOf(resources, Function.identity()));
  }
}

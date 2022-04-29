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

import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class AppClassLoaderModel extends ClassLoaderModelDecorator<AppClassLoaderModel> {

  public AppClassLoaderModel(ClassLoaderModel<?> classLoaderModel) {
    super(classLoaderModel);
  }

  @Override
  protected AppClassLoaderModel createInstance(ClassLoaderModel<?> classLoaderModel) {
    return new AppClassLoaderModel(classLoaderModel);
  }

  @Override
  public AppClassLoaderModel getParametrizedUriModel() {
    DefaultClassLoaderModel classLoaderModel = new DefaultClassLoaderModel(getVersion(), getArtifactCoordinates());
    List<Plugin> plugins =
        getAdditionalPluginDependencies().stream().map(Plugin::copyWithParameterizedDependenciesUri).collect(toList());
    return createInstance(classLoaderModel.setAdditionalPluginDependencies(plugins));
  }

  @Override
  public Set<Artifact> getArtifacts() {
    return Stream.concat(
                         super.getArtifacts().stream(),
                         getAdditionalPluginDependencies().stream().map(Plugin::getAdditionalDependencies).flatMap(List::stream))
        .collect(Collectors.collectingAndThen(toSet(), ImmutableSet::copyOf));
  }
}

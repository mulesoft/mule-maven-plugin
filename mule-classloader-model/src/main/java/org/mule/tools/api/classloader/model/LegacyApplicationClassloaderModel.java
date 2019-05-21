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

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class LegacyApplicationClassloaderModel extends ApplicationClassloaderModel {

  public LegacyApplicationClassloaderModel(ClassLoaderModel classLoaderModel) {
    super(classLoaderModel);
  }

  @Override
  public void mergeDependencies(Collection<ClassLoaderModel> otherClassloaderModels) {
    for (ClassLoaderModel otherClassLoaderModel : otherClassloaderModels) {
      getClassLoaderModel().getDependencies().stream()
          .filter(dependency -> dependency.getArtifactCoordinates().equals(otherClassLoaderModel.getArtifactCoordinates()))
          .findFirst()
          .orElseThrow(() -> new IllegalStateException(format("Couldn't find an artifact coordinate for '%s' to merge dependencies as inline",
                                                              otherClassLoaderModel.getArtifactCoordinates())));

      if (getNestedClassLoaderModels().putIfAbsent(otherClassLoaderModel.getArtifactCoordinates(),
                                                   otherClassLoaderModel) != null) {
        throw new IllegalArgumentException(format("Duplicated definition of a nested class loader model for artifact coordinates '%s'",
                                                  otherClassLoaderModel.getArtifactCoordinates()));
      }
    }
  }

  @Override
  public List<Artifact> getArtifacts() {
    return ImmutableList.<Artifact>builder()
        .addAll(getClassLoaderModel().getArtifacts())
        .addAll(getNestedClassLoaderModels().values().stream()
            .flatMap(nestedClassLoaderModels -> nestedClassLoaderModels.getArtifacts().stream()).collect(toList()))
        .build();
  }

}

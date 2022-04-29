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
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class ApplicationClassloaderModel extends ClassLoaderModelDecorator<ApplicationClassloaderModel> {

  private final List<ClassLoaderModel<?>> mulePluginsClassloaderModels;

  public ApplicationClassloaderModel(ClassLoaderModel<?> classLoaderModel) {
    this(classLoaderModel, null);
  }

  public ApplicationClassloaderModel(ClassLoaderModel<?> classLoaderModel,
                                     List<ClassLoaderModel<?>> mulePluginsClassloaderModels) {
    super(classLoaderModel);
    this.mulePluginsClassloaderModels = Optional.ofNullable(mulePluginsClassloaderModels)
        .map(list -> (List<ClassLoaderModel<?>>) ImmutableList.copyOf(list))
        .orElseGet(Collections::emptyList);
  }

  @Override
  protected ApplicationClassloaderModel createInstance(ClassLoaderModel<?> classLoaderModel) {
    return new ApplicationClassloaderModel(classLoaderModel);
  }

  public ClassLoaderModel<?> getClassLoaderModel() {
    return classLoaderModel;
  }

  public <T extends ClassLoaderModel<T>> ApplicationClassloaderModel addMulePluginClassloaderModel(T mulePluginClassloaderModel) {
    if (Objects.isNull(mulePluginClassloaderModel)) {
      return this;
    }
    return addAllMulePluginClassloaderModels(Collections.singletonList(mulePluginClassloaderModel));
  }

  public <T extends ClassLoaderModel<T>> ApplicationClassloaderModel addAllMulePluginClassloaderModels(Collection<T> mulePluginClassloaderModels) {
    if (Optional.ofNullable(mulePluginClassloaderModels).map(Collection::isEmpty).orElse(true)) {
      return this;
    }
    return new ApplicationClassloaderModel(classLoaderModel, Stream
        .concat(mulePluginClassloaderModels.stream(), this.mulePluginsClassloaderModels.stream()).collect(Collectors.toList()));
  }

  public Set<Artifact> getArtifacts() {
    return Stream.concat(classLoaderModel.getArtifacts()
        .stream(), mulePluginsClassloaderModels.stream().map(ClassLoaderModel::getArtifacts)
            .flatMap(Collection::stream))
        .collect(Collectors.toSet());
  }
}

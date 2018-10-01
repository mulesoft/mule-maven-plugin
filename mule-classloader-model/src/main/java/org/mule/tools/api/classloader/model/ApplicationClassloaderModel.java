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

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

public class ApplicationClassloaderModel {

  private ClassLoaderModel classLoaderModel;
  private List<ClassLoaderModel> mulePluginsClassloaderModels = new ArrayList<>();
  private List<ClassLoaderModel> ramlsClassloaderModels = new ArrayList<>();

  public ApplicationClassloaderModel(ClassLoaderModel classLoaderModel) {
    this.classLoaderModel = classLoaderModel;
  }

  public ClassLoaderModel getClassLoaderModel() {
    return classLoaderModel;
  }

  public void addMulePluginClassloaderModel(ClassLoaderModel mulePluginClassloaderModel) {
    this.mulePluginsClassloaderModels.add(mulePluginClassloaderModel);
  }

  public void addAllMulePluginClassloaderModels(Collection<ClassLoaderModel> mulePluginClassloaderModels) {
    this.mulePluginsClassloaderModels.addAll(mulePluginClassloaderModels);
  }

  public Set<Artifact> getArtifacts() {
    Set<Artifact> artifacts = new HashSet<>();
    artifacts.addAll(classLoaderModel.getArtifacts());
    artifacts.addAll(mulePluginsClassloaderModels.stream()
        .map(ClassLoaderModel::getArtifacts)
        .flatMap(Collection::stream)
        .collect(Collectors.toList()));
    artifacts.addAll(ramlsClassloaderModels.stream()
        .map(ClassLoaderModel::getArtifacts)
        .flatMap(Collection::stream)
        .collect(Collectors.toList()));
    return artifacts;
  }

  public List<ClassLoaderModel> getMulePluginsClassloaderModels() {
    return mulePluginsClassloaderModels;
  }

  public void addAllRamlClassloaderModels(Collection<ClassLoaderModel> ramlClassloaderModels) {
    this.ramlsClassloaderModels.addAll(ramlClassloaderModels);
  }

  public void addAllRamlToApplicationClassloaderModel(List<Artifact> ramlArtifacts) {
    checkArgument(ramlArtifacts != null, "Raml artifacts list cannot be null");
    for (Artifact artifact : ramlArtifacts) {
      if (!classLoaderModel.getDependencies().contains(artifact)) {
        classLoaderModel.getDependencies().add(artifact);
      }
    }
  }

  public List<ClassLoaderModel> getRamlsClassloaderModels() {
    return ramlsClassloaderModels;
  }
}

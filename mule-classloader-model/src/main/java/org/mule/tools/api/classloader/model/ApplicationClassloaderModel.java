/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.classloader.model;

import java.util.*;
import java.util.stream.Collectors;

public class ApplicationClassloaderModel {

  private ClassLoaderModel classLoaderModel;
  private List<ClassLoaderModel> mulePluginsClassloaderModels = new ArrayList<>();

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
    return artifacts;
  }

  public List<ClassLoaderModel> getMulePluginsClassloaderModels() {
    return mulePluginsClassloaderModels;
  }

  public String[] getPackages() {
    return classLoaderModel.getPackages();
  }

  public String[] getResources() {
    return classLoaderModel.getResources();
  }
}

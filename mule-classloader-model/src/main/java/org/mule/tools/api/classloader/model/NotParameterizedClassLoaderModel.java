/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.classloader.model;

import java.util.List;
import java.util.Set;

/**
 * Decorator for {@link ClassLoaderModel} that will not resolve the URIs
 * parameterized, used when building a class loader model that should reference to the resolved
 * artifact URIs in the local Maven repository.
 *
 * @since 3.4.0
 */
public class NotParameterizedClassLoaderModel extends ClassLoaderModel {

  private final ClassLoaderModel classLoaderModel;

  public NotParameterizedClassLoaderModel(ClassLoaderModel classLoaderModel) {
    super(classLoaderModel.getVersion(), classLoaderModel.getArtifactCoordinates());
    this.classLoaderModel = classLoaderModel;
  }

  @Override
  public String getVersion() {
    return classLoaderModel.getVersion();
  }

  @Override
  public ArtifactCoordinates getArtifactCoordinates() {
    return classLoaderModel.getArtifactCoordinates();
  }

  @Override
  public List<Artifact> getDependencies() {
    return classLoaderModel.getDependencies();
  }

  @Override
  public void setDependencies(List<Artifact> dependencies) {
    classLoaderModel.setDependencies(dependencies);
  }

  @Override
  public Set<Artifact> getArtifacts() {
    return classLoaderModel.getArtifacts();
  }

  @Override
  public ClassLoaderModel getParametrizedUriModel() {
    return this.classLoaderModel;
  }
}

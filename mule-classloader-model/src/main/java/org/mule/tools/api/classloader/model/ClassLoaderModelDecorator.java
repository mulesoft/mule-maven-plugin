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

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class ClassLoaderModelDecorator<T extends ClassLoaderModelDecorator<T>> implements ClassLoaderModel<T> {

  protected final ClassLoaderModel<?> classLoaderModel;

  public ClassLoaderModelDecorator(ClassLoaderModel<?> classLoaderModel) {
    checkArgument(classLoaderModel != null, "classLoaderModel cannot be null");
    this.classLoaderModel = classLoaderModel;
  }

  @Override
  public String getVersion() {
    return classLoaderModel.getVersion();
  }

  @Override
  public T setVersion(String version) {
    return instance(classLoaderModel.setVersion(version));
  }

  @Override
  public ArtifactCoordinates getArtifactCoordinates() {
    return classLoaderModel.getArtifactCoordinates();
  }

  @Override
  public T setArtifactCoordinates(ArtifactCoordinates artifactCoordinates) {
    return instance(classLoaderModel.setArtifactCoordinates(artifactCoordinates));
  }

  @Override
  public List<Artifact> getDependencies() {
    return classLoaderModel.getDependencies();
  }

  @Override
  public T setDependencies(List<Artifact> dependencies) {
    return instance(classLoaderModel.setDependencies(dependencies));
  }

  @Override
  public String[] getPackages() {
    return classLoaderModel.getPackages();
  }

  @Override
  public T setPackages(String[] packages) {
    return instance(classLoaderModel.setPackages(packages));
  }

  @Override
  public String[] getResources() {
    return classLoaderModel.getResources();
  }

  @Override
  public T setResources(String[] resources) {
    return instance(classLoaderModel.setResources(resources));
  }

  @Override
  public List<Plugin> getAdditionalPluginDependencies() {
    return classLoaderModel.getAdditionalPluginDependencies();
  }

  @Override
  public T setAdditionalPluginDependencies(List<Plugin> additionalPluginDependencies) {
    return instance(classLoaderModel.setAdditionalPluginDependencies(additionalPluginDependencies));
  }

  @Override
  public ClassLoaderModel<T> getParametrizedUriModel() {
    return instance(classLoaderModel);
  }

  protected abstract T instance(ClassLoaderModel<?> classLoaderModel);

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof ClassLoaderModel)) {
      return false;
    }

    return classLoaderModel.equals(o);
  }

  @Override
  public int hashCode() {
    return classLoaderModel.getArtifactCoordinates().hashCode();
  }

  @Override
  public Set<Artifact> getArtifacts() {
    return classLoaderModel.getArtifacts();
  }
}

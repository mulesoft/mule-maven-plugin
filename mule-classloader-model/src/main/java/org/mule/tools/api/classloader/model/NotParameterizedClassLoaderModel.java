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

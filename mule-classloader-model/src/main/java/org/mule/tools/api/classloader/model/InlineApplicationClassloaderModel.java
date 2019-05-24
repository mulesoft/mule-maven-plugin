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

import java.util.Collection;
import java.util.List;

/**
 * An implementation of {@link ApplicationClassloaderModel} that defines dependencies for mule-plugin inline.
 */
public class InlineApplicationClassloaderModel extends ApplicationClassloaderModel {

  public InlineApplicationClassloaderModel(ClassLoaderModel classLoaderModel) {
    super(classLoaderModel);
  }

  @Override
  public void mergeDependencies(Collection<ClassLoaderModel> otherClassloaderModels) {
    doMergeDependencies(otherClassloaderModels,
                        context -> context.getArtifact().setDependencies(context.getClassLoaderModel().getDependencies()));
  }

  @Override
  public List<Artifact> getArtifacts() {
    return getClassLoaderModel().getArtifacts();
  }

}

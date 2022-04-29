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

/**
 * Decorator for {@link ClassLoaderModel} that will not resolve the URIs
 * parameterized, used when building a class loader model that should reference to the resolved
 * artifact URIs in the local Maven repository.
 *
 * @since 3.4.0
 */
public class NotParameterizedClassLoaderModel extends ClassLoaderModelDecorator<NotParameterizedClassLoaderModel> {

  public NotParameterizedClassLoaderModel(ClassLoaderModel<?> classLoaderModel) {
    super(classLoaderModel);
  }

  @Override
  protected NotParameterizedClassLoaderModel createInstance(ClassLoaderModel<?> classLoaderModel) {
    return new NotParameterizedClassLoaderModel(classLoaderModel);
  }
}

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.classloader;

import static org.mule.tools.api.classloader.AppClassLoaderModelJsonSerializer.deserialize;

import org.mule.tools.api.classloader.model.ClassLoaderModel;

import java.io.File;

public class AppClassLoaderModelJsonDeserializerValidationsTest extends ClassLoaderModelJsonDeserializerValidationsTest {

  @Override
  protected ClassLoaderModel deserializeClassLoaderModel(File classloaderModelJsonFile) {
    return deserialize(classloaderModelJsonFile);
  }

}

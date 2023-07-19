/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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

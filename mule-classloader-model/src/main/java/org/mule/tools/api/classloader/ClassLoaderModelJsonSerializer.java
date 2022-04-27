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

import com.google.common.collect.ImmutableMap;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ClassLoaderModel;
import org.mule.tools.api.classloader.model.DefaultClassLoaderModel;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("rawtypes")
public class ClassLoaderModelJsonSerializer extends JsonSerializer<ClassLoaderModel> {

  private static final ClassLoaderModelJsonSerializer INSTANCE = new ClassLoaderModelJsonSerializer();

  /**
   * Creates a {@link ClassLoaderModel} from the JSON representation
   *
   * @param file file containing the classloader model in JSON format
   * @return a non null {@link ClassLoaderModel} matching the provided JSON content
   */
  public static ClassLoaderModel<?> deserialize(File file) {
    try {
      return INSTANCE.deserializeFromFile(file);
    } catch (IOException e) {
      throw new RuntimeException("Could not create classloader-model.json", e);
    }
  }

  public ClassLoaderModelJsonSerializer() {
    super(DefaultClassLoaderModel.class, ImmutableMap.of(Artifact.class, new ArtifactCustomJsonSerializer()));
  }
}

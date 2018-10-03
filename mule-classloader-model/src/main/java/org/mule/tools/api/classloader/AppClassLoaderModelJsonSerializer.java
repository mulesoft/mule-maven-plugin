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

import static org.mule.tools.api.classloader.Constants.ADDITIONAL_PLUGIN_DEPENDENCIES_FIELD;
import org.mule.tools.api.classloader.model.AppClassLoaderModel;
import org.mule.tools.api.classloader.model.Artifact;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;

/**
 * Deserializer for an AppClassLoaderModel
 *
 * @since 3.2.0
 */
public class AppClassLoaderModelJsonSerializer extends ClassLoaderModelJsonSerializer {

  public static AppClassLoaderModel deserialize(File classLoaderModelDescriptor) {
    try {
      Gson gson = new GsonBuilder()
          .enableComplexMapKeySerialization()
          .setPrettyPrinting()
          .create();

      Reader reader = new FileReader(classLoaderModelDescriptor);
      AppClassLoaderModel classLoaderModel = gson.fromJson(reader, AppClassLoaderModel.class);
      reader.close();
      return classLoaderModel;
    } catch (IOException e) {
      throw new RuntimeException("Could not create classloader-model.json", e);
    }
  }


  /**
   * Custom JsonSerializer for {@link AppClassLoaderModel}
   *
   * @since 3.2.0
   */
  public static class AppClassLoaderModelCustomJsonSerializer implements JsonSerializer<AppClassLoaderModel> {

    @Override
    public JsonElement serialize(AppClassLoaderModel classLoaderModel, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      Gson gson = new GsonBuilder()
          .enableComplexMapKeySerialization()
          .setPrettyPrinting()
          .registerTypeAdapter(Artifact.class, new ArtifactCustomJsonSerializer())
          .create();
      JsonObject jsonObject = (JsonObject) gson.toJsonTree(classLoaderModel);
      if (classLoaderModel.getAdditionalPluginDependencies().isEmpty()) {
        jsonObject.remove(ADDITIONAL_PLUGIN_DEPENDENCIES_FIELD);
      }
      return jsonObject;
    }
  }


}

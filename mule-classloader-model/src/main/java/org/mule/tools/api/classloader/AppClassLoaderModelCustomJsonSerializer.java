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

import java.lang.reflect.Type;

/**
 * JsonSerializer for an AppClassLoaderModel
 *
 * @since 3.2.0
 */
public class AppClassLoaderModelCustomJsonSerializer implements JsonSerializer<AppClassLoaderModel> {

  @Override
  public JsonElement serialize(AppClassLoaderModel classLoaderModel, Type type,
                               JsonSerializationContext jsonSerializationContext) {
    Gson gson = new GsonBuilder()
        .enableComplexMapKeySerialization()
        .setPrettyPrinting()
        .registerTypeAdapter(Artifact.class, new ArtifactCustomJsonSerializer())
        .create();
    JsonObject jsonObject = (JsonObject) gson.toJsonTree(classLoaderModel);
    if (classLoaderModel.getPluginsWithAdditionalDependencies().isEmpty()) {
      jsonObject.remove(ADDITIONAL_PLUGIN_DEPENDENCIES_FIELD);
    }
    return jsonObject;
  }

}

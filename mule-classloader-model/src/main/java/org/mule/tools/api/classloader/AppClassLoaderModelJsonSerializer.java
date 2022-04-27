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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.mule.tools.api.classloader.model.AppClassLoaderModel;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ClassLoaderModel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static org.mule.tools.api.classloader.Constants.ADDITIONAL_PLUGIN_DEPENDENCIES_FIELD;

/**
 * Deserializer for an AppClassLoaderModel
 *
 * @since 3.2.0
 */
public class AppClassLoaderModelJsonSerializer extends JsonSerializer<AppClassLoaderModel> {

  private static final AppClassLoaderModelJsonSerializer INSTANCE = new AppClassLoaderModelJsonSerializer();

  /**
   * Creates a {@link ClassLoaderModel} from the JSON representation
   *
   * @param file file containing the classloader model in JSON format
   * @return a non null {@link ClassLoaderModel} matching the provided JSON content
   */
  public static AppClassLoaderModel deserialize(File file) {
    try {
      return INSTANCE.deserializeFromFile(file);
    } catch (IOException e) {
      throw new RuntimeException("Could not create classloader-model.json", e);
    }
  }

  public AppClassLoaderModelJsonSerializer() {
    super(AppClassLoaderModel.class, ImmutableMap.of(Artifact.class, new ArtifactCustomJsonSerializer()));
  }

  @Override
  public JsonElement serialize(AppClassLoaderModel appClassLoaderModel, Type type, JsonSerializationContext context) {
    JsonObject jsonObject = (JsonObject) super.serialize(appClassLoaderModel, type, context);
    if (Optional.ofNullable(appClassLoaderModel.getAdditionalPluginDependencies()).map(List::isEmpty).orElse(false)) {
      jsonObject.remove(ADDITIONAL_PLUGIN_DEPENDENCIES_FIELD);
    } else {
      jsonObject.add(ADDITIONAL_PLUGIN_DEPENDENCIES_FIELD, jsonObject.remove(ADDITIONAL_PLUGIN_DEPENDENCIES_FIELD));
    }
    return jsonObject;
  }
}

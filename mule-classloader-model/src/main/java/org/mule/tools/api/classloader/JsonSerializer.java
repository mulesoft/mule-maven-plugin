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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import org.mule.tools.api.classloader.model.AppClassLoaderModel;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ClassLoaderModel;
import org.mule.tools.api.classloader.model.DefaultClassLoaderModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.mule.tools.api.classloader.Constants.CLASSLOADER_MODEL_FILE_NAME;

public class JsonSerializer<T> implements com.google.gson.JsonSerializer<T>, JsonDeserializer<T> {

  protected static final Gson GSON = new GsonBuilder()
      .enableComplexMapKeySerialization()
      .setPrettyPrinting()
      .create();

  /**
   * Serializes the classloader model to a string
   *
   * @param classLoaderModel the classloader model of the application being packaged
   * @return string containing the classloader model's JSON representation
   */
  @Deprecated
  public static String serialize(ClassLoaderModel<?> classLoaderModel) {
    return serialize(classLoaderModel, true);
  }

  /**
   * Serializes the classloader model to the classloader-model.json file in the destination folder
   *
   * @param classLoaderModel the classloader model of the application being packaged
   * @param destinationFolder the directory model where the file is going to be written
   * @return the created File containing the classloader model's JSON representation
   */
  public static File serializeToFile(ClassLoaderModel<?> classLoaderModel, File destinationFolder) {
    return serializeToFile(classLoaderModel, destinationFolder, true);
  }

  /**
   * Serializes the classloader model to the classloader-model.json file in the destination folder
   *
   * @param classLoaderModel the classloader model of the application being packaged
   * @param destinationFolder the directory model where the file is going to be written
   * @param prettyPrinting if {@code true} the json will be printed with pretty print mode
   * @return the created File containing the classloader model's JSON representation
   */
  public static File serializeToFile(ClassLoaderModel<?> classLoaderModel, File destinationFolder, boolean prettyPrinting) {
    File destinationFile = new File(destinationFolder, CLASSLOADER_MODEL_FILE_NAME);
    try {

      if (!destinationFolder.exists()) {
        destinationFolder.mkdirs();
      }

      destinationFile.createNewFile();
      Writer writer = new FileWriter(destinationFile.getAbsolutePath());
      writer.write(serialize(classLoaderModel, prettyPrinting));
      writer.close();
      return destinationFile;
    } catch (IOException e) {
      throw new RuntimeException("Could not create classloader-model.json", e);
    }
  }

  /**
   * Serializes the classloader model to a string
   *
   * @param classLoaderModel the classloader model of the application being packaged
   * @param prettyPrinting if {@code true} the json will be printed with pretty print mode
   * @return string containing the classloader model's JSON representation
   */
  public static String serialize(ClassLoaderModel<?> classLoaderModel, boolean prettyPrinting) {
    GsonBuilder gsonBuilder = GSON.newBuilder()
        .registerTypeAdapter(Artifact.class, new ArtifactCustomJsonSerializer())
        .registerTypeAdapter(AppClassLoaderModel.class, new AppClassLoaderModelJsonSerializer())
        .registerTypeAdapter(DefaultClassLoaderModel.class, new ClassLoaderModelJsonSerializer());

    if (prettyPrinting) {
      gsonBuilder.setPrettyPrinting();
    }

    return gsonBuilder.create().toJson(classLoaderModel.getParametrizedUriModel());
  }

  private final Class<? extends T> clazz;
  private final Gson gson;

  public JsonSerializer(Class<? extends T> clazz) {
    this(clazz, null);
  }

  public JsonSerializer(Class<? extends T> clazz, Map<Class<?>, Object> adapters) {
    checkNotNull(clazz, "clazz cannot be null");
    this.clazz = clazz;
    gson = Optional.ofNullable(adapters).filter(map -> !map.isEmpty())
        .map(this::buildGson)
        .orElse(GSON);
  }

  @Override
  public JsonElement serialize(T src, Type type, JsonSerializationContext context) {
    JsonElement jsonElement = gson.toJsonTree(src);
    return jsonElement.isJsonObject() ? removeNullOrEmpty(jsonElement.getAsJsonObject()) : jsonElement;
  }

  private Gson buildGson(Map<Class<?>, Object> adapters) {
    GsonBuilder gsonBuilder = GSON.newBuilder();
    adapters.forEach(gsonBuilder::registerTypeAdapter);
    return gsonBuilder.create();
  }

  private JsonObject removeNullOrEmpty(JsonObject jsonObject) {
    if (Objects.isNull(jsonObject) || jsonObject.size() == 0) {
      return new JsonObject();
    }

    JsonObject temp = new JsonObject();
    for (String key : jsonObject.keySet()) {
      JsonElement element = jsonObject.get(key);
      if (!element.isJsonNull()) {
        if (!(element.isJsonArray() && element.getAsJsonArray().size() == 0)) {
          temp.add(key, element);
        } else if (element.isJsonObject()) {
          Optional.of(removeNullOrEmpty(element.getAsJsonObject()))
              .filter(object -> object.size() > 0)
              .ifPresent(object -> temp.add(key, object));
        }
      }
    }

    return temp;
  }

  @Override
  public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    return json.isJsonNull() ? null : gson.fromJson(json, clazz);
  }

  public T deserializeFromFile(File file) throws JsonParseException, IOException {
    Reader reader = new BufferedReader(new FileReader(file));
    T instance = gson.fromJson(reader, clazz);
    reader.close();
    return instance;
  }
}

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

import static org.mule.tools.api.classloader.Constants.CLASSLOADER_MODEL_FILE_NAME;
import static org.mule.tools.api.classloader.Constants.PACKAGES_FIELD;
import static org.mule.tools.api.classloader.Constants.RESOURCES_FIELD;

import org.mule.tools.api.classloader.model.AppClassLoaderModel;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ClassLoaderModel;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ClassLoaderModelJsonSerializer {

  /**
   * Creates a {@link ClassLoaderModel} from the JSON representation
   *
   * @param classLoaderModelDescriptor file containing the classloader model in JSON format
   * @return a non null {@link ClassLoaderModel} matching the provided JSON content
   */
  public static ClassLoaderModel deserialize(File classLoaderModelDescriptor) {
    try {
      Gson gson = new GsonBuilder()
          .enableComplexMapKeySerialization()
          .create();

      Reader reader = new FileReader(classLoaderModelDescriptor);
      ClassLoaderModel classLoaderModel = gson.fromJson(reader, ClassLoaderModel.class);
      reader.close();

      return classLoaderModel;
    } catch (IOException e) {
      throw new RuntimeException("Could not create classloader-model.json", e);
    }
  }

  /**
   * Serializes the classloader model to a string
   *
   * @param classLoaderModel the classloader model of the application being packaged
   * @return string containing the classloader model's JSON representation
   */
  @Deprecated
  public static String serialize(ClassLoaderModel classLoaderModel) {
    return serialize(classLoaderModel, true);
  }

  /**
   * Serializes the classloader model to a string
   *
   * @param classLoaderModel the classloader model of the application being packaged
   * @param prettyPrinting if {@code true} the json will be printed with pretty print mode
   * @return string containing the classloader model's JSON representation
   */
  public static String serialize(ClassLoaderModel classLoaderModel, boolean prettyPrinting) {
    GsonBuilder gsonBuilder = new GsonBuilder().enableComplexMapKeySerialization();
    if (prettyPrinting) {
      gsonBuilder = gsonBuilder.setPrettyPrinting();
    }
    Gson gson = gsonBuilder
        .registerTypeAdapter(Artifact.class, new ArtifactCustomJsonSerializer())
        .registerTypeAdapter(AppClassLoaderModel.class,
                             new AppClassLoaderModelJsonSerializer.AppClassLoaderModelCustomJsonSerializer())
        .registerTypeAdapter(ClassLoaderModel.class,
                             new ClassLoaderModelCustomJsonSerializer())
        .create();
    ClassLoaderModel parameterizedClassloaderModel = classLoaderModel.getParametrizedUriModel();
    return gson.toJson(parameterizedClassloaderModel);
  }

  /**
   * Serializes the classloader model to the classloader-model.json file in the destination folder
   *
   * @param classLoaderModel the classloader model of the application being packaged
   * @param destinationFolder the directory model where the file is going to be written
   * @return the created File containing the classloader model's JSON representation
   */
  public static File serializeToFile(ClassLoaderModel classLoaderModel, File destinationFolder) {
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
  public static File serializeToFile(ClassLoaderModel classLoaderModel, File destinationFolder, boolean prettyPrinting) {
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

  private static class ClassLoaderModelCustomJsonSerializer implements JsonSerializer<ClassLoaderModel> {

    @Override
    public JsonElement serialize(ClassLoaderModel classLoaderModel, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      Gson gson = new GsonBuilder()
          .enableComplexMapKeySerialization()
          .registerTypeAdapter(Artifact.class, new ArtifactCustomJsonSerializer())
          .create();
      JsonObject jsonObject = (JsonObject) gson.toJsonTree(classLoaderModel);

      if (classLoaderModel.getPackages() == null || classLoaderModel.getPackages().length == 0) {
        jsonObject.remove(PACKAGES_FIELD);
      }
      if (classLoaderModel.getResources() == null || classLoaderModel.getResources().length == 0) {
        jsonObject.remove(RESOURCES_FIELD);
      }

      return jsonObject;
    }
  }
}

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

import static org.mule.tools.api.classloader.Constants.ARTIFACT_IS_SHARED_FIELD;
import static org.mule.tools.api.classloader.Constants.ARTIFACT_PACKAGES_FIELD;
import static org.mule.tools.api.classloader.Constants.ARTIFACT_RESOURCES_FIELD;

import org.mule.tools.api.classloader.model.Artifact;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Handles {@link Artifact} serialization.
 */
public class ArtifactCustomJsonSerializer implements JsonSerializer<Artifact> {

  @Override
  public JsonElement serialize(Artifact artifact, Type type, JsonSerializationContext jsonSerializationContext) {
    Gson gson = new GsonBuilder().create();
    JsonObject jsonObject = (JsonObject) gson.toJsonTree(artifact);
    if (!artifact.isShared()) {
      jsonObject.remove(ARTIFACT_IS_SHARED_FIELD);
    }
    if (artifact.getPackages() == null || artifact.getPackages().length == 0) {
      jsonObject.remove(ARTIFACT_PACKAGES_FIELD);
    }
    if (artifact.getResources() == null || artifact.getResources().length == 0) {
      jsonObject.remove(ARTIFACT_RESOURCES_FIELD);
    }
    return jsonObject;
  }
}

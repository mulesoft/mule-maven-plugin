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

import org.mule.tools.api.classloader.model.Artifact;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Handles {@link Artifact} serialization.
 */
public class ArtifactCustomJsonSerializer implements JsonSerializer<Artifact> {

  private static final String IS_SHARED_FIELD = "isShared";

  @Override
  public JsonElement serialize(Artifact artifact, Type type, JsonSerializationContext jsonSerializationContext) {
    Gson gson = new Gson();
    JsonObject jsonObject = (JsonObject) gson.toJsonTree(artifact);
    if (!artifact.isShared()) {
      jsonObject.remove(IS_SHARED_FIELD);
    }
    return jsonObject;
  }
}

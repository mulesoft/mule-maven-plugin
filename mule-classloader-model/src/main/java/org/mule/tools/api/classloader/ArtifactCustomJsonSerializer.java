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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.mule.tools.api.classloader.model.Artifact;

import java.lang.reflect.Type;

import static org.mule.tools.api.classloader.Constants.ARTIFACT_IS_SHARED_FIELD;

/**
 * Handles {@link Artifact} serialization.
 */
public class ArtifactCustomJsonSerializer extends JsonSerializer<Artifact> {

  public ArtifactCustomJsonSerializer() {
    super(Artifact.class);
  }

  @Override
  public JsonElement serialize(Artifact artifact, Type type, JsonSerializationContext jsonSerializationContext) {
    JsonObject jsonObject = (JsonObject) super.serialize(artifact, type, jsonSerializationContext);

    if (Boolean.FALSE.equals(artifact.isShared())) {
      jsonObject.remove(ARTIFACT_IS_SHARED_FIELD);
    }

    return jsonObject;
  }
}

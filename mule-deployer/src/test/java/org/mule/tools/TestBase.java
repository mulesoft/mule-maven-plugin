/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class TestBase {

  protected static final String APPLICATION_ID = UUID.randomUUID().toString();
  protected static final String APPLICATION_NAME = "APPLICATION_NAME - 8908 - " + UUID.randomUUID();
  protected static final Random RANDOM = new Random();
  protected static final String GROUP_ID = UUID.randomUUID().toString();
  protected static final String ARTIFACT_ID = UUID.randomUUID().toString();
  protected static final String VERSION = generateVersion();
  protected static final MustacheFactory MUSTACHE = new DefaultMustacheFactory();
  protected static final Gson GSON = new Gson();

  protected static String generateVersion() {
    return String.format("%d.%d.%d", nextInt(5), nextInt(3), nextInt(4));
  }

  protected static int nextInt(int limit) {
    return Math.abs(RANDOM.nextInt(limit));
  }

  protected static String readFile(String file) {
    try {
      return IOUtils.toString(Objects.requireNonNull(TestBase.class.getResource("/" + file)), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected static <T> T fromJson(String jsonString, Class<T> clazz) {
    return GSON.fromJson(jsonString, clazz);
  }

  protected static <T> T fromJsonFile(String file, Class<T> clazz) {
    return fromJson(readFile(file), clazz);
  }

  protected static JsonElement toJsonTree(Object object) {
    return GSON.toJsonTree(object);
  }

  protected static String template(String file, Map<String, Object> data) {
    Writer writer = new StringWriter();
    MUSTACHE.compile(file).execute(writer, data);
    return writer.toString();
  }
}

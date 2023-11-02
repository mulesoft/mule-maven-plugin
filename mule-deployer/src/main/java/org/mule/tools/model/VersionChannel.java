/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum VersionChannel {
  LEGACY, EDGE;

  private static final Map<String, VersionChannel> VALUES =
      Stream.of(VersionChannel.values()).collect(Collectors.toMap(Enum::name, Function.identity()));

  public static VersionChannel fromString(String value) {
    return Optional.ofNullable(value).map(String::toUpperCase).map(VALUES::get).orElse(LEGACY);
  }
}

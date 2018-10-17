/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.arm;

import org.mule.tools.client.model.TargetType;

import java.io.File;
import java.util.Map;

import static java.lang.String.format;

/**
 * Represents all the metadata relative to a application being deployed to ARM.
 */
public class ApplicationMetadata {

  private final File file;
  private final String name;
  private final TargetType targetType;
  private final String target;
  private final Map<String, String> properties;

  public ApplicationMetadata(File file, String name, TargetType targetType, String target, Map<String, String> properties) {
    this.file = file;
    this.name = name;
    this.targetType = targetType;
    this.target = target;
    this.properties = properties;
  }

  public File getFile() {
    return file;
  }

  public String getName() {
    return name;
  }

  public TargetType getTargetType() {
    return targetType;
  }

  public String getTarget() {
    return target;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  @Override
  public String toString() {
    String description = "application %s on %s %s";
    return format(description, name, targetType, target, properties);
  }
}

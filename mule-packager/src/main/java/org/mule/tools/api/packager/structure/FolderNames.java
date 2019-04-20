/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.structure;

public enum FolderNames {
  SRC("src"),

  MAIN("main"),

  MULE("mule"),

  APP("app"),

  TEST("test"),

  MUNIT("munit"),

  TARGET("target"),

  CLASSES("classes"),

  TEST_CLASSES("test-classes"),

  TEST_MULE("test-mule"),

  API("api"),

  WSDL("wsdl"),

  LIB("lib"),

  META_INF("META_INF"),

  MAPPINGS("mappings"),

  JAVA("java"),

  RESOURCES("resources"),

  SITE("site");

  private String value;

  FolderNames(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }
}

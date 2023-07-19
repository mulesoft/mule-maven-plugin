/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.packager.structure;

public enum FolderNames {
  SRC("src"),

  MAIN("main"),

  MULE("mule"),

  POLICY("policy"),

  TEST("test"),

  MUNIT("munit"),

  TARGET("target"),

  CLASSES("classes"),

  TEST_CLASSES("test-classes"),

  TEST_MULE("test-mule"),

  MAVEN("maven"),

  META_INF("META-INF"),

  MULE_SRC("mule-src"),

  MULE_ARTIFACT("mule-artifact"),

  REPOSITORY("repository"),

  TEMP("temp"),

  MUNIT_WORKING_DIR("munitworkingdir"),

  APPLICATION("application"),

  CONTAINER("container"),

  APPLICATIONS("applications"),

  DOMAIN("domain"),

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

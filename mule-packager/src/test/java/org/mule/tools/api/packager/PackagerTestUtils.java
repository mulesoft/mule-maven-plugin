/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class contains static methods used by all the test classes in the package org.mule.tools.packager
 */
public class PackagerTestUtils {

  public static final String CLASSES = "classes";

  public static final String TEST_CLASSES = "test-classes";

  public static final String SRC = "src";
  public static final String MAIN = "main";

  public static final String MULE = "mule";
  public static final String POLICY = "policy";

  public static final String TEST = "test";
  public static final String TEST_MULE = "test-mule";
  public static final String MUNIT = "munit";

  public static final String REPOSITORY = "repository";

  public static final String MAVEN = "maven";
  public static final String MULE_SRC = "mule-src";
  public static final String MULE_ARTIFACT = "mule-artifact";
  public static final String META_INF = "META-INF";

  public static final String POM_PROPERTIES = "pom.properties";


  public static void createFolder(Path path, String fileName, Boolean createFile) throws IOException {
    path.toFile().mkdirs();
    if (createFile) {
      path.resolve(fileName).toFile().createNewFile();
    }
  }

  public static void createEmptyFolder(Path path) throws IOException {
    createFolder(path, "", false);
  }

  public static void assertFolderExist(Path path) {
    assertThat(path.toFile().exists()).describedAs("The folder " + path.toString() + " should exist").isTrue();
  }

  public static void assertFolderIsEmpty(Path path) {
    assertThat(path.toFile().listFiles().length).describedAs("The folder " + path.toString() + " should be empty").isEqualTo(0);
  }

  public static void assertFileExists(Path path) {
    assertThat(path.toFile().exists()).describedAs("The file" + path.toString() + " is missing").isTrue();
  }

  public static void assertFileDoesNotExists(Path path) {
    assertThat(path.toFile().exists()).describedAs("The file" + path.toString() + " should not not exits").isFalse();
  }


}

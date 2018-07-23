/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.nio.file.Path;

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
    assertThat("The folder " + path.toString() + " should exist", path.toFile().exists(), is(true));
  }

  public static void assertFolderIsEmpty(Path path) {
    assertThat("The folder " + path.toString() + " should be empty", path.toFile().listFiles().length, is(0));
  }

  public static void assertFileExists(Path path) {
    assertThat("The file" + path.toString() + " is missing", path.toFile().exists(), is(true));
  }

  public static void assertFileDoesNotExists(Path path) {
    assertThat("The file" + path.toString() + " should not not exits", path.toFile().exists(), is(false));
  }


}

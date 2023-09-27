/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.muleclassloader.model;

import org.junit.jupiter.api.Test;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.ClassLoaderModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClassLoaderModelTest {

  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";

  private final ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION);

  @Test
  void equals() {
    assertThat(new ClassLoaderModel("1.0", artifactCoordinates)).isEqualTo(new ClassLoaderModel("2.0", artifactCoordinates));
  }

  @Test
  void checkNullVersionTest() {
    assertThatThrownBy(() -> new ClassLoaderModel(null, artifactCoordinates)).isExactlyInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void checkNullArtifactCoordinatesTest() {
    assertThatThrownBy(() -> new ClassLoaderModel(VERSION, null)).isExactlyInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void checkNullArgumentsTest() {
    assertThatThrownBy(() -> new ClassLoaderModel(null, null)).isExactlyInstanceOf(IllegalArgumentException.class);
  }
}

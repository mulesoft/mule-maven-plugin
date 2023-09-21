/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.muleclassloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mule.tools.api.muleclassloader.ClassLoaderModelJsonSerializer.deserialize;
import static org.mule.tools.api.muleclassloader.model.ArtifactCoordinates.DEFAULT_ARTIFACT_TYPE;

import static org.apache.commons.io.FileUtils.toFile;

import org.junit.jupiter.api.Test;
import org.mule.tools.api.muleclassloader.model.ClassLoaderModel;
import org.mule.tools.api.muleclassloader.model.ArtifactCoordinates;

import java.io.File;

class ClassLoaderModelJsonDeserializerValidationsTest {

  protected ClassLoaderModel deserializeClassLoaderModel(File classloaderModelJsonFile) {
    return deserialize(classloaderModelJsonFile);
  }

  @Test
  void classLoaderModelNoVersion() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-version.json"));

    assertThatThrownBy(() -> deserializeClassLoaderModel(classloaderModelJsonFile))
        .isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  void classLoaderModelNoArtifactCoordinates() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-artifact-coordinates.json"));

    assertThatThrownBy(() -> deserializeClassLoaderModel(classloaderModelJsonFile))
        .isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  void classLoaderModelNoArtifactCoordinatesGroupId() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-artifact-coordinates-group-id.json"));

    assertThatThrownBy(() -> deserializeClassLoaderModel(classloaderModelJsonFile))
        .isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  void classLoaderModelNoArtifactCoordinatesArtifactId() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-artifact-coordinates-artifact-id.json"));

    assertThatThrownBy(() -> deserializeClassLoaderModel(classloaderModelJsonFile))
        .isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  void classLoaderModelNoArtifactCoordinatesVersion() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-artifact-coordinates-version.json"));

    assertThatThrownBy(() -> deserializeClassLoaderModel(classloaderModelJsonFile))
        .isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  void classLoaderModelNoArtifactCoordinatesType() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-artifact-coordinates-type.json"));

    ClassLoaderModel deserialized = deserializeClassLoaderModel(classloaderModelJsonFile);
    assertThat(deserialized.getArtifactCoordinates().getType(), is(DEFAULT_ARTIFACT_TYPE));
  }

  @Test
  void classLoaderModelNoArtifactCoordinatesClassifier() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-artifact-coordinates-classifier.json"));

    assertThatThrownBy(() -> deserializeClassLoaderModel(classloaderModelJsonFile))
        .isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  void classLoaderModelNoDepArtifactCoordinates() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-artifact-coordinates.json"));

    assertThatThrownBy(() -> deserializeClassLoaderModel(classloaderModelJsonFile))
        .isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  void classLoaderModelNoDepArtifactCoordinatesGroupId() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-artifact-coordinates-group-id.json"));

    assertThatThrownBy(() -> deserializeClassLoaderModel(classloaderModelJsonFile))
        .isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  void classLoaderModelNoDepArtifactCoordinatesArtifactId() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-artifact-coordinates-artifact-id.json"));

    assertThatThrownBy(() -> deserializeClassLoaderModel(classloaderModelJsonFile))
        .isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  void classLoaderModelNoDepArtifactCoordinatesVersion() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-artifact-coordinates-version.json"));

    assertThatThrownBy(() -> deserializeClassLoaderModel(classloaderModelJsonFile))
        .isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  void classLoaderModelNoDepArtifactCoordinatesType() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-artifact-coordinates-type.json"));

    ClassLoaderModel deserialized = deserializeClassLoaderModel(classloaderModelJsonFile);
    assertThat(deserialized.getDependencies().get(0).getArtifactCoordinates().getType(), is(DEFAULT_ARTIFACT_TYPE));
  }

  @Test
  void classLoaderModelNoDepUri() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-uri.json"));

    assertThatThrownBy(() -> deserializeClassLoaderModel(classloaderModelJsonFile))
        .isExactlyInstanceOf(IllegalStateException.class);
  }
}

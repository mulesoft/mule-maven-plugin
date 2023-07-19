/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.classloader;

import static org.mule.tools.api.classloader.ClassLoaderModelJsonSerializer.deserialize;
import static org.mule.tools.api.classloader.model.ArtifactCoordinates.DEFAULT_ARTIFACT_TYPE;

import static org.apache.commons.io.FileUtils.toFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;

import org.mule.tools.api.classloader.model.ClassLoaderModel;

import java.io.File;

import org.junit.Test;

public class ClassLoaderModelJsonDeserializerValidationsTest {

  protected ClassLoaderModel deserializeClassLoaderModel(File classloaderModelJsonFile) {
    return deserialize(classloaderModelJsonFile);
  }

  @Test
  public void classLoaderModelNoVersion() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-version.json"));

    assertThrows(IllegalStateException.class, () -> deserializeClassLoaderModel(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoArtifactCoordinates() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-artifact-coordinates.json"));

    assertThrows(IllegalStateException.class, () -> deserializeClassLoaderModel(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoArtifactCoordinatesGroupId() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-artifact-coordinates-group-id.json"));

    assertThrows(IllegalStateException.class, () -> deserializeClassLoaderModel(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoArtifactCoordinatesArtifactId() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-artifact-coordinates-artifact-id.json"));

    assertThrows(IllegalStateException.class, () -> deserializeClassLoaderModel(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoArtifactCoordinatesVersion() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-artifact-coordinates-version.json"));

    assertThrows(IllegalStateException.class, () -> deserializeClassLoaderModel(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoArtifactCoordinatesType() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-artifact-coordinates-type.json"));

    ClassLoaderModel deserialized = deserializeClassLoaderModel(classloaderModelJsonFile);
    assertThat(deserialized.getArtifactCoordinates().getType(), is(DEFAULT_ARTIFACT_TYPE));
  }

  @Test
  public void classLoaderModelNoArtifactCoordinatesClassifier() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-artifact-coordinates-classifier.json"));

    assertThrows(IllegalStateException.class, () -> deserializeClassLoaderModel(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoDepArtifactCoordinates() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-artifact-coordinates.json"));

    assertThrows(IllegalStateException.class, () -> deserializeClassLoaderModel(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoDepArtifactCoordinatesGroupId() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-artifact-coordinates-group-id.json"));

    assertThrows(IllegalStateException.class, () -> deserializeClassLoaderModel(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoDepArtifactCoordinatesArtifactId() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-artifact-coordinates-artifact-id.json"));

    assertThrows(IllegalStateException.class, () -> deserializeClassLoaderModel(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoDepArtifactCoordinatesVersion() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-artifact-coordinates-version.json"));

    assertThrows(IllegalStateException.class, () -> deserializeClassLoaderModel(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoDepArtifactCoordinatesType() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-artifact-coordinates-type.json"));

    ClassLoaderModel deserialized = deserializeClassLoaderModel(classloaderModelJsonFile);
    assertThat(deserialized.getDependencies().get(0).getArtifactCoordinates().getType(), is(DEFAULT_ARTIFACT_TYPE));
  }

  @Test
  public void classLoaderModelNoDepUri() {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-uri.json"));

    assertThrows(IllegalStateException.class, () -> deserializeClassLoaderModel(classloaderModelJsonFile));
  }

}

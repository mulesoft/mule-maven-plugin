/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
import java.net.URISyntaxException;

import org.junit.Test;

public class ClassLoaderModelJsonDeserializerValidationsTest {

  @Test
  public void classLoaderModelNoVersion() throws URISyntaxException {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-version.json"));

    assertThrows(IllegalStateException.class, () -> deserialize(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoArtifactCoordinates() throws URISyntaxException {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-artifact-coordinates.json"));

    assertThrows(IllegalStateException.class, () -> deserialize(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoArtifactCoordinatesGroupId() throws URISyntaxException {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-artifact-coordinates-group-id.json"));

    assertThrows(IllegalStateException.class, () -> deserialize(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoArtifactCoordinatesArtifactId() throws URISyntaxException {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-artifact-coordinates-artifact-id.json"));

    assertThrows(IllegalStateException.class, () -> deserialize(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoArtifactCoordinatesVersion() throws URISyntaxException {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-artifact-coordinates-version.json"));

    assertThrows(IllegalStateException.class, () -> deserialize(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoArtifactCoordinatesType() throws URISyntaxException {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-artifact-coordinates-type.json"));

    ClassLoaderModel deserialized = deserialize(classloaderModelJsonFile);
    assertThat(deserialized.getArtifactCoordinates().getType(), is(DEFAULT_ARTIFACT_TYPE));
  }

  @Test
  public void classLoaderModelNoDepArtifactCoordinates() throws URISyntaxException {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-artifact-coordinates.json"));

    assertThrows(IllegalStateException.class, () -> deserialize(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoDepArtifactCoordinatesGroupId() throws URISyntaxException {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-artifact-coordinates-group-id.json"));

    assertThrows(IllegalStateException.class, () -> deserialize(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoDepArtifactCoordinatesArtifactId() throws URISyntaxException {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-artifact-coordinates-artifact-id.json"));

    assertThrows(IllegalStateException.class, () -> deserialize(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoDepArtifactCoordinatesVersion() throws URISyntaxException {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-artifact-coordinates-version.json"));

    assertThrows(IllegalStateException.class, () -> deserialize(classloaderModelJsonFile));
  }

  @Test
  public void classLoaderModelNoDepArtifactCoordinatesType() throws URISyntaxException {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-artifact-coordinates-type.json"));

    ClassLoaderModel deserialized = deserialize(classloaderModelJsonFile);
    assertThat(deserialized.getDependencies().get(0).getArtifactCoordinates().getType(), is(DEFAULT_ARTIFACT_TYPE));
  }

  @Test
  public void classLoaderModelNoDepUri() throws URISyntaxException {
    File classloaderModelJsonFile =
        toFile(this.getClass().getClassLoader().getResource("classloader-model-no-dep-uri.json"));

    assertThrows(IllegalStateException.class, () -> deserialize(classloaderModelJsonFile));
  }

}

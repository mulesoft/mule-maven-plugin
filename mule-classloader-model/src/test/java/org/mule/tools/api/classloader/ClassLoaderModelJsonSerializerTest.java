/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.classloader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.ClassLoaderModel;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClassLoaderModelJsonSerializerTest {

  protected static final String GROUP_ID = "org.mule.munit";
  protected static final String ARTIFACT_ID = "fake-id";
  protected static final String VERSION = "1.0.0-SNAPSHOT";

  private static final String TYPE = "jar";
  private static final String CLASSIFIER = "classifier";
  private static final String CLASSLOADER_MODEL_JSON_FILE_NAME = "classloader-model.json";

  @Test
  void classLoaderModelSerializationTest(@TempDir Path tempDir) throws URISyntaxException, IOException {
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    ClassLoaderModel expectedClassLoaderModel = new ClassLoaderModel(VERSION, artifactCoordinates);
    List<Artifact> dependencies = getDependencies();
    expectedClassLoaderModel.setDependencies(dependencies);
    expectedClassLoaderModel.setPackages(new String[0]);
    expectedClassLoaderModel.setResources(new String[0]);
    File classloaderModelJsonFile =
        ClassLoaderModelJsonSerializer.serializeToFile(expectedClassLoaderModel,
                                                       Files.createDirectories(tempDir.resolve("target")).toFile());
    assertThat(classloaderModelJsonFile.getName())
        .as("Classloader model json file name is incorrect")
        .endsWith(CLASSLOADER_MODEL_JSON_FILE_NAME);

    ClassLoaderModel actualClassloaderModel = ClassLoaderModelJsonSerializer.deserialize(classloaderModelJsonFile);
    assertThat(actualClassloaderModel)
        .as("Actual classloader model is not equal to the expected")
        .isEqualTo(expectedClassLoaderModel);
  }

  private List<Artifact> getDependencies() throws URISyntaxException {
    List<Artifact> artifacts = new ArrayList<>();
    for (int i = 0; i < 10; ++i) {
      artifacts.add(createArtifact(i));
    }
    return artifacts;
  }

  private Artifact createArtifact(int i) throws URISyntaxException {
    ArtifactCoordinates coordinates = new ArtifactCoordinates(VERSION, GROUP_ID + i, ARTIFACT_ID + i, TYPE, CLASSIFIER);
    URI uri = new URI("file:/repository/path/" + i);
    return new Artifact(coordinates, uri);
  }
}

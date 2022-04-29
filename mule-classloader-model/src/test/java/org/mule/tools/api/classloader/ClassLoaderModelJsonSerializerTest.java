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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.ClassLoaderModel;
import org.mule.tools.api.classloader.model.DefaultClassLoaderModel;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ClassLoaderModelJsonSerializerTest {

  private static final String GROUP_ID = "org.mule.munit";
  private static final String ARTIFACT_ID = "fake-id";
  private static final String VERSION = "1.0.0-SNAPSHOT";
  private static final String TYPE = "jar";
  private static final String CLASSIFIER = "classifier";
  private static final String CLASSLOADER_MODEL_JSON_FILE_NAME = "classloader-model.json";

  @Test
  public void classLoaderModelSerializationTest(@TempDir Path tempDir) {
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    ClassLoaderModel<?> expectedClassLoaderModel = new DefaultClassLoaderModel(VERSION, artifactCoordinates)
        .setDependencies(getDependencies())
        .setPackages(new String[0])
        .setResources(new String[0]);

    File jsonFile = ClassLoaderModelJsonSerializer.serializeToFile(expectedClassLoaderModel, tempDir.resolve("target").toFile());
    assertThat("Classloader model json file name is incorrect", jsonFile.getName().endsWith(CLASSLOADER_MODEL_JSON_FILE_NAME),
               is(true));

    ClassLoaderModel<?> actualClassloaderModel = ClassLoaderModelJsonSerializer.deserialize(jsonFile);
    assertThat("Actual classloader model is not equal to the expected", actualClassloaderModel,
               equalTo(expectedClassLoaderModel));
  }

  private List<Artifact> getDependencies() {
    return IntStream.range(0, 10).mapToObj(this::createArtifact).collect(Collectors.toList());
  }

  private Artifact createArtifact(int i) {
    ArtifactCoordinates coordinates = new ArtifactCoordinates(VERSION, GROUP_ID + i, ARTIFACT_ID + i, TYPE, CLASSIFIER);
    return new Artifact(coordinates, URI.create("file:/repository/path/" + i));
  }
}

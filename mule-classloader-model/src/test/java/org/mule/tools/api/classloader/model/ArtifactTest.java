/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.classloader.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.lang.String.join;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArtifactTest {

  private static final URI EXPECTED_URI = URI.create("repository/aaa/bbb.jar");
  private static final String RESOURCE_FULL_PATH = "User/lala/repository/aaa/bbb.jar";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String TYPE = "zip";
  private static final String CLASSIFIER = "classifier";
  private static final String PREFIX_GROUP_ID = "group";
  private static final String SUFFIX_GROUP_ID = "id";
  private static final String GROUP_ID = PREFIX_GROUP_ID + "." + SUFFIX_GROUP_ID;
  private static final String OUTPUT_DIRECTORY = join(File.separator, PREFIX_GROUP_ID, SUFFIX_GROUP_ID, ARTIFACT_ID, VERSION);

  public static Stream<Arguments> getFormattedArtifactFileNameTestDataProvider() {
    return Stream.of(
                     Arguments.of("Formatted filename.", CLASSIFIER, ARTIFACT_ID + "-" + VERSION + "-" + CLASSIFIER + "." + TYPE),
                     Arguments.of("Formatted filename without classifier.", StringUtils.EMPTY,
                                  ARTIFACT_ID + "-" + VERSION + "." + TYPE));
  }

  @DisplayName("Checking the constructors.")
  @Test
  public void constructorsTest() {
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    Artifact artifact01 = new Artifact(artifactCoordinates, EXPECTED_URI);
    Artifact artifact02 = new Artifact(artifactCoordinates, EXPECTED_URI, true, new String[] {}, new String[] {});
    Artifact artifact03 = new Artifact(artifactCoordinates, EXPECTED_URI, true, new String[] {"a", "b"}, new String[] {"a"});
    Throwable thrown01 =  assertThrows(NullPointerException.class, () -> new Artifact(null, EXPECTED_URI));
    Throwable thrown02 =  assertThrows(NullPointerException.class, () -> new Artifact(artifactCoordinates, null));

    // artifact01
    assertThat(artifact01.getArtifactCoordinates(), equalTo(artifactCoordinates));
    assertThat(artifact01.getUri(), equalTo(EXPECTED_URI));
    assertThat(artifact01.isShared(), equalTo(false));
    assertThat(artifact01.getPackages(), emptyArray());
    assertThat(artifact01.getResources(), emptyArray());

    // artifact02
    assertThat(artifact02.getArtifactCoordinates(), equalTo(artifactCoordinates));
    assertThat(artifact02.getUri(), equalTo(EXPECTED_URI));
    assertThat(artifact02.isShared(), equalTo(true));
    assertThat(artifact02.getPackages(), emptyArray());
    assertThat(artifact02.getResources(), emptyArray());

    // artifact03
    assertThat(artifact03.getArtifactCoordinates(), equalTo(artifactCoordinates));
    assertThat(artifact03.getUri(), equalTo(EXPECTED_URI));
    assertThat(artifact03.isShared(), equalTo(true));
    assertThat(artifact03.getPackages(), not(emptyArray()));
    assertThat(artifact03.getPackages(), arrayWithSize(2));
    assertThat(artifact03.getResources(), not(emptyArray()));
    assertThat(artifact03.getResources(), arrayWithSize(1));

    // thrown01
    assertThat(thrown01.getMessage(), is("ArtifactCoordinates cannot be null"));
    assertThat(thrown01, instanceOf(NullPointerException.class));

    // thrown02
    assertThat(thrown02.getMessage(), is("Uri cannot be null"));
    assertThat(thrown02, instanceOf(NullPointerException.class));
  }

  @DisplayName("Get formatted maven directory.")
  @Test
  public void getFormattedMavenDirectoryTest(@TempDir Path tempDir) {
    File outputFolder = tempDir.toFile();
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    Artifact artifact = new Artifact(artifactCoordinates, EXPECTED_URI);
    assertThat(
               "Actual formatted output directory is not the expected",
               artifact.getFormattedMavenDirectory(outputFolder).getAbsolutePath(),
               equalTo(new File(outputFolder, OUTPUT_DIRECTORY).getAbsolutePath()));
  }

  @DisplayName("Get formatted artifact filename.")
  @ParameterizedTest(name = "{0}")
  @MethodSource("getFormattedArtifactFileNameTestDataProvider")
  public void getFormattedArtifactFileNameTest(String name, String classifier, String expected) {
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, classifier);
    Artifact artifact = new Artifact(artifactCoordinates, EXPECTED_URI);
    assertThat("Formatted file name is different from the expected", artifact.getFormattedArtifactFileName(), equalTo(expected));
  }

  @DisplayName("Artifacts are not shared by default.")
  @Test
  public void artifactsAreNotSharedByDefaultTest() {
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    Artifact artifact = new Artifact(artifactCoordinates, EXPECTED_URI);
    assertThat(artifact.isShared(), equalTo(false));
  }

  @DisplayName("Artifacts with different classifier are not equal.")
  @Test
  public void artifactsWithDifferentClassifierAreNotEqualTest() {
    Artifact artifact01 =
        new Artifact(new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER), URI.create(RESOURCE_FULL_PATH));
    Artifact artifact02 =
        new Artifact(new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER), URI.create(RESOURCE_FULL_PATH));
    Artifact artifact03 = new Artifact(new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, StringUtils.EMPTY),
                                       URI.create(RESOURCE_FULL_PATH));

    assertThat(artifact01.equals(artifact02), equalTo(true));
    assertThat(artifact02.equals(artifact01), equalTo(true));
    assertThat(artifact01.equals(artifact03), equalTo(false));
    assertThat(artifact02.equals(artifact03), equalTo(false));
    assertThat(artifact03.equals(artifact01), equalTo(false));
    assertThat(artifact03.equals(artifact02), equalTo(false));
  }
}

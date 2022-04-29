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
import org.hamcrest.collection.IsArrayWithSize;
import org.hamcrest.core.AllOf;
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

import static java.io.File.separator;
import static java.io.File.separatorChar;
import static java.lang.String.join;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mule.tools.api.classloader.model.Artifact.getFormattedArtifactFileName;

public class ArtifactTest {

  private static final URI EXPECTED_URI = URI.create("repository/aaa/bbb.jar");
  private static final String MULE_DOMAIN = "mule-domain";
  private static final String RESOURCE_FULL_PATH = "User/lala/repository/aaa/bbb.jar";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String TYPE = "zip";
  private static final String CLASSIFIER = "classifier";
  private static final String PREFIX_GROUP_ID = "group";
  private static final String SUFFIX_GROUP_ID = "id";
  private static final String GROUP_ID = PREFIX_GROUP_ID + "." + SUFFIX_GROUP_ID;
  private static final String OUTPUT_DIRECTORY = join(separator, PREFIX_GROUP_ID, SUFFIX_GROUP_ID, ARTIFACT_ID, VERSION);

  private static Stream<Arguments> getFormattedArtifactFileNameTestDataProvider() {
    return Stream.of(
                     Arguments.of("Formatted filename.", CLASSIFIER, ARTIFACT_ID + "-" + VERSION + "-" + CLASSIFIER + "." + TYPE),
                     Arguments.of("Formatted filename without classifier.", StringUtils.EMPTY,
                                  ARTIFACT_ID + "-" + VERSION + "." + TYPE));
  }

  @DisplayName("Checking the constructors.")
  @Test
  public void constructorsTests() {
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    Artifact artifact01 = new Artifact(artifactCoordinates, EXPECTED_URI);
    Artifact artifact02 = new Artifact(artifactCoordinates, EXPECTED_URI, true, new String[] {}, new String[] {});
    Artifact artifact03 = new Artifact(artifactCoordinates, EXPECTED_URI, true, new String[] {"a", "b"}, new String[] {"a"});
    Throwable thrown01 = assertThrows(NullPointerException.class, () -> new Artifact(null, EXPECTED_URI));
    Throwable thrown02 = assertThrows(NullPointerException.class, () -> new Artifact(artifactCoordinates, null));

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

  @DisplayName("Set Uri tests.")
  @Test
  public void setUriTests() {
    URI uri = URI.create(RESOURCE_FULL_PATH);
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);

    // SETTING NULL URI
    Artifact artifact01 = new Artifact(artifactCoordinates, uri);
    Throwable thrown = assertThrows(NullPointerException.class, () -> artifact01.setUri(null));

    assertThat(thrown.getMessage(), is("Uri cannot be null"));
    assertThat(thrown, instanceOf(NullPointerException.class));

    // MULE_DOMAIN CAN ACCEPT NULL URI
    Artifact artifact02 = new Artifact(new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, MULE_DOMAIN), uri)
        .setUri(null);

    assertThat(artifact02.getUri(), nullValue());

    // SETTING NOT NULL URI
    URI newUri = URI.create("User/lala/repository/aaa/ccc.jar");
    Artifact artifact03 = new Artifact(artifactCoordinates, uri);

    assertThat(artifact03.getUri(), equalTo(uri));
    assertThat(artifact03.setUri(newUri).getUri(), AllOf.allOf(equalTo(newUri), not(equalTo(uri))));
  }

  @DisplayName("Set artifact coordinates tests.")
  @Test
  public void setArtifactCoordinatesTests() {
    URI uri = URI.create(RESOURCE_FULL_PATH);
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);

    // SETTING NULL ARTIFACT COORDINATES
    Artifact artifact = new Artifact(artifactCoordinates, uri);
    Throwable thrown = assertThrows(NullPointerException.class, () -> artifact.setArtifactCoordinates(null));

    assertThat(thrown.getMessage(), is("Artifact coordinates cannot be null"));
    assertThat(thrown, instanceOf(NullPointerException.class));

    // SETTING NOT NULL ARTIFACT COORDINATES
    ArtifactCoordinates newArtifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, MULE_DOMAIN);
    assertThat(artifact.getArtifactCoordinates(), equalTo(artifactCoordinates));
    assertThat(artifact.setArtifactCoordinates(newArtifactCoordinates).getArtifactCoordinates(),
               AllOf.allOf(equalTo(newArtifactCoordinates), not(equalTo(artifactCoordinates))));
  }

  @DisplayName("Set shared tests.")
  @Test
  public void setSharedTests() {
    URI uri = URI.create(RESOURCE_FULL_PATH);
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    Artifact artifact = new Artifact(artifactCoordinates, uri);

    // CHECKING DEFAULT VALUE (FALSE)
    assertThat(artifact.isShared(), equalTo(false));
    assertThat(artifact.getIsShared(), equalTo(false));

    // CHECKING SETTING
    assertThat(artifact.setShared(true).isShared(), equalTo(true));
    assertThat(artifact.setShared(false).isShared(), equalTo(false));
    assertThat(artifact.setShared(true).getIsShared(), equalTo(true));
    assertThat(artifact.setShared(false).getIsShared(), equalTo(false));
  }

  @DisplayName("Set packages tests.")
  @Test
  public void setPackagesTests() {
    URI uri = URI.create(RESOURCE_FULL_PATH);
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    Artifact artifact = new Artifact(artifactCoordinates, uri);

    // CHECKING DEFAULT VALUE (EMPTY ARRAY)
    assertThat(artifact.getPackages(), IsArrayWithSize.emptyArray());

    // CHECKING SETTING
    assertThat(artifact.setPackages(null).getPackages(), IsArrayWithSize.emptyArray());
    assertThat(artifact.setPackages(new String[0]).getPackages(), IsArrayWithSize.emptyArray());
    assertThat(artifact.setPackages(new String[] {"", "", ""}).getPackages(), IsArrayWithSize.arrayWithSize(3));
  }

  @DisplayName("Set resources tests.")
  @Test
  public void setResourcesTests() {
    URI uri = URI.create(RESOURCE_FULL_PATH);
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    Artifact artifact = new Artifact(artifactCoordinates, uri);

    // CHECKING DEFAULT VALUE (EMPTY ARRAY)
    assertThat(artifact.getResources(), IsArrayWithSize.emptyArray());

    // CHECKING SETTING
    assertThat(artifact.setResources(null).getResources(), IsArrayWithSize.emptyArray());
    assertThat(artifact.setResources(new String[0]).getResources(), IsArrayWithSize.emptyArray());
    assertThat(artifact.setResources(new String[] {"", "", "", ""}).getResources(), IsArrayWithSize.arrayWithSize(4));
  }

  @DisplayName("To string tests.")
  @Test
  public void toStringTests() {
    URI uri = URI.create(RESOURCE_FULL_PATH);
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    Artifact artifact = new Artifact(artifactCoordinates, uri);

    assertThat(artifact.toString(), equalTo(artifact.toString()));
    assertThat(String.valueOf(artifact), equalTo(artifact.toString()));
    assertThat(String.valueOf(artifact), equalTo(String.valueOf(artifact)));
    assertThat(artifact.toString(), equalTo(String.valueOf(artifact)));
  }

  @DisplayName("Compare To tests.")
  @Test
  public void compareToTests() {
    URI uri = URI.create(RESOURCE_FULL_PATH);
    ArtifactCoordinates artifactCoordinates01 = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    ArtifactCoordinates artifactCoordinates02 = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, MULE_DOMAIN);
    Artifact artifact01 = new Artifact(artifactCoordinates01, uri);
    Artifact artifact02 = new Artifact(artifactCoordinates02, uri);

    assertThat(artifact01.compareTo(artifactCoordinates01), equalTo(0));
    assertThat(artifact01.compareTo(null), lessThan(0));
    assertThat(artifact01.compareTo(artifactCoordinates02), lessThan(0));
    assertThat(artifact02.compareTo(artifactCoordinates02), equalTo(0));
    assertThat(artifact02.compareTo(null), lessThan(0));
    assertThat(artifact02.compareTo(artifactCoordinates01), greaterThan(0));
  }

  @DisplayName("Equals tests.")
  @Test
  public void equalsTests() {
    URI uri = URI.create(RESOURCE_FULL_PATH);
    ArtifactCoordinates artifactCoordinates01 = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    ArtifactCoordinates artifactCoordinates02 = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, MULE_DOMAIN);
    Artifact artifact01 = new Artifact(artifactCoordinates01, uri);
    Artifact artifact02 = new Artifact(artifactCoordinates01, uri);
    Artifact artifact03 = new Artifact(artifactCoordinates02, uri);

    assertThat(artifact01.equals(artifact01), equalTo(true));
    assertThat(artifact01.equals(null), equalTo(false));
    assertThat(artifact01.equals(artifactCoordinates01), equalTo(false));
    assertThat(artifact01.equals(artifact02), equalTo(true));
    assertThat(artifact01.equals(artifact03), equalTo(false));
  }

  @DisplayName("HashCode test.")
  @Test
  public void hashCodeTest() {
    URI uri = URI.create(RESOURCE_FULL_PATH);
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    Artifact artifact = new Artifact(artifactCoordinates, uri);

    assertThat(artifact.hashCode(), equalTo(artifactCoordinates.hashCode()));
  }

  @DisplayName("Copy with parameterized uri tests.")
  @Test
  public void copyWithParameterizedUriTests() {
    URI uri = URI.create(RESOURCE_FULL_PATH);

    // COPY WITH MULE_DOMAIN CLASSIFIER
    ArtifactCoordinates artifactCoordinates01 = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, MULE_DOMAIN);
    Artifact artifact01 = new Artifact(artifactCoordinates01, uri, true, new String[] {"", "", ""}, new String[] {"", ""});
    Artifact copyOfArtifact01 = artifact01.copyWithParameterizedUri();
    assertThat(copyOfArtifact01.getResources(), IsArrayWithSize.emptyArray());
    assertThat(copyOfArtifact01.getPackages(), IsArrayWithSize.emptyArray());
    assertThat(copyOfArtifact01.getIsShared(), equalTo(false));
    assertThat(copyOfArtifact01.getUri(), equalTo(artifact01.getUri()));
    assertThat(copyOfArtifact01.getArtifactCoordinates(), equalTo(artifact01.getArtifactCoordinates()));

    // COPY WITH OTHER DOMAIN
    ArtifactCoordinates artifactCoordinates02 = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    Artifact artifact02 = new Artifact(artifactCoordinates02, uri, true, new String[] {"", "", ""}, new String[] {"", ""});
    String path = String.join(separator,
                              "repository",
                              GROUP_ID.replace('.', separatorChar),
                              ARTIFACT_ID,
                              VERSION,
                              getFormattedArtifactFileName(artifact02));
    URI newUri = URI.create(path);
    Artifact copyOfArtifact02 = artifact02.copyWithParameterizedUri();
    assertThat(copyOfArtifact02.getResources(), equalTo(artifact02.getResources()));
    assertThat(copyOfArtifact02.getPackages(), equalTo(artifact02.getPackages()));
    assertThat(copyOfArtifact02.getIsShared(), equalTo(artifact01.getIsShared()));
    assertThat(copyOfArtifact02.getArtifactCoordinates(), equalTo(artifact02.getArtifactCoordinates()));
    assertThat(copyOfArtifact02.getUri(), equalTo(newUri));

    // COPY WITH INVALID URI
    ArtifactCoordinates artifactCoordinates03 = new ArtifactCoordinates(GROUP_ID, "-  -", VERSION, TYPE, CLASSIFIER);
    Artifact artifact03 = new Artifact(artifactCoordinates03, uri, true, new String[] {"", "", ""}, new String[] {"", ""});
    Throwable thrown = assertThrows(RuntimeException.class, artifact03::copyWithParameterizedUri);
    assertThat(thrown.getMessage(), containsString("Could not generate URI for resource, the given path is invalid: "));
  }

  @DisplayName("Get pom filename.")
  @Test
  public void getPomFileNameTests() {
    URI uri = URI.create(RESOURCE_FULL_PATH);
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    String pomFileName = new Artifact(artifactCoordinates, uri).getPomFileName();

    assertThat(pomFileName, endsWith(".pom"));
    assertThat(pomFileName, startsWith(ARTIFACT_ID));
    assertThat(pomFileName, containsString(VERSION));
  }
}

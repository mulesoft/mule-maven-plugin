/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.maven.pom.parser.api.model.BundleDependency;
import org.mule.maven.pom.parser.api.model.BundleDescriptor;
import org.mule.tools.api.muleclassloader.model.Artifact;
import org.mule.tools.api.muleclassloader.model.ArtifactCoordinates;
import org.mule.tools.api.util.ArtifactUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ArtifactUtilsTest {

  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String MULE_APP_CLASSIFIER = "mule-application";
  private static final String DEFAULT_ARTIFACT_DESCRIPTOR_TYPE = "jar";
  private static final String POM_TYPE = "pom";
  private static final String MULE_PLUGIN = "mule-plugin";
  private static final String RESOURCE_LOCATION = "/Users/username/.m2/group/id/artifact-id/1.0.0/artifact-id-1.0.0.jar";
  private BundleDescriptor bundleDescriptor;
  private final ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION);
  private URI bundleURI;

  @BeforeEach
  void before() throws URISyntaxException {
    bundleDescriptor =
        new BundleDescriptor.Builder().setGroupId(GROUP_ID).setArtifactId(ARTIFACT_ID).setVersion(VERSION).setBaseVersion(VERSION)
            .build();
    bundleURI = new URI(RESOURCE_LOCATION);
  }

  @Test
  void toArtifactCoordinatesFromMinimumRequirementsTest() {
    ArtifactCoordinates actualArtifactCoordinates = ArtifactUtils.toArtifactCoordinates(bundleDescriptor);
    assertArtifactCoordinates(actualArtifactCoordinates, DEFAULT_ARTIFACT_DESCRIPTOR_TYPE, null);
  }

  @Test
  void toArtifactCoordinatesTest() {
    bundleDescriptor =
        new BundleDescriptor.Builder().setGroupId(GROUP_ID).setArtifactId(ARTIFACT_ID).setVersion(VERSION).setBaseVersion(VERSION)
            .setType(POM_TYPE)
            .setClassifier(MULE_PLUGIN).build();

    ArtifactCoordinates actualArtifactCoordinates = ArtifactUtils.toArtifactCoordinates(bundleDescriptor);

    assertArtifactCoordinates(actualArtifactCoordinates, POM_TYPE, MULE_PLUGIN);
  }

  @Test
  void toDependencyTest() {
    BundleDependency bundleDependency =
        new BundleDependency.Builder().setBundleDescriptor(bundleDescriptor).setBundleUri(bundleURI).build();

    Artifact actualArtifact = ArtifactUtils.toArtifact(bundleDependency);

    assertArtifactCoordinates(actualArtifact.getArtifactCoordinates(), DEFAULT_ARTIFACT_DESCRIPTOR_TYPE, null);
    assertThat(actualArtifact.getUri()).as("Artifact path location is not the expected").isEqualTo(bundleURI);
  }

  private void assertArtifactCoordinates(ArtifactCoordinates actualArtifactCoordinates, String type, String classifier) {
    assertThat(actualArtifactCoordinates.getGroupId()).as("Group id is not the expected").isEqualTo(GROUP_ID);
    assertThat(actualArtifactCoordinates.getArtifactId()).as("Artifact id is not the expected").isEqualTo(ARTIFACT_ID);
    assertThat(actualArtifactCoordinates.getVersion()).as("Version is not the expected").isEqualTo(VERSION);
    assertThat(actualArtifactCoordinates.getType()).as("Type is not the expected").isEqualTo(type);
    assertThat(actualArtifactCoordinates.getClassifier()).as("Classifier is not the expected").isEqualTo(classifier);
  }

  @Test
  void toBundleDescriptorTest() {
    artifactCoordinates.setClassifier(MULE_APP_CLASSIFIER);
    artifactCoordinates.setVersion(VERSION);
    BundleDescriptor actualBundleDescriptor = ArtifactUtils.toBundleDescriptor(artifactCoordinates);
    assertThat(actualBundleDescriptor.getGroupId()).as("The group id is not the expected").isEqualTo(GROUP_ID);
    assertThat(actualBundleDescriptor.getArtifactId()).as("The artifact id is not the expected").isEqualTo(ARTIFACT_ID);
    assertThat(actualBundleDescriptor.getVersion()).as("The version is not the expected").isEqualTo(VERSION);
    assertThat(actualBundleDescriptor.getBaseVersion()).as("The base version is not the expected").isEqualTo(VERSION);
    assertThat(actualBundleDescriptor.getClassifier()).as("The classifier is not the expected")
        .isEqualTo(Optional.of(MULE_APP_CLASSIFIER));
    assertThat(actualBundleDescriptor.getType()).as("The type is not the expected").isEqualTo(DEFAULT_ARTIFACT_DESCRIPTOR_TYPE);
  }
}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.classloader.model.util;

import static java.nio.file.Paths.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mule.tools.api.muleclassloader.model.Artifact.MULE_DOMAIN;
import static org.mule.tools.api.classloader.model.util.ZipUtils.compress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.maven.pom.parser.api.model.BundleDependency;
import org.mule.maven.pom.parser.api.model.BundleDescriptor;
import org.mule.tools.api.muleclassloader.model.ApplicationGAVModel;
import org.mule.tools.api.muleclassloader.model.Artifact;
import org.mule.tools.api.muleclassloader.model.ArtifactCoordinates;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;

class ArtifactUtilsTest {

  private static final String PARENT_VERSION = "2.0.0";
  private static final String PARENT_GROUP_ID = "parent.group.id";
  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String MULE_APP_CLASSIFIER = "mule-application";
  private static final String DEFAULT_ARTIFACT_DESCRIPTOR_TYPE = "jar";
  private static final String POM_TYPE = "pom";
  private static final String MULE_PLUGIN = "mule-plugin";
  private static final String RESOURCE_LOCATION = "groupid/artifact-id/1.0.0/artifact-id-1.0.0.jar";
  private static final String NOT_MULE_PLUGIN = "javadoc";
  private BundleDescriptor bundleDescriptor;
  private ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION);
  private URI bundleURI;
  private Model pomModel;
  private Parent parentProject;

  @TempDir
  public Path temporaryFolder;

  private File localRepository;

  @BeforeEach
  void before() throws IOException {
    localRepository = Files.createDirectories(temporaryFolder.resolve(UUID.randomUUID().toString())).toFile();

    bundleDescriptor =
        new BundleDescriptor.Builder().setGroupId(GROUP_ID).setArtifactId(ARTIFACT_ID).setVersion(VERSION).setBaseVersion(VERSION)
            .build();

    bundleURI = buildBundleURI();
    pomModel = new Model();
    pomModel.setGroupId(GROUP_ID);
    pomModel.setArtifactId(ARTIFACT_ID);
    pomModel.setVersion(VERSION);
    parentProject = new Parent();
    parentProject.setVersion(PARENT_VERSION);
    parentProject.setGroupId(PARENT_GROUP_ID);
    pomModel.setParent(parentProject);
  }

  private URI buildBundleURI() {
    File bundleFile = new File(localRepository.getAbsolutePath(), RESOURCE_LOCATION);
    assertThat(bundleFile.mkdirs()).isTrue();
    return bundleFile.toURI();
  }

  @Test
  void getApplicationArtifactCoordinates() {
    ApplicationGAVModel appGAVModel =
        new ApplicationGAVModel(pomModel.getGroupId(), pomModel.getArtifactId(), pomModel.getVersion());
    ArtifactCoordinates applicationArtifactCoordinates = ArtifactUtils.getApplicationArtifactCoordinates(pomModel, appGAVModel);
    assertThat(applicationArtifactCoordinates.getType()).isEqualTo("jar");
    assertThat(applicationArtifactCoordinates.getClassifier()).isEqualTo(pomModel.getPackaging());
    assertThat(applicationArtifactCoordinates.getGroupId()).isEqualTo(appGAVModel.getGroupId());
    assertThat(applicationArtifactCoordinates.getArtifactId()).isEqualTo(appGAVModel.getArtifactId());
    assertThat(applicationArtifactCoordinates.getVersion()).isEqualTo(appGAVModel.getVersion());
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

  @Test
  void checkPackagesAndResourcesTests() throws Exception {
    BundleDependency bundleDependency =
        new BundleDependency.Builder().setBundleDescriptor(bundleDescriptor).setBundleUri(bundleURI).build();

    File jarFile = FileUtils.toFile(bundleDependency.getBundleUri().toURL());
    jarFile.delete();
    compress(jarFile,
             get(this.getClass().getClassLoader()
                 .getResource("org/mule/tools/api/classloader/model/util/testpackages").toURI())
                     .toFile());

    Artifact actualArtifact = ArtifactUtils.updatePackagesResources(ArtifactUtils.toArtifact(bundleDependency));
    if (!System.getProperty("os.name").startsWith("Windows")) {
      assertArtifactCoordinates(actualArtifact.getArtifactCoordinates(), DEFAULT_ARTIFACT_DESCRIPTOR_TYPE, null);
      assertThat(actualArtifact.getPackages()).as("Artifact packages are not the expected").hasSize(2);
      assertThat(actualArtifact.getPackages()).containsExactly("testpackage1", "testpackage2");
      assertThat(actualArtifact.getResources()).as("Artifact resources are not the expected").hasSize(1);
      assertThat(actualArtifact.getResources()).containsExactly("testpackage1/myresource.properties");
    }
  }

  @Test
  void checkPackagesAndResourcesWithNullUriTests() {
    bundleDescriptor =
        new BundleDescriptor.Builder()
            .setGroupId(GROUP_ID)
            .setArtifactId(ARTIFACT_ID)
            .setVersion(VERSION)
            .setBaseVersion(VERSION)
            .setClassifier(MULE_DOMAIN)
            .build();

    BundleDependency bundleDependency =
        new BundleDependency.Builder().setBundleDescriptor(bundleDescriptor).build();

    Artifact actualArtifact = ArtifactUtils.updatePackagesResources(ArtifactUtils.toArtifact(bundleDependency));

    assertArtifactCoordinates(actualArtifact.getArtifactCoordinates(), DEFAULT_ARTIFACT_DESCRIPTOR_TYPE, MULE_DOMAIN);
    assertThat(actualArtifact.getPackages()).isNull();
    assertThat(actualArtifact.getResources()).isNull();
  }

  private void assertArtifactCoordinates(ArtifactCoordinates actualArtifactCoordinates, String type,
                                         String classifier) {
    assertThat(actualArtifactCoordinates.getGroupId()).as("Group id is not the expected").isEqualTo(GROUP_ID);
    assertThat(actualArtifactCoordinates.getArtifactId()).as("Artifact id is not the expected").isEqualTo(ARTIFACT_ID);
    assertThat(actualArtifactCoordinates.getVersion()).as("Version is not the expected").isEqualTo(VERSION);
    assertThat(actualArtifactCoordinates.getType()).as("Type is not the expected").isEqualTo(type);
    assertThat(actualArtifactCoordinates.getClassifier()).as("Classifier is not the expected").isEqualTo(classifier);

  }

  @Test
  void isNotValidMulePluginMissingClassifierTest() {
    Artifact notAMulePluginDependency = new Artifact(artifactCoordinates, bundleURI);
    assertThat(ArtifactUtils.isValidMulePlugin(notAMulePluginDependency))
        .as("Mule plugin validation method should have returned false").isFalse();
  }

  @Test
  void isNotValidMulePluginWrongClassifierTest() {
    artifactCoordinates =
        new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, DEFAULT_ARTIFACT_DESCRIPTOR_TYPE, NOT_MULE_PLUGIN);
    Artifact notAMulePluginArtifact = new Artifact(artifactCoordinates, bundleURI);
    assertThat(ArtifactUtils.isValidMulePlugin(notAMulePluginArtifact))
        .as("Mule plugin validation method should have returned false").isFalse();
  }

  @Test
  void isValidMulePluginTest() {
    artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, DEFAULT_ARTIFACT_DESCRIPTOR_TYPE, MULE_PLUGIN);
    Artifact mulePluginArtifact = new Artifact(artifactCoordinates, bundleURI);
    assertThat(ArtifactUtils.isValidMulePlugin(mulePluginArtifact)).as("Mule plugin validation method should have returned true")
        .isTrue();
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

  @Test
  void getBundleDescriptorTest() {
    ApplicationGAVModel appGAVModel = new ApplicationGAVModel(GROUP_ID, ARTIFACT_ID, VERSION);
    BundleDescriptor actualBundleDescriptor = ArtifactUtils.getBundleDescriptor(appGAVModel);

    assertThat(actualBundleDescriptor.getGroupId()).as("Group id is not the expected").isEqualTo(GROUP_ID);
    assertThat(actualBundleDescriptor.getArtifactId()).as("Artifact id is not the expected").isEqualTo(ARTIFACT_ID);
    assertThat(actualBundleDescriptor.getVersion()).as("Version is not the expected").isEqualTo(VERSION);
    assertThat(actualBundleDescriptor.getBaseVersion()).as("Base version is not the expected").isEqualTo(VERSION);
    assertThat(actualBundleDescriptor.getType()).as("Type is not the expected").isEqualTo(POM_TYPE);
  }
}

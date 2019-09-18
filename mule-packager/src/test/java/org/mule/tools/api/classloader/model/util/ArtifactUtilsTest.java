/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.classloader.model.util;

import static java.nio.file.Paths.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsArrayContainingInOrder.arrayContaining;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.hamcrest.core.Is.is;
import static org.mule.tools.api.classloader.model.Artifact.MULE_DOMAIN;
import static org.mule.tools.api.classloader.model.util.ZipUtils.compress;

import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.tools.api.classloader.model.ApplicationGAVModel;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.hamcrest.collection.IsArrayWithSize;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class ArtifactUtilsTest {

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

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private File localRepository;

  @Before
  public void before() throws IOException {
    localRepository = temporaryFolder.newFolder();

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
    assertThat(bundleFile.mkdirs(), is(true));
    return bundleFile.toURI();
  }

  @Test
  public void getApplicationArtifactCoordinates() {
    ApplicationGAVModel appGAVModel =
        new ApplicationGAVModel(pomModel.getGroupId(), pomModel.getArtifactId(), pomModel.getVersion());
    ArtifactCoordinates applicationArtifactCoordinates = ArtifactUtils.getApplicationArtifactCoordinates(pomModel, appGAVModel);
    assertThat(applicationArtifactCoordinates.getType(), equalTo("jar"));
    assertThat(applicationArtifactCoordinates.getClassifier(), equalTo(pomModel.getPackaging()));
    assertThat(applicationArtifactCoordinates.getGroupId(), equalTo(appGAVModel.getGroupId()));
    assertThat(applicationArtifactCoordinates.getArtifactId(), equalTo(appGAVModel.getArtifactId()));
    assertThat(applicationArtifactCoordinates.getVersion(), equalTo(appGAVModel.getVersion()));
  }

  @Test
  public void toArtifactCoordinatesFromMinimumRequirementsTest() {
    ArtifactCoordinates actualArtifactCoordinates = ArtifactUtils.toArtifactCoordinates(bundleDescriptor);
    assertArtifactCoordinates(actualArtifactCoordinates, DEFAULT_ARTIFACT_DESCRIPTOR_TYPE, null);
  }

  @Test
  public void toArtifactCoordinatesTest() {
    bundleDescriptor =
        new BundleDescriptor.Builder().setGroupId(GROUP_ID).setArtifactId(ARTIFACT_ID).setVersion(VERSION).setBaseVersion(VERSION)
            .setType(POM_TYPE)
            .setClassifier(MULE_PLUGIN).build();

    ArtifactCoordinates actualArtifactCoordinates = ArtifactUtils.toArtifactCoordinates(bundleDescriptor);

    assertArtifactCoordinates(actualArtifactCoordinates, POM_TYPE, MULE_PLUGIN);
  }

  @Test
  public void toDependencyTest() {
    BundleDependency bundleDependency =
        new BundleDependency.Builder().sedBundleDescriptor(bundleDescriptor).setBundleUri(bundleURI).build();

    Artifact actualArtifact = ArtifactUtils.toArtifact(bundleDependency);

    assertArtifactCoordinates(actualArtifact.getArtifactCoordinates(), DEFAULT_ARTIFACT_DESCRIPTOR_TYPE, null);
    assertThat("Artifact path location is not the expected", actualArtifact.getUri(), equalTo(bundleURI));
  }

  @Test
  public void checkPackagesAndResourcesTests() throws Exception {
    BundleDependency bundleDependency =
        new BundleDependency.Builder().sedBundleDescriptor(bundleDescriptor).setBundleUri(bundleURI).build();

    File jarFile = FileUtils.toFile(bundleDependency.getBundleUri().toURL());
    jarFile.delete();
    compress(jarFile,
             get(this.getClass().getClassLoader().getResource("org/mule/tools/api/classloader/model/util/testpackages").toURI())
                 .toFile());

    Artifact actualArtifact = ArtifactUtils.updatePackagesResources(ArtifactUtils.toArtifact(bundleDependency));

    assertArtifactCoordinates(actualArtifact.getArtifactCoordinates(), DEFAULT_ARTIFACT_DESCRIPTOR_TYPE, null);
    assertThat("Artifact packages are not the expected", actualArtifact.getPackages(), arrayWithSize(2));
    assertThat(actualArtifact.getPackages(), arrayContaining("testpackage1", "testpackage2"));
    assertThat("Artifact resources are not the expected", actualArtifact.getResources(), arrayWithSize(1));
    assertThat(actualArtifact.getResources(), arrayContaining("testpackage1/myresource.properties"));
  }

  @Test
  public void checkPackagesAndResourcesWithNullUriTests() {
    bundleDescriptor =
        new BundleDescriptor.Builder()
            .setGroupId(GROUP_ID)
            .setArtifactId(ARTIFACT_ID)
            .setVersion(VERSION)
            .setBaseVersion(VERSION)
            .setClassifier(MULE_DOMAIN)
            .build();

    BundleDependency bundleDependency =
        new BundleDependency.Builder().sedBundleDescriptor(bundleDescriptor).build();

    Artifact actualArtifact = ArtifactUtils.updatePackagesResources(ArtifactUtils.toArtifact(bundleDependency));

    assertArtifactCoordinates(actualArtifact.getArtifactCoordinates(), DEFAULT_ARTIFACT_DESCRIPTOR_TYPE, MULE_DOMAIN);
    assertThat(actualArtifact.getPackages(), is(nullValue()));
    assertThat(actualArtifact.getResources(), is(nullValue()));
  }

  private void assertArtifactCoordinates(ArtifactCoordinates actualArtifactCoordinates, String type,
                                         String classifier) {
    assertThat("Group id is not the expected", actualArtifactCoordinates.getGroupId(), equalTo(GROUP_ID));
    assertThat("Artifact id is not the expected", actualArtifactCoordinates.getArtifactId(), equalTo(ARTIFACT_ID));
    assertThat("Version is not the expected", actualArtifactCoordinates.getVersion(), equalTo(VERSION));
    assertThat("Type is not the expected", actualArtifactCoordinates.getType(),
               equalTo(type));
    assertThat("Classifier is not the expected", actualArtifactCoordinates.getClassifier(), equalTo(classifier));

  }

  @Test
  public void isNotValidMulePluginMissingClassifierTest() {
    Artifact notAMulePluginDependency = new Artifact(artifactCoordinates, bundleURI);
    assertThat("Mule plugin validation method should have returned false",
               ArtifactUtils.isValidMulePlugin(notAMulePluginDependency),
               is(false));
  }

  @Test
  public void isNotValidMulePluginWrongClassifierTest() {
    artifactCoordinates =
        new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, DEFAULT_ARTIFACT_DESCRIPTOR_TYPE, NOT_MULE_PLUGIN);
    Artifact notAMulePluginArtifact = new Artifact(artifactCoordinates, bundleURI);
    assertThat("Mule plugin validation method should have returned false",
               ArtifactUtils.isValidMulePlugin(notAMulePluginArtifact),
               is(false));
  }

  @Test
  public void isValidMulePluginTest() {
    artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, DEFAULT_ARTIFACT_DESCRIPTOR_TYPE, MULE_PLUGIN);
    Artifact mulePluginArtifact = new Artifact(artifactCoordinates, bundleURI);
    assertThat("Mule plugin validation method should have returned true", ArtifactUtils.isValidMulePlugin(mulePluginArtifact),
               is(true));
  }

  @Test
  public void toBundleDescriptorTest() {
    artifactCoordinates.setClassifier(MULE_APP_CLASSIFIER);
    artifactCoordinates.setVersion(VERSION);
    BundleDescriptor actualBundleDescriptor = ArtifactUtils.toBundleDescriptor(artifactCoordinates);
    assertThat("The group id is not the expected", actualBundleDescriptor.getGroupId(), equalTo(GROUP_ID));
    assertThat("The artifact id is not the expected", actualBundleDescriptor.getArtifactId(), equalTo(ARTIFACT_ID));
    assertThat("The version is not the expected", actualBundleDescriptor.getVersion(), equalTo(VERSION));
    assertThat("The base version is not the expected", actualBundleDescriptor.getBaseVersion(), equalTo(VERSION));
    assertThat("The classifier is not the expected", actualBundleDescriptor.getClassifier(),
               equalTo(Optional.of(MULE_APP_CLASSIFIER)));
    assertThat("The type is not the expected", actualBundleDescriptor.getType(), equalTo(DEFAULT_ARTIFACT_DESCRIPTOR_TYPE));
  }

  @Test
  public void getBundleDescriptorTest() {
    ApplicationGAVModel appGAVModel = new ApplicationGAVModel(GROUP_ID, ARTIFACT_ID, VERSION);
    BundleDescriptor actualBundleDescriptor = ArtifactUtils.getBundleDescriptor(appGAVModel);

    assertThat("Group id is not the expected", actualBundleDescriptor.getGroupId(), equalTo(GROUP_ID));
    assertThat("Artifact id is not the expected", actualBundleDescriptor.getArtifactId(), equalTo(ARTIFACT_ID));
    assertThat("Version is not the expected", actualBundleDescriptor.getVersion(), equalTo(VERSION));
    assertThat("Base version is not the expected", actualBundleDescriptor.getBaseVersion(), equalTo(VERSION));
    assertThat("Type is not the expected", actualBundleDescriptor.getType(), equalTo(POM_TYPE));
  }
}

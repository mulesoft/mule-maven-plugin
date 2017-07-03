/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.dependency.util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.tools.maven.dependency.model.ArtifactCoordinates;
import org.mule.tools.maven.dependency.model.Dependency;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mule.tools.maven.dependency.util.DependencyUtils.isValidMulePlugin;

public class DependencyUtilsTest {

  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String DEFAULT_ARTIFACT_DESCRIPTOR_TYPE = "jar";
  private static final String POM_TYPE = "pom";
  private static final String MULE_PLUGIN = "mule-plugin";
  private static final String RESOURCE_LOCATION = "/Users/username/.m2/group/id/artifact-id/1.0.0/artifact-id-1.0.0.jar";
  private static final String NOT_MULE_PLUGIN = "javadoc";
  private BundleDescriptor bundleDescriptor;
  private ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION);
  private URI bundleURI;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void before() throws URISyntaxException {
    bundleDescriptor =
        new BundleDescriptor.Builder().setGroupId(GROUP_ID).setArtifactId(ARTIFACT_ID).setVersion(VERSION).build();
    bundleURI = new URI(RESOURCE_LOCATION);
  }

  @Test
  public void toArtifactCoordinatesFromMinimumRequirementsTest() {
    ArtifactCoordinates actualArtifactCoordinates = DependencyUtils.toArtifactCoordinates(bundleDescriptor);
    assertArtifactCoordinates(actualArtifactCoordinates, Optional.of(DEFAULT_ARTIFACT_DESCRIPTOR_TYPE), Optional.empty());
  }

  @Test
  public void toArtifactCoordinatesTest() {
    bundleDescriptor =
        new BundleDescriptor.Builder().setGroupId(GROUP_ID).setArtifactId(ARTIFACT_ID).setVersion(VERSION).setType(POM_TYPE)
            .setClassifier(MULE_PLUGIN).build();

    ArtifactCoordinates actualArtifactCoordinates = DependencyUtils.toArtifactCoordinates(bundleDescriptor);

    assertArtifactCoordinates(actualArtifactCoordinates, Optional.of(POM_TYPE), Optional.of(MULE_PLUGIN));
  }

  @Test
  public void toDependencyTest() throws URISyntaxException {
    BundleDependency bundleDependency =
        new BundleDependency.Builder().sedBundleDescriptor(bundleDescriptor).setBundleUri(bundleURI).build();

    Dependency actualDependency = DependencyUtils.toDependency(bundleDependency);

    assertArtifactCoordinates(actualDependency.getArtifactCoordinates(), Optional.of("jar"), Optional.empty());
    assertThat("Dependency path location is not the expected", actualDependency.getPath(), equalTo(bundleURI));
  }

  private void assertArtifactCoordinates(ArtifactCoordinates actualArtifactCoordinates, Optional<String> optionalType,
                                         Optional<Object> optionalClassifier) {
    assertThat("Group id is not the expected", actualArtifactCoordinates.getGroupId(), equalTo(GROUP_ID));
    assertThat("Artifact id is not the expected", actualArtifactCoordinates.getArtifactId(), equalTo(ARTIFACT_ID));
    assertThat("Version is not the expected", actualArtifactCoordinates.getVersion(), equalTo(VERSION));
    assertThat("Type is not the expected", actualArtifactCoordinates.getType(),
               equalTo(optionalType));
    assertThat("Classifier is not the expected", actualArtifactCoordinates.getClassifier(), equalTo(optionalClassifier));

  }

  @Test
  public void isNotValidMulePluginMissingClassifierTest() {
    Dependency notAMulePluginDependency = new Dependency(artifactCoordinates, bundleURI);
    assertThat("Mule plugin validation method should have returned false", isValidMulePlugin(notAMulePluginDependency),
               is(false));
  }

  @Test
  public void isNotValidMulePluginWrongClassifierTest() {
    artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, Optional.empty(), Optional.of(NOT_MULE_PLUGIN));
    Dependency notAMulePluginDependency = new Dependency(artifactCoordinates, bundleURI);
    assertThat("Mule plugin validation method should have returned false", isValidMulePlugin(notAMulePluginDependency),
               is(false));
  }

  @Test
  public void isValidMulePluginTest() {
    artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, Optional.empty(), Optional.of(MULE_PLUGIN));
    Dependency mulePluginDependency = new Dependency(artifactCoordinates, bundleURI);
    assertThat("Mule plugin validation method should have returned true", isValidMulePlugin(mulePluginDependency), is(true));
  }
}

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.validation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MulePluginDependencyResolutionValidatorTest {

  private static final String USER_REPOSITORY_LOCATION =
      "/Users/muleuser/.m2/repository";
  private static final String SEPARATOR = "/";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";
  private static final String GROUP_ID_SEPARATOR = ".";
  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String BASE_VERSION = "1.0.0-SNAPSHOT";
  private static final String TYPE = "jar";
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void verifyThatAreNoTransitiveMulePluginsEmptyMapTest() {
    Map<BundleDependency, List<BundleDependency>> mulePluginDependencies = new HashMap<>();
    MulePluginDependencyResolutionValidator.verifyThatAreNoTransitiveMulePlugins(mulePluginDependencies);
  }

  @Test
  public void verifyThatAreNoTransitiveMulePluginsShouldFailTest() throws URISyntaxException {
    expectedException.expect(IllegalStateException.class);
    BundleDependency validMulePlugin = buildBundleDependency(0, 0, MULE_PLUGIN_CLASSIFIER, BASE_VERSION);
    BundleDependency transitiveMulePluginDependencyOfMulePlugin =
        buildBundleDependency(0, 1, MULE_PLUGIN_CLASSIFIER, BASE_VERSION);
    BundleDependency directDependencyOfMulePlugin = buildBundleDependency(1, 2, "", BASE_VERSION);

    List<BundleDependency> validMulePluginDependencies = new ArrayList<>();
    validMulePluginDependencies.add(transitiveMulePluginDependencyOfMulePlugin);
    validMulePluginDependencies.add(directDependencyOfMulePlugin);

    Map<BundleDependency, List<BundleDependency>> mulePluginDependencies = new HashMap<>();
    mulePluginDependencies.put(validMulePlugin, validMulePluginDependencies);

    MulePluginDependencyResolutionValidator.verifyThatAreNoTransitiveMulePlugins(mulePluginDependencies);
  }

  @Test
  public void verifyThatAreNoTransitiveMuleSuccessfulValidTest() throws URISyntaxException {
    BundleDependency validMulePlugin = buildBundleDependency(0, 0, MULE_PLUGIN_CLASSIFIER, BASE_VERSION);
    BundleDependency directMulePluginDependencyOfMulePlugin = buildBundleDependency(0, 1, MULE_PLUGIN_CLASSIFIER, BASE_VERSION);
    BundleDependency transitiveDependencyOfMulePlugin = buildBundleDependency(0, 2, "", BASE_VERSION);

    List<BundleDependency> validMulePluginDependencies = new ArrayList<>();
    validMulePluginDependencies.add(directMulePluginDependencyOfMulePlugin);

    List<BundleDependency> directMulePluginDependencyDependencies = new ArrayList<>();
    directMulePluginDependencyDependencies.add(transitiveDependencyOfMulePlugin);

    Map<BundleDependency, List<BundleDependency>> mulePluginDependencies = new HashMap<>();
    mulePluginDependencies.put(validMulePlugin, validMulePluginDependencies);
    mulePluginDependencies.put(directMulePluginDependencyOfMulePlugin, directMulePluginDependencyDependencies);

    MulePluginDependencyResolutionValidator.verifyThatAreNoTransitiveMulePlugins(mulePluginDependencies);
  }

  @Test
  public void verifyThatAreNoTransitiveMuleSuccessfulValidWithDifferentTimestampedVersionTest() throws URISyntaxException {
    BundleDependency validMulePlugin = buildBundleDependency(0, 0, MULE_PLUGIN_CLASSIFIER, BASE_VERSION);
    BundleDependency directMulePluginDependencyOfMulePlugin =
        buildBundleDependency(0, 1, MULE_PLUGIN_CLASSIFIER, "1.0.0-20170810.025654-749");
    BundleDependency directMulePluginDependencyOfMulePluginDifferentTimestamp =
        buildBundleDependency(0, 1, MULE_PLUGIN_CLASSIFIER, "1.0.0-20170810.030426-1337");
    BundleDependency transitiveDependencyOfMulePlugin = buildBundleDependency(0, 2, "", BASE_VERSION);

    List<BundleDependency> validMulePluginDependencies = new ArrayList<>();
    validMulePluginDependencies.add(directMulePluginDependencyOfMulePlugin);
    validMulePluginDependencies.add(directMulePluginDependencyOfMulePluginDifferentTimestamp);

    List<BundleDependency> directMulePluginDependencyDependencies = new ArrayList<>();
    directMulePluginDependencyDependencies.add(transitiveDependencyOfMulePlugin);

    Map<BundleDependency, List<BundleDependency>> mulePluginDependencies = new HashMap<>();
    mulePluginDependencies.put(validMulePlugin, validMulePluginDependencies);
    mulePluginDependencies.put(directMulePluginDependencyOfMulePlugin, directMulePluginDependencyDependencies);

    MulePluginDependencyResolutionValidator.verifyThatAreNoTransitiveMulePlugins(mulePluginDependencies);
  }

  private BundleDependency buildBundleDependency(int groupIdSuffix, int artifactIdSuffix, String classifier, String version)
      throws URISyntaxException {
    BundleDescriptor bundleDescriptor = buildBundleDescriptor(groupIdSuffix, artifactIdSuffix, classifier, version);
    URI bundleUri = buildBundleURI(bundleDescriptor);
    return new BundleDependency.Builder().setDescriptor(bundleDescriptor).setBundleUri(bundleUri).build();
  }

  private URI buildBundleURI(BundleDescriptor bundleDescriptor) throws URISyntaxException {
    return new URI(USER_REPOSITORY_LOCATION + SEPARATOR + bundleDescriptor.getGroupId().replace(GROUP_ID_SEPARATOR, SEPARATOR) +
        bundleDescriptor.getArtifactId() + SEPARATOR + bundleDescriptor.getBaseVersion());

  }

  private BundleDescriptor buildBundleDescriptor(int groupIdSuffix, int artifactIdSuffix, String classifier, String version) {
    return new BundleDescriptor.Builder().setGroupId(GROUP_ID + groupIdSuffix).setArtifactId(ARTIFACT_ID + artifactIdSuffix)
        .setVersion(version).setBaseVersion(BASE_VERSION).setType(TYPE).setClassifier(classifier).build();
  }

}

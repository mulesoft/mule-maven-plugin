/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.classloader.model.resolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.maven.client.internal.MuleMavenClient;
import org.mule.maven.pom.parser.api.model.BundleDependency;
import org.mule.maven.pom.parser.api.model.BundleDescriptor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

public class MulePluginClassloaderModelResolverTest {

  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";
  private static final String USER_REPOSITORY_LOCATION = "/Users/muleuser/.m2/repository";
  private static final String SEPARATOR = "/";
  private static final String GROUP_ID_SEPARATOR = ".";
  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String TYPE = "jar";
  private MulePluginClassloaderModelResolver resolver;

  @BeforeEach
  void setUp() {
    resolver = new MulePluginClassloaderModelResolver(mock(MuleMavenClient.class));
  }

  @Test
  void hasSameArtifactId() throws URISyntaxException {
    BundleDependency bundleDependency1 = buildBundleDependency(1, 1, MULE_PLUGIN_CLASSIFIER);
    BundleDependency bundleDependency2 = buildBundleDependency(1, 1, MULE_PLUGIN_CLASSIFIER);
    assertThat(resolver.hasSameArtifactIdAndMajor(bundleDependency1, bundleDependency2)).as("Method should have returned true")
        .isTrue();
  }

  @Test
  void hasSameArtifactIdFalse() throws URISyntaxException {
    BundleDependency bundleDependency1 = buildBundleDependency(1, 1, MULE_PLUGIN_CLASSIFIER);
    BundleDependency bundleDependency2 = buildBundleDependency(1, 2, MULE_PLUGIN_CLASSIFIER);
    assertThat(resolver.hasSameArtifactIdAndMajor(bundleDependency1, bundleDependency2)).as("Method should have returned false")
        .isFalse();
  }

  @Test
  void resolveMulePluginsVersionsPluginsToResolveNull() {
    assertThatThrownBy(() -> resolver.resolveConflicts(null, new ArrayList<>()))
        .isExactlyInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void resolveMulePluginsVersionsDefinitivePluginsNull() {
    assertThatThrownBy(() -> resolver.resolveConflicts(new ArrayList<>(), null))
        .isExactlyInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void resolveMulePluginsVersionsDefinitivePluginsNewer() throws URISyntaxException {
    BundleDependency mulePluginToResolveOlder = buildBundleDependency(1, 1, MULE_PLUGIN_CLASSIFIER, "1.0.0");
    BundleDependency mulePluginToResolveNewer = buildBundleDependency(1, 1, MULE_PLUGIN_CLASSIFIER, "1.0.1");

    List<BundleDependency> mulePluginsToResolve = new ArrayList<>();
    mulePluginsToResolve.add(mulePluginToResolveOlder);

    List<BundleDependency> definitiveMulePlugins = new ArrayList<>();
    definitiveMulePlugins.add(mulePluginToResolveNewer);

    List<BundleDependency> resolvedPlugins =
        resolver.resolveConflicts(mulePluginsToResolve, definitiveMulePlugins);
    assertThat(resolvedPlugins).as("List should contain only the newer version")
        .containsAll(Collections.singletonList(mulePluginToResolveNewer));
    assertThat(resolvedPlugins).as("List should contain only one dependency").hasSize(1);
  }

  @Test
  void resolveMulePluginsVersionsDefinitivePluginsOlder() throws URISyntaxException {
    BundleDependency mulePluginToResolveOlder = buildBundleDependency(1, 1, MULE_PLUGIN_CLASSIFIER, "1.0.0");
    BundleDependency mulePluginToResolveNewer = buildBundleDependency(1, 1, MULE_PLUGIN_CLASSIFIER, "1.0.1");

    List<BundleDependency> mulePluginsToResolve = new ArrayList<>();
    mulePluginsToResolve.add(mulePluginToResolveNewer);

    List<BundleDependency> definitiveMulePlugins = new ArrayList<>();
    definitiveMulePlugins.add(mulePluginToResolveOlder);

    List<BundleDependency> resolvedPlugins =
        resolver.resolveConflicts(mulePluginsToResolve, definitiveMulePlugins);
    assertThat(resolvedPlugins).as("List should contain only the older version")
        .containsAll(Collections.singletonList(mulePluginToResolveOlder));
    assertThat(resolvedPlugins).as("List should contain only one dependency").hasSize(1);
  }

  @Test
  void resolveMulePluginsVersions() throws URISyntaxException {
    BundleDependency mulePluginToResolveOlder = buildBundleDependency(1, 1, MULE_PLUGIN_CLASSIFIER, "1.0.0");
    BundleDependency mulePluginToResolveNewer = buildBundleDependency(1, 1, MULE_PLUGIN_CLASSIFIER, "1.0.1");
    BundleDependency anotherRandomMulePlugin = buildBundleDependency(0, 0, MULE_PLUGIN_CLASSIFIER, "1.0.0");

    List<BundleDependency> mulePluginsToResolve = new ArrayList<>();
    mulePluginsToResolve.add(mulePluginToResolveNewer);
    mulePluginsToResolve.add(anotherRandomMulePlugin);

    List<BundleDependency> definitiveMulePlugins = new ArrayList<>();
    definitiveMulePlugins.add(mulePluginToResolveOlder);

    List<BundleDependency> resolvedPlugins =
        resolver.resolveConflicts(mulePluginsToResolve, definitiveMulePlugins);
    assertThat(resolvedPlugins).as("List should contain only the older version")
        .containsAll(Arrays.asList(mulePluginToResolveOlder, anotherRandomMulePlugin));
    assertThat(resolvedPlugins).as("List should contain only one dependency").hasSize(2);
  }

  private BundleDependency buildBundleDependency(int groupIdSuffix, int artifactIdSuffix, String classifier)
      throws URISyntaxException {
    return buildBundleDependency(groupIdSuffix, artifactIdSuffix, classifier, VERSION);
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
        .setVersion(version).setBaseVersion(version).setType(TYPE).setClassifier(classifier).build();
  }
}

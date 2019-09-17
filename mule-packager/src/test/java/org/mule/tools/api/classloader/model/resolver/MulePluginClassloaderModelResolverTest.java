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

import org.junit.Before;
import org.junit.Test;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.internal.AetherMavenClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class MulePluginClassloaderModelResolverTest {

  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";
  private static final String USER_REPOSITORY_LOCATION =
      "/Users/muleuser/.m2/repository";
  private static final String SEPARATOR = "/";
  private static final String GROUP_ID_SEPARATOR = ".";
  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String TYPE = "jar";
  private MulePluginClassloaderModelResolver resolver;

  @Before
  public void setUp() {
    resolver = new MulePluginClassloaderModelResolver(mock(AetherMavenClient.class));
  }

  @Test
  public void hasSameArtifactId() throws URISyntaxException {
    BundleDependency bundleDependency1 = buildBundleDependency(1, 1, MULE_PLUGIN_CLASSIFIER);
    BundleDependency bundleDependency2 = buildBundleDependency(1, 1, MULE_PLUGIN_CLASSIFIER);
    assertThat("Method should have returned true",
               resolver.hasSameArtifactIdAndMajor(bundleDependency1, bundleDependency2), is(true));
  }

  @Test
  public void hasSameArtifactIdFalse() throws URISyntaxException {
    BundleDependency bundleDependency1 = buildBundleDependency(1, 1, MULE_PLUGIN_CLASSIFIER);
    BundleDependency bundleDependency2 = buildBundleDependency(1, 2, MULE_PLUGIN_CLASSIFIER);
    assertThat("Method should have returned false",
               resolver.hasSameArtifactIdAndMajor(bundleDependency1, bundleDependency2), is(false));
  }

  @Test(expected = IllegalArgumentException.class)
  public void resolveMulePluginsVersionsPluginsToResolveNull() {
    resolver.resolveConflicts(null, new ArrayList<>());
  }

  @Test(expected = IllegalArgumentException.class)
  public void resolveMulePluginsVersionsDefinitivePluginsNull() {
    resolver.resolveConflicts(new ArrayList<>(), null);
  }

  @Test
  public void resolveMulePluginsVersionsDefinitivePluginsNewer() throws URISyntaxException {
    BundleDependency mulePluginToResolveOlder = buildBundleDependency(1, 1, MULE_PLUGIN_CLASSIFIER, "1.0.0");
    BundleDependency mulePluginToResolveNewer = buildBundleDependency(1, 1, MULE_PLUGIN_CLASSIFIER, "1.0.1");

    List<BundleDependency> mulePluginsToResolve = new ArrayList<>();
    mulePluginsToResolve.add(mulePluginToResolveOlder);

    List<BundleDependency> definitiveMulePlugins = new ArrayList<>();
    definitiveMulePlugins.add(mulePluginToResolveNewer);

    List<BundleDependency> resolvedPlugins =
        resolver.resolveConflicts(mulePluginsToResolve, definitiveMulePlugins);
    assertThat("List should contain only the newer version", resolvedPlugins, containsInAnyOrder(mulePluginToResolveNewer));
    assertThat("List should contain only one dependency", resolvedPlugins.size(), equalTo(1));
  }

  @Test
  public void resolveMulePluginsVersionsDefinitivePluginsOlder() throws URISyntaxException {
    BundleDependency mulePluginToResolveOlder = buildBundleDependency(1, 1, MULE_PLUGIN_CLASSIFIER, "1.0.0");
    BundleDependency mulePluginToResolveNewer = buildBundleDependency(1, 1, MULE_PLUGIN_CLASSIFIER, "1.0.1");

    List<BundleDependency> mulePluginsToResolve = new ArrayList<>();
    mulePluginsToResolve.add(mulePluginToResolveNewer);

    List<BundleDependency> definitiveMulePlugins = new ArrayList<>();
    definitiveMulePlugins.add(mulePluginToResolveOlder);

    List<BundleDependency> resolvedPlugins =
        resolver.resolveConflicts(mulePluginsToResolve, definitiveMulePlugins);
    assertThat("List should contain only the older version", resolvedPlugins, containsInAnyOrder(mulePluginToResolveOlder));
    assertThat("List should contain only one dependency", resolvedPlugins.size(), equalTo(1));
  }

  @Test
  public void resolveMulePluginsVersions() throws URISyntaxException {
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
    assertThat("List should contain only the older version", resolvedPlugins,
               containsInAnyOrder(mulePluginToResolveOlder, anotherRandomMulePlugin));
    assertThat("List should contain only one dependency", resolvedPlugins.size(), equalTo(2));
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

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

import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.ADDITIONAL_DEPENDENCIES_ELEMENT;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.ADDITIONAL_PLUGIN_DEPENDENCIES_ELEMENT;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.ARTIFACT_ID_ELEMENET;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.DEPENDENCY_ELEMENT;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.GROUP_ID_ELEMENT;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.MULE_EXTENSIONS_PLUGIN_ARTIFACT_ID;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.MULE_EXTENSIONS_PLUGIN_GROUP_ID;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.MULE_MAVEN_PLUGIN_ARTIFACT_ID;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.MULE_MAVEN_PLUGIN_GROUP_ID;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.PLUGIN_ELEMENT;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.VERSION_ELEMENT;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.ClassLoaderModel;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;


public class AdditionalPluginDependenciesResolverTest {

  private static final String PLUGIN_WITH_ADDITIONAL_DEPENDENCY_ARTIFACT_ID = "test.plugin";
  private static final String PLUGIN_WITH_ADDITIONAL_DEPENDENCY_GROUP_ID = "org.tests.plugins";
  private static final String PLUGIN_WITH_ADDITIONAL_DEPENDENCY_VERSION = "1.0.0";
  private static final String PLUGIN_WITH_ADDITIONAL_DEPENDENCY_CLASSIFIER = "mule-plugin";
  private static final String DEPENDENCY_X_ARTIFACT_ID = "declaredPomDependencyX";
  private static final String DEPENDENCY_X_GROUP_ID = "dep.en.den.cy.x";
  private static final String DEPENDENCY_X_VERSION = "1.0.0";

  private static final Plugin DECLARED_POM_PLUGIN = new Plugin();

  private static final BundleDependency RESOLVED_BUNDLE_PLUGIN;

  static {
    try {
      RESOLVED_BUNDLE_PLUGIN = new BundleDependency.Builder()
          .setDescriptor(new BundleDescriptor.Builder()
              .setArtifactId(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_ARTIFACT_ID)
              .setGroupId(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_GROUP_ID)
              .setVersion(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_VERSION)
              .setClassifier(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_CLASSIFIER)
              .build())
          .setBundleUri(new URI("file://nowhere"))

          .build();
    } catch (URISyntaxException e) {
      throw new MuleRuntimeException(e);
    }
  }

  private static ClassLoaderModel resolvedPluginClassLoaderModel;

  private static Dependency declaredPomDependencyX;

  private static Artifact dependencyXArtifact;
  private static ArtifactCoordinates dependencyXArtifactCoordinates;

  private static BundleDependency resolvedDependencyX;

  private AetherMavenClient mockedMavenClient;


  @Rule
  public ExpectedException expectedException = none();

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @BeforeClass
  public static void setUpClass() {
    DECLARED_POM_PLUGIN.setGroupId(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_GROUP_ID);
    DECLARED_POM_PLUGIN.setArtifactId(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_ARTIFACT_ID);

    resolvedPluginClassLoaderModel = mock(ClassLoaderModel.class);
    when(resolvedPluginClassLoaderModel.getArtifactCoordinates())
        .thenReturn(new ArtifactCoordinates(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_GROUP_ID,
                                            PLUGIN_WITH_ADDITIONAL_DEPENDENCY_ARTIFACT_ID,
                                            PLUGIN_WITH_ADDITIONAL_DEPENDENCY_VERSION));

    declaredPomDependencyX = mock(Dependency.class);
    resolvedDependencyX = mock(BundleDependency.class);
    dependencyXArtifact = mock(Artifact.class);
    dependencyXArtifactCoordinates = mock(ArtifactCoordinates.class);

    when(declaredPomDependencyX.getArtifactId()).thenReturn(DEPENDENCY_X_ARTIFACT_ID);
    when(declaredPomDependencyX.getGroupId()).thenReturn(DEPENDENCY_X_GROUP_ID);
    when(declaredPomDependencyX.getVersion()).thenReturn(DEPENDENCY_X_VERSION);
    when(declaredPomDependencyX.getType()).thenReturn("dep");
    when(dependencyXArtifactCoordinates.getArtifactId()).thenReturn(DEPENDENCY_X_ARTIFACT_ID);
    when(dependencyXArtifactCoordinates.getGroupId()).thenReturn(DEPENDENCY_X_GROUP_ID);
    when(dependencyXArtifactCoordinates.getVersion()).thenReturn(DEPENDENCY_X_VERSION);
    when(dependencyXArtifact.getArtifactCoordinates()).thenReturn(dependencyXArtifactCoordinates);


  }

  @Before
  public void setUp() {
    mockedMavenClient = mock(AetherMavenClient.class);
    when(mockedMavenClient
        .resolveBundleDescriptor(argThat(descriptor -> descriptor.getArtifactId().equals(declaredPomDependencyX.getArtifactId())
            && descriptor.getGroupId().equals(declaredPomDependencyX.getGroupId())))).thenReturn(resolvedDependencyX);
    when(mockedMavenClient.getEffectiveModel(any(), any())).thenReturn(new Model());
  }

  @Before
  public void resetPlugin() {
    DECLARED_POM_PLUGIN.setAdditionalDependencies(emptyList());
    when(resolvedPluginClassLoaderModel.getDependencies()).thenReturn(emptyList());
  }

  private AdditionalPluginDependenciesResolver createAdditionalPluginDependenciesResolver(List<Plugin> pluginsWithAdditionalDependencies)
      throws IOException {
    return new AdditionalPluginDependenciesResolver(mockedMavenClient, pluginsWithAdditionalDependencies,
                                                    temporaryFolder.newFolder());
  }

  @Test
  public void resolutionFailsIfPluginNotDeclared() throws Exception {
    expectedException.expect(MuleRuntimeException.class);
    expectedException.expectMessage("plugin not present");
    createAdditionalPluginDependenciesResolver(of(DECLARED_POM_PLUGIN)).resolveDependencies(emptyList(), emptyList());
  }

  @Test
  public void resolutionFailsIfPluginClassLoaderModelWasNotCreated() throws Exception {
    expectedException.expect(MuleRuntimeException.class);
    expectedException.expectMessage("ClassLoaderModel");
    createAdditionalPluginDependenciesResolver(of(DECLARED_POM_PLUGIN)).resolveDependencies(of(RESOLVED_BUNDLE_PLUGIN),
                                                                                            emptyList());
  }

  @Test
  public void additionalDependenciesGetResolved() throws IOException {
    DECLARED_POM_PLUGIN.setAdditionalDependencies(of(declaredPomDependencyX));
    Map<BundleDependency, List<BundleDependency>> resolvedAdditionalDependencies =
        createAdditionalPluginDependenciesResolver(of(DECLARED_POM_PLUGIN))
            .resolveDependencies(of(RESOLVED_BUNDLE_PLUGIN), of(resolvedPluginClassLoaderModel));
    assertThat(resolvedAdditionalDependencies, hasEntry(equalTo(RESOLVED_BUNDLE_PLUGIN), hasItem(resolvedDependencyX)));
  }

  @Test
  public void additionalDependencyIsNotAddedIfAlreadyAPluginDependency() throws IOException {
    DECLARED_POM_PLUGIN.setAdditionalDependencies(of(declaredPomDependencyX));
    when(resolvedPluginClassLoaderModel.getDependencies()).thenReturn(of(dependencyXArtifact));
    Map<BundleDependency, List<BundleDependency>> resolvedAdditionalDependencies =
        createAdditionalPluginDependenciesResolver(of(DECLARED_POM_PLUGIN))
            .resolveDependencies(of(RESOLVED_BUNDLE_PLUGIN), of(resolvedPluginClassLoaderModel));
    assertThat(resolvedAdditionalDependencies.isEmpty(), equalTo(true));
  }

  @Test
  public void additionalDependenciesFromMulePluginWithNoBuild() throws IOException {
    testNoAdditionalDependenciesMulePluginDependencyPomConfiguration(new Model());
  }

  @Test
  public void additionalDependenciesFromMulePluginWithNoPlugin() throws IOException {
    Model model = new Model();
    model.setBuild(new Build());
    testNoAdditionalDependenciesMulePluginDependencyPomConfiguration(model);
  }

  @Test
  public void additionalDependenciesFromMulePluginWithExtensionsPluginNoConfiguration() throws IOException {
    testAdditionalDependenciesWithNoConfiguration(MULE_EXTENSIONS_PLUGIN_GROUP_ID, MULE_EXTENSIONS_PLUGIN_ARTIFACT_ID);
  }

  @Test
  public void additionalDependenciesFromMulePluginWithApplicationPluginNoConfiguration() throws IOException {
    testAdditionalDependenciesWithNoConfiguration(MULE_MAVEN_PLUGIN_GROUP_ID, MULE_MAVEN_PLUGIN_ARTIFACT_ID);
  }

  @Test
  public void additionalDependenciesFromPluginWithNoAdditionalPluginDependencies() throws IOException {
    Model model = createModelWithConfiguration().getLeft();
    testNoAdditionalDependenciesMulePluginDependencyPomConfiguration(model);
  }

  @Test
  public void additionalDependenciesFromPluginWithEmptyAdditionalPluginDependencies() throws IOException {
    Pair<Model, Xpp3Dom> pair = createModelWithConfiguration();
    pair.getRight().addChild(new Xpp3Dom(ADDITIONAL_PLUGIN_DEPENDENCIES_ELEMENT));
    testNoAdditionalDependenciesMulePluginDependencyPomConfiguration(pair.getLeft());
  }

  @Test
  public void additionalDependenciesFromPluginWithEmptyGroupIdAdditionalPluginDependencies() throws IOException {
    testNoAddtionalPluginDependencyBundleDescriptorField(ARTIFACT_ID_ELEMENET, GROUP_ID_ELEMENT);
  }

  @Test
  public void additionalDependenciesFromPluginWithEmptyArtifactIdAdditionalPluginDependencies() throws IOException {
    testNoAddtionalPluginDependencyBundleDescriptorField(GROUP_ID_ELEMENT, ARTIFACT_ID_ELEMENET);
  }

  @Test
  public void nonEmptyAdditionalDependenciesFromPlugin() throws IOException {
    Pair<Model, Xpp3Dom> pair = createModelWithConfiguration();
    Xpp3Dom additionalPluginDependencies = new Xpp3Dom(ADDITIONAL_PLUGIN_DEPENDENCIES_ELEMENT);
    pair.getRight().addChild(additionalPluginDependencies);
    Xpp3Dom pluginChildren = new Xpp3Dom(PLUGIN_ELEMENT);
    additionalPluginDependencies.addChild(pluginChildren);
    Xpp3Dom artifactId = new Xpp3Dom(ARTIFACT_ID_ELEMENET);
    pluginChildren.addChild(artifactId);
    artifactId.setValue(RESOLVED_BUNDLE_PLUGIN.getDescriptor().getArtifactId());
    Xpp3Dom groupId = new Xpp3Dom(GROUP_ID_ELEMENT);
    pluginChildren.addChild(groupId);
    groupId.setValue(RESOLVED_BUNDLE_PLUGIN.getDescriptor().getGroupId());
    Xpp3Dom additionalDependencies = new Xpp3Dom(ADDITIONAL_DEPENDENCIES_ELEMENT);
    pluginChildren.addChild(additionalDependencies);
    addDependency(additionalDependencies, "1");
    addDependency(additionalDependencies, "2");
    reset(mockedMavenClient);
    when(mockedMavenClient.getEffectiveModel(any(), any())).thenReturn(pair.getLeft());
    Map<BundleDependency, List<BundleDependency>> resolvedAdditionalDependencies =
        createAdditionalPluginDependenciesResolver(emptyList())
            .resolveDependencies(of(RESOLVED_BUNDLE_PLUGIN), of(resolvedPluginClassLoaderModel));
    assertThat(resolvedAdditionalDependencies.size(), is(1));
    List<BundleDependency> dependencies = resolvedAdditionalDependencies.values().iterator().next();
    assertThat(dependencies.size(), is(2));
  }

  private void addDependency(Xpp3Dom additionalDependencies, String suffix) {
    Xpp3Dom additionalDependency1 = new Xpp3Dom(DEPENDENCY_ELEMENT);
    additionalDependencies.addChild(additionalDependency1);
    Xpp3Dom additionalDependency1GroupId = new Xpp3Dom(GROUP_ID_ELEMENT);
    additionalDependency1GroupId.setValue(GROUP_ID_ELEMENT + suffix);
    additionalDependency1.addChild(additionalDependency1GroupId);
    Xpp3Dom additionalDependency1ArtifactId = new Xpp3Dom(ARTIFACT_ID_ELEMENET);
    additionalDependency1ArtifactId.setValue(ARTIFACT_ID_ELEMENET + suffix);
    additionalDependency1.addChild(additionalDependency1ArtifactId);
    Xpp3Dom additionalDependency1Version = new Xpp3Dom(VERSION_ELEMENT);
    additionalDependency1Version.setValue(VERSION_ELEMENT + suffix);
    additionalDependency1.addChild(additionalDependency1Version);
  }

  private void testNoAddtionalPluginDependencyBundleDescriptorField(String partNameWithValue, String partNameWithoutValue)
      throws IOException {
    Pair<Model, Xpp3Dom> pair = createModelWithConfiguration();
    Xpp3Dom additionalPluginDependencies = new Xpp3Dom(ADDITIONAL_PLUGIN_DEPENDENCIES_ELEMENT);
    pair.getRight().addChild(additionalPluginDependencies);
    Xpp3Dom pluginChildren = new Xpp3Dom(PLUGIN_ELEMENT);
    additionalPluginDependencies.addChild(pluginChildren);
    Xpp3Dom artifactId = new Xpp3Dom(partNameWithValue);
    pluginChildren.addChild(artifactId);
    artifactId.setValue("value");
    expectedException.expectMessage(String.format("Expecting child element with not null value %s", partNameWithoutValue));
    testNoAdditionalDependenciesMulePluginDependencyPomConfiguration(pair.getLeft());
  }

  private void testNoAdditionalDependenciesMulePluginDependencyPomConfiguration(Model model) throws IOException {
    reset(mockedMavenClient);
    when(mockedMavenClient.getEffectiveModel(any(), any())).thenReturn(model);
    Map<BundleDependency, List<BundleDependency>> resolvedAdditionalDependencies =
        createAdditionalPluginDependenciesResolver(emptyList())
            .resolveDependencies(of(RESOLVED_BUNDLE_PLUGIN), of(resolvedPluginClassLoaderModel));
    assertThat(resolvedAdditionalDependencies.isEmpty(), is(true));
  }

  private Pair<Model, Xpp3Dom> createModelWithConfiguration() {
    Model model = new Model();
    Build build = new Build();
    model.setBuild(build);
    org.apache.maven.model.Plugin plugin = new org.apache.maven.model.Plugin();
    plugin.setGroupId(MULE_MAVEN_PLUGIN_GROUP_ID);
    plugin.setArtifactId(MULE_MAVEN_PLUGIN_ARTIFACT_ID);
    build.addPlugin(plugin);
    Xpp3Dom configuration = new Xpp3Dom("configuration");
    plugin.setConfiguration(configuration);
    return Pair.of(model, configuration);
  }

  private void testAdditionalDependenciesWithNoConfiguration(String pluginGroupId, String pluginArtifactId) throws IOException {
    Model model = new Model();
    Build build = new Build();
    model.setBuild(build);
    org.apache.maven.model.Plugin plugin = new org.apache.maven.model.Plugin();
    plugin.setGroupId(pluginGroupId);
    plugin.setArtifactId(pluginArtifactId);
    build.addPlugin(plugin);
    testNoAdditionalDependenciesMulePluginDependencyPomConfiguration(model);
  }

}
